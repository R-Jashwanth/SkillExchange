package com.skillexchange.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.skillexchange.databinding.ActivityPostDetailBinding
import com.skillexchange.firebase.FirebaseClient
import com.skillexchange.models.Notification
import com.skillexchange.models.Post
import com.skillexchange.models.Swap
import com.skillexchange.repository.FirestoreRepository
import com.skillexchange.ui.chat.ChatActivity
import com.skillexchange.ui.utils.Constants

class PostDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostDetailBinding
    private val repository = FirestoreRepository()
    private var postId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getStringExtra("post_id")
        fetchPostDetails()
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun fetchPostDetails() {
        val id = postId ?: return
        repository.db.collection(Constants.POSTS).document(id)
            .get()
            .addOnSuccessListener { snapshot ->
                val post = snapshot.toObject(Post::class.java) ?: return@addOnSuccessListener
                updateUI(post)
            }
    }

    private fun updateUI(post: Post) {
        binding.tvTitle.text       = post.title
        binding.tvDescription.text = post.description
        binding.tvSkill.text       = post.skillRequired
        binding.tvHours.text       = "${post.timeRequired} Hours"
        binding.tvAuthor.text      = post.authorName

        // Skill label with fallback for old posts
        val skillLabel = when {
            post.authorSkill.isNotEmpty()   -> post.authorSkill
            post.skillRequired.isNotEmpty() -> post.skillRequired
            else -> "—"
        }
        binding.tvSkillLabel.text = skillLabel
        binding.tvTrust.text      = "⭐ ${String.format("%.1f", post.authorTrust)}"

        // Author name clickable → view their profile
        binding.tvAuthor.isClickable = true
        binding.tvAuthor.isFocusable = true
        binding.tvAuthor.setOnClickListener {
            startActivity(Intent(this, UserProfileViewActivity::class.java).apply {
                putExtra("user_id", post.userId)
            })
        }

        val currentUid = FirebaseClient.currentUserId ?: ""
        if (currentUid == post.userId) {
            binding.btnOfferSwap.visibility = View.GONE
            binding.btnDelete.visibility    = View.VISIBLE
            binding.btnDelete.setOnClickListener { confirmDelete() }
        } else {
            binding.btnOfferSwap.visibility = View.VISIBLE
            binding.btnDelete.visibility    = View.GONE
            binding.btnOfferSwap.setOnClickListener { createSwapRequest(post) }
        }
    }

    private fun createSwapRequest(post: Post) {
        val requesterId = FirebaseClient.currentUserId ?: return
        binding.btnOfferSwap.isEnabled = false

        repository.db.collection(Constants.SWAPS)
            .get()
            .addOnSuccessListener { result ->
                val existingDoc = result.documents.firstOrNull { doc ->
                    val rId    = doc.getString("requesterId") ?: ""
                    val oId    = doc.getString("ownerId") ?: ""
                    val status = doc.getString("status") ?: ""
                    ((rId == requesterId && oId == post.userId) ||
                            (rId == post.userId && oId == requesterId)) &&
                            status in listOf("Pending", "Accepted")
                }

                if (existingDoc != null) {
                    val existingStatus = existingDoc.getString("status") ?: ""
                    val existingRId    = existingDoc.getString("requesterId") ?: ""

                    if (existingStatus == "Accepted" && existingRId != requesterId) {
                        repository.getUser(requesterId)
                            .addOnSuccessListener { userSnapshot ->
                                val requesterName = userSnapshot.getString("name") ?: "Someone"
                                val newSwap = Swap(
                                    postId        = post.id,
                                    postTitle     = post.title,
                                    requesterId   = requesterId,
                                    requesterName = requesterName,
                                    ownerId       = post.userId,
                                    ownerName     = post.authorName,
                                    status        = "Pending"
                                )
                                repository.createSwap(newSwap)
                                    .addOnSuccessListener { docRef ->
                                        val newSwapId = docRef.id
                                        docRef.update("id", newSwapId)
                                        sendNotification(
                                            postOwnerId   = post.userId,
                                            requesterName = requesterName,
                                            postTitle     = post.title,
                                            postId        = post.id,
                                            swapId        = newSwapId
                                        )
                                        showOfferSentDialog()
                                    }
                                    .addOnFailureListener {
                                        binding.btnOfferSwap.isEnabled = true
                                        Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                binding.btnOfferSwap.isEnabled = true
                            }
                    } else {
                        // Already has active swap — just open chat
                        startActivity(Intent(this, ChatActivity::class.java).apply {
                            putExtra("swap_id", existingDoc.id)
                        })
                        finish()
                    }
                    return@addOnSuccessListener
                }

                // No existing swap — create new one
                repository.getUser(requesterId)
                    .addOnSuccessListener { userSnapshot ->
                        val requesterName = userSnapshot.getString("name") ?: "Someone"
                        val swap = Swap(
                            postId        = post.id,
                            postTitle     = post.title,
                            requesterId   = requesterId,
                            requesterName = requesterName,
                            ownerId       = post.userId,
                            ownerName     = post.authorName,
                            status        = "Pending"
                        )
                        repository.createSwap(swap)
                            .addOnSuccessListener { docRef ->
                                val swapId = docRef.id
                                docRef.update("id", swapId)
                                sendNotification(
                                    postOwnerId   = post.userId,
                                    requesterName = requesterName,
                                    postTitle     = post.title,
                                    postId        = post.id,
                                    swapId        = swapId
                                )
                                showOfferSentDialog()
                            }
                            .addOnFailureListener {
                                binding.btnOfferSwap.isEnabled = true
                                Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        binding.btnOfferSwap.isEnabled = true
                    }
            }
            .addOnFailureListener {
                binding.btnOfferSwap.isEnabled = true
                Toast.makeText(this, "Failed to check swaps: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ── Show popup after offer sent — no chat navigation ─────────────────────
    private fun showOfferSentDialog() {
        AlertDialog.Builder(this)
            .setTitle("🎉 Offer Sent!")
            .setMessage("Your swap offer has been sent. You'll be notified when they respond.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish() // go back to dashboard
            }
            .setCancelable(false)
            .show()
    }

    private fun sendNotification(
        postOwnerId: String,
        requesterName: String,
        postTitle: String,
        postId: String,
        swapId: String
    ) {
        val notifRef = repository.db.collection(Constants.NOTIFICATIONS).document()
        val notification = Notification(
            id      = notifRef.id,
            userId  = postOwnerId,
            message = "$requesterName wants to swap skills for: \"$postTitle\"",
            postId  = postId,
            swapId  = swapId,
            read    = false
        )
        notifRef.set(notification).addOnSuccessListener {
            Log.d("NOTIF", "Notification sent to $postOwnerId")
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Yes") { _, _ ->
                postId?.let { id ->
                    repository.deletePost(id).addOnSuccessListener {
                        Toast.makeText(this, "Post Deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}