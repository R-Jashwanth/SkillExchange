package com.skillexchange.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skillexchange.R
import com.skillexchange.databinding.ItemPostBinding
import com.skillexchange.models.Post
import com.skillexchange.ui.home.UserProfileViewActivity

class PostAdapter(private val onClick: (Post) -> Unit) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var posts = listOf<Post>()

    fun submitList(newData: List<Post>) {
        posts = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(posts[position])

    override fun getItemCount() = posts.size

    inner class ViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.tvTitle.text       = post.title
            binding.tvSkill.text       = post.skillRequired
            binding.tvTime.text        = "${post.timeRequired} hrs"
            binding.tvDescription.text = post.description
            binding.tvAuthor.text      = post.authorName

            // Show trust score from the post document
            binding.tvTrust.text = "⭐ ${String.format("%.1f", post.authorTrust)} Trust"

            // Default avatar icon (no image upload in this version)
            binding.ivAvatar.setImageResource(R.drawable.fg_user)

            // Tap the whole card → open post detail
            binding.root.setOnClickListener { onClick(post) }

            // Tap the author row → open that user's public profile
            binding.tvAuthor.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, UserProfileViewActivity::class.java).apply {
                    putExtra("user_id", post.userId)
                }
                context.startActivity(intent)
            }
        }
    }
}