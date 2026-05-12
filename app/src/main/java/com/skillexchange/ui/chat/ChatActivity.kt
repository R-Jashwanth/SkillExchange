package com.skillexchange.ui.chat

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillexchange.ui.utils.Constants
import com.skillexchange.adapters.ChatAdapter
import com.skillexchange.databinding.ActivityChatBinding
import com.skillexchange.models.Message
import com.skillexchange.models.Notification
import com.skillexchange.models.Swap
import com.skillexchange.repository.FirestoreRepository
import com.skillexchange.ui.utils.SessionManager
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import com.skillexchange.R
import androidx.core.graphics.toColorInt


class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: ChatAdapter
    private val repository = FirestoreRepository()
    private var swapId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        swapId = intent.getStringExtra("swap_id")
        if (swapId.isNullOrEmpty()) {
            finish()
            return
        }
        setupChat()
        listenForMessages()
        listenForSwapStatus()

        binding.btnSend.setOnClickListener { sendMessage() }
    }

    private fun setupChat() {
        adapter = ChatAdapter(SessionManager.getUid() ?: "")
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = adapter
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty() || swapId.isNullOrEmpty()) return

        val msg = Message(
            swapId    = swapId!!,
            senderId  = SessionManager.getUid() ?: "",
            text      = text,
            timestamp = Timestamp.now()
        )
        repository.db.collection(Constants.MESSAGES).add(msg)
            .addOnSuccessListener { sendChatNotification(text) }
        binding.etMessage.setText("")
    }

    private fun sendChatNotification(messageText: String) {
        repository.db.collection(Constants.SWAPS).document(swapId ?: return)
            .get()
            .addOnSuccessListener { doc ->
                val swap = doc.toObject(Swap::class.java) ?: return@addOnSuccessListener
                val currentUid = SessionManager.getUid() ?: return@addOnSuccessListener
                val receiverId = if (currentUid == swap.requesterId) swap.ownerId else swap.requesterId

                val notifRef = repository.db.collection(Constants.NOTIFICATIONS).document()
                val notification = Notification(
                    id        = notifRef.id,
                    userId    = receiverId,
                    message   = "New message: \"$messageText\"",
                    postId    = swap.postId,
                    swapId    = swapId!!,
                    read      = false,
                    timestamp = Timestamp.now()
                )
                notifRef.set(notification)
            }
    }

    private fun listenForMessages() {
        repository.db.collection(Constants.MESSAGES)
            .whereEqualTo("swapId", swapId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val messages = snapshot.toObjects(Message::class.java)
                    adapter.submitList(messages)
                    if (messages.isNotEmpty()) {
                        binding.rvChat.scrollToPosition(messages.size - 1)
                    }
                }
            }
    }

    private fun listenForSwapStatus() {
        repository.db.collection(Constants.SWAPS)
            .document(swapId ?: "")
            .addSnapshotListener { snapshot, _ ->
                val swap = snapshot?.toObject(Swap::class.java) ?: return@addSnapshotListener
                handleSwapActions(swap)
            }
    }

    private fun handleSwapActions(swap: Swap) {
        val currentUid = SessionManager.getUid() ?: ""

        when {
            // Pending — only the owner (receiver) can accept/reject
            swap.status == "Pending" && currentUid == swap.ownerId -> {
                binding.layoutActions.visibility = View.VISIBLE
                binding.btnReject.visibility     = View.VISIBLE
                binding.btnAccept.setText(R.string.accept_swap)
                binding.btnAccept.backgroundTintList =
                    ColorStateList.valueOf("#2ECC71".toColorInt())
                binding.btnAccept.setOnClickListener {
                    updateSwapStatus("Accepted")
                    sendSystemMessage("✅ Swap accepted! You can now chat.")
                }
                binding.btnReject.setOnClickListener {
                    updateSwapStatus("Rejected")
                    sendSystemMessage("❌ Swap was rejected.")
                }
            }

            // Pending — requester just waits, no buttons
            swap.status == "Pending" && currentUid == swap.requesterId -> {
                binding.layoutActions.visibility = View.GONE
            }

            // Accepted — ONLY the owner (who accepted) sees Mark Completed
            swap.status == "Accepted" && currentUid == swap.ownerId -> {
                binding.layoutActions.visibility = View.VISIBLE
                binding.btnReject.visibility     = View.GONE
                binding.btnAccept.setText(R.string.mark_completed)
                binding.btnAccept.backgroundTintList =
                    ColorStateList.valueOf("#F39C12".toColorInt())
                binding.btnAccept.setOnClickListener { markAsCompleted(swap) }
            }

            // Accepted — requester of THIS swap sees nothing (they need their own swap)
            swap.status == "Accepted" && currentUid == swap.requesterId -> {
                binding.layoutActions.visibility = View.GONE
            }

            // Completed or anything else — hide everything
            else -> {
                binding.layoutActions.visibility = View.GONE
            }
        }
    }

    private fun sendSystemMessage(text: String) {
        val msg = Message(
            swapId    = swapId ?: return,
            senderId  = "system",
            text      = text,
            timestamp = Timestamp.now()
        )
        repository.db.collection(Constants.MESSAGES).add(msg)
    }

    private fun updateSwapStatus(status: String) {
        repository.updateSwapStatus(swapId ?: "", status)
    }

    private fun markAsCompleted(swap: Swap) {
        val currentUid = SessionManager.getUid() ?: ""
        val updates = if (currentUid == swap.requesterId)
            mapOf("requesterConfirmed" to true)
        else
            mapOf("ownerConfirmed" to true)

        repository.db.collection(Constants.SWAPS).document(swap.id)
            .update(updates)
            .addOnSuccessListener {
                repository.db.collection(Constants.SWAPS).document(swap.id)
                    .get()
                    .addOnSuccessListener { fresh ->
                        val s = fresh.toObject(Swap::class.java) ?: return@addOnSuccessListener

                        // Mark this swap as completed
                        repository.updateSwapStatus(s.id, "Completed")

                        // Increase trust score and swaps done for BOTH users
                        repository.incrementTrustScore(s.requesterId)
                        repository.incrementTrustScore(s.ownerId)
                        repository.incrementSwapsDone(s.requesterId)
                        repository.incrementSwapsDone(s.ownerId)

                        Toast.makeText(
                            this,
                            "Swap completed! Trust scores updated 🎉",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
    }
}