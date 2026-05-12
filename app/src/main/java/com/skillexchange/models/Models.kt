package com.skillexchange.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
import com.google.firebase.Timestamp

data class User(
    val id: String = "",
    val name: String = "",
    val skill: String = "",
    val email: String = "",
    val profilePic: String = "",
    val trustScore: Double = 0.0,
    val swapsDone: Int = 0
)

data class Post(
    val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorSkill: String = "",       // NEW: e.g. "Carpentry"
    val authorTrust: Double = 0.0,      // NEW: live trust score snapshot
    val authorAvatar: String = "",      // NEW: kept for future use
    val title: String = "",
    val description: String = "",
    val skillRequired: String = "",
    val timeRequired: Int = 0,
    val status: String = "Open",
    @ServerTimestamp val createdAt: Date? = null
)

data class Swap(
    val id: String = "",
    val postId: String = "",
    val postTitle: String = "",
    val requesterId: String = "",
    val requesterName: String = "",  // ← add
    val ownerId: String = "",
    val ownerName: String = "",      // ← add
    val status: String = "Pending",
    val requesterConfirmed: Boolean = false,
    val ownerConfirmed: Boolean = false
)


data class Message(
    val id: String = "",
    val swapId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()  // ← was Long, causes crash
)

data class Notification(
    val id: String = "",
    val userId: String = "",
    val message: String = "",
    val postId: String = "",
    val swapId: String = "",  // ← must exist
    val read: Boolean = false,
    val timestamp: Timestamp = Timestamp.now()  // ← also fix this
)