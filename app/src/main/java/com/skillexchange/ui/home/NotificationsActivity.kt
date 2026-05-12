package com.skillexchange.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillexchange.adapters.NotificationAdapter
import com.skillexchange.databinding.ActivityNotificationsBinding
import com.skillexchange.repository.FirestoreRepository
import com.skillexchange.ui.chat.ChatActivity
import com.skillexchange.ui.utils.Constants
import com.skillexchange.ui.utils.SessionManager

class NotificationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationsBinding
    private val repository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = SessionManager.getUid() ?: return

        val adapter = NotificationAdapter { notification ->
            // Mark as read
            if (!notification.read && notification.id.isNotEmpty()) {
                repository.db.collection(Constants.NOTIFICATIONS)
                    .document(notification.id)
                    .update("read", true)
            }
            // Open chat if swapId exists
            if (notification.swapId.isNotEmpty()) {
                startActivity(
                    Intent(this, ChatActivity::class.java).apply {
                        putExtra("swap_id", notification.swapId)
                    }
                )
            }
        }

        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter

        repository.getNotifications(uid) { notifications ->
            adapter.submitList(notifications)
        }

        binding.btnBack.setOnClickListener { finish() }
    }
}