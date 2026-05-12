package com.skillexchange.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skillexchange.databinding.ItemMessageMeBinding
import com.skillexchange.databinding.ItemMessageOtherBinding
import com.skillexchange.models.Message

class ChatAdapter(private val currentUserId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages = listOf<Message>()

    companion object {
        const val VIEW_TYPE_ME = 1
        const val VIEW_TYPE_OTHER = 2
    }

    fun submitList(newData: List<Message>) {
        messages = newData
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            messages[position].senderId == "system" -> VIEW_TYPE_OTHER  // center or style differently
            messages[position].senderId == currentUserId -> VIEW_TYPE_ME
            else -> VIEW_TYPE_OTHER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ME) {
            MeViewHolder(
                ItemMessageMeBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        } else {
            OtherViewHolder(
                ItemMessageOtherBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is MeViewHolder -> holder.bind(message)
            is OtherViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size

    inner class MeViewHolder(private val binding: ItemMessageMeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.tvMessage.text = message.text
        }
    }

    inner class OtherViewHolder(private val binding: ItemMessageOtherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.tvMessage.text = message.text
        }
    }
}