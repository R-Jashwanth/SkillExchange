package com.skillexchange.ui.chat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillexchange.adapters.SwapChatAdapter
import com.skillexchange.databinding.ActivityChatsBinding
import com.skillexchange.models.Swap
import com.skillexchange.repository.FirestoreRepository
import com.skillexchange.ui.utils.SessionManager

class ChatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatsBinding
    private val repository = FirestoreRepository()
    private val allSwaps = mutableListOf<Swap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = SessionManager.getUid() ?: return

        val adapter = SwapChatAdapter { swap ->
            startActivity(
                Intent(this, ChatActivity::class.java).apply {
                    putExtra("swap_id", swap.id)
                }
            )
        }

        binding.rvChats.layoutManager = LinearLayoutManager(this)
        binding.rvChats.adapter = adapter
        binding.btnBack.setOnClickListener { finish() }

        repository.getMyChats(uid) { swaps ->
            allSwaps.removeAll { s -> swaps.any { it.id == s.id } }
            allSwaps.addAll(swaps)
            adapter.submitList(allSwaps.toList())
        }

        repository.getMyChatsAsOwner(uid) { swaps ->
            allSwaps.removeAll { s -> swaps.any { it.id == s.id } }
            allSwaps.addAll(swaps)
            adapter.submitList(allSwaps.toList())
        }
    }
}