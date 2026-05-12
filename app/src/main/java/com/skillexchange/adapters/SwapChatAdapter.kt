package com.skillexchange.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skillexchange.databinding.ItemSwapChatBinding
import com.skillexchange.models.Swap
import com.skillexchange.ui.utils.SessionManager

class SwapChatAdapter(
    private val onClick: (Swap) -> Unit
) : RecyclerView.Adapter<SwapChatAdapter.ViewHolder>() {

    private var swaps = listOf<Swap>()

    fun submitList(newData: List<Swap>) {
        swaps = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemSwapChatBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(swaps[position])

    override fun getItemCount() = swaps.size

    inner class ViewHolder(private val binding: ItemSwapChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(swap: Swap) {
            val currentUid = SessionManager.getUid() ?: ""

            // Show the OTHER person's name
            val otherPersonName = if (currentUid == swap.requesterId) {
                swap.ownerName      // I'm the requester, show owner
            } else {
                swap.requesterName  // I'm the owner, show requester
            }

            binding.tvPostTitle.text =
                if (otherPersonName.isNotEmpty()) otherPersonName else "Chat"
            binding.tvStatus.text = swap.postTitle  // show post title as subtitle
            binding.root.setOnClickListener { onClick(swap) }
        }
    }
}