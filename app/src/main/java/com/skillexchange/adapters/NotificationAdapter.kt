package com.skillexchange.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skillexchange.databinding.ItemNotificationBinding
import com.skillexchange.models.Notification

class NotificationAdapter(
    private val onClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private var notifications = listOf<Notification>()

    fun submitList(newData: List<Notification>) {
        notifications = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(notifications[position])

    override fun getItemCount() = notifications.size

    inner class ViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            binding.tvMessage.text = notification.message

            // Dim read notifications slightly
            binding.root.alpha = if (notification.read) 0.6f else 1.0f

            // Open chat on tap
            binding.root.setOnClickListener { onClick(notification) }
        }
    }
}