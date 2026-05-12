package com.skillexchange.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillexchange.ui.utils.Constants
import com.skillexchange.adapters.PostAdapter
import com.skillexchange.databinding.ActivityHistoryBinding
import com.skillexchange.models.Post
import com.skillexchange.repository.FirestoreRepository
import com.skillexchange.ui.utils.SessionManager

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private val repository = FirestoreRepository()
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PostAdapter { /* Handle click */ }
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter

        fetchHistory()
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun fetchHistory() {
        val uid = SessionManager.getUid() ?: return
        repository.db.collection(Constants.SWAPS)
            .whereEqualTo("status", "Completed")
            .addSnapshotListener { snapshot, _ ->
                val swapPostIds = snapshot?.documents?.mapNotNull { it.getString("postId") } ?: listOf()
                if (swapPostIds.isNotEmpty()) {
                    repository.db.collection(Constants.POSTS)
                        .whereIn("id", swapPostIds)
                        .get().addOnSuccessListener { postsSnap ->
                            adapter.submitList(postsSnap.toObjects(Post::class.java))
                        }
                }
            }
    }
}
