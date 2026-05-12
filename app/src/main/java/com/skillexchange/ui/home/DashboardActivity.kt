package com.skillexchange.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillexchange.R
import com.skillexchange.adapters.PostAdapter
import com.skillexchange.databinding.ActivityDashboardBinding
import com.skillexchange.models.Post
import com.skillexchange.repository.FirestoreRepository
import com.skillexchange.ui.profile.ProfileActivity
import com.skillexchange.ui.utils.Constants
import com.skillexchange.ui.utils.SessionManager
import com.skillexchange.ui.chat.ChatsActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var adapter: PostAdapter
    private val repository = FirestoreRepository()

    private var selectedFilter = "All"
    private var allPosts: List<Post> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ✅ binding initialized FIRST before anything else
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFilters()
        fetchPosts()
        listenForUnreadNotifications()

        binding.btnCreatePost.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }
        binding.navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        // ✅ Single navChat click listener pointing to ChatsActivity
        binding.navChat.setOnClickListener {
            startActivity(Intent(this, ChatsActivity::class.java))
        }
        binding.btnNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
    }

    // ── LIVE UNREAD NOTIFICATION BADGE ──────────────────────────────────────
    private fun listenForUnreadNotifications() {
        val uid = SessionManager.getUid() ?: return

        repository.db.collection(Constants.NOTIFICATIONS)
            .whereEqualTo("userId", uid)
            .whereEqualTo("read", false)
            .addSnapshotListener { snapshot, _ ->
                val unreadCount = snapshot?.size() ?: 0
                if (unreadCount > 0) {
                    binding.tvNotificationBadge.visibility = View.VISIBLE
                    binding.tvNotificationBadge.text =
                        if (unreadCount > 9) "9+" else unreadCount.toString()
                } else {
                    binding.tvNotificationBadge.visibility = View.GONE
                }
            }
    }

    // ── RECYCLER VIEW ────────────────────────────────────────────────────────
    private fun setupRecyclerView() {
        adapter = PostAdapter { post ->
            startActivity(
                Intent(this, PostDetailActivity::class.java).apply {
                    putExtra("post_id", post.id)
                }
            )
        }
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = adapter
    }

    // ── CHIP FILTERS ─────────────────────────────────────────────────────────
    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                selectedFilter = when (checkedIds[0]) {
                    R.id.chipAll                  -> "All"
                    R.id.chipPlumbing             -> "Plumbing"
                    R.id.chipElectrical           -> "Electrical"
                    R.id.chipCarpentry            -> "Carpentry"
                    R.id.chipMasonry              -> "Masonry"
                    R.id.chipPainting             -> "Painting"
                    R.id.chipWelding              -> "Welding"
                    R.id.chipTiling               -> "Tiling"
                    R.id.chipRoofing              -> "Roofing"
                    R.id.chipInterior_Design      -> "Interior Design"
                    R.id.chipFurniture_Repair     -> "Furniture Repair"
                    R.id.chipCar_Mechanic         -> "Car Mechanic"
                    R.id.chipConstruction_Work    -> "Construction Work"
                    R.id.chipWater_Tank_Cleaning  -> "Water Tank Cleaning"
                    else                          -> "All"
                }
                applyFilter()
            }
        }
    }

    // ── FETCH & FILTER POSTS ─────────────────────────────────────────────────
    private fun fetchPosts() {
        binding.progressBar.visibility = View.VISIBLE
        repository.getPosts { posts ->
            binding.progressBar.visibility = View.GONE
            allPosts = posts
            applyFilter()
        }
    }

    private fun applyFilter() {
        adapter.submitList(
            if (selectedFilter == "All") allPosts
            else allPosts.filter {
                it.skillRequired.equals(selectedFilter, ignoreCase = true)
            }
        )
    }
}