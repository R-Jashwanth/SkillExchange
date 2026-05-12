package com.skillexchange.repository

import com.google.firebase.firestore.Query
import com.skillexchange.firebase.FirebaseClient
import com.skillexchange.models.*
import com.skillexchange.ui.utils.Constants
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.FieldValue

class FirestoreRepository : BaseRepository() {

    fun createPost(post: Post): Task<Void> {
        val docRef = db.collection(Constants.POSTS).document(post.id)
        return docRef.set(post)
    }

    fun getPosts(listener: (List<Post>) -> Unit) {
        db.collection(Constants.POSTS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    listener(snapshot.toObjects(Post::class.java))
                }
            }
    }

    fun deletePost(postId: String): Task<Void> {
        return db.collection(Constants.POSTS).document(postId).delete()
    }

    fun createSwap(swap: Swap): Task<DocumentReference> {
        return db.collection(Constants.SWAPS).add(swap)
    }

    fun updateSwapStatus(swapId: String, status: String): Task<Void> {
        return db.collection(Constants.SWAPS).document(swapId).update("status", status)
    }

    fun getNotifications(userId: String, listener: (List<Notification>) -> Unit) {
        db.collection(Constants.NOTIFICATIONS)
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    listener(snapshot.toObjects(Notification::class.java))
                }
            }
    }

    fun sendNotification(notification: Notification): Task<Void> {
        val docRef = db.collection(Constants.NOTIFICATIONS).document()
        val finalNotification = notification.copy(id = docRef.id)
        return docRef.set(finalNotification)
    }

    fun createUser(user: User): Task<Void> {
        return db.collection(Constants.USERS).document(user.id).set(user)
    }

    fun getUser(userId: String): Task<DocumentSnapshot> {
        return db.collection(Constants.USERS).document(userId).get()
    }
    
    fun updateProfile(userId: String, name: String, skill: String): Task<Void> {
        return db.collection(Constants.USERS).document(userId).update("name", name, "skill", skill)
    }
    // Add to FirestoreRepository.kt

    fun sendMessage(message: Message): Task<Void> {
        val docRef = db.collection(Constants.CHATS)
            .document(message.swapId)
            .collection(Constants.MESSAGES)
            .document(message.id)
        return docRef.set(message)
    }

    fun getMessages(swapId: String, listener: (List<Message>) -> Unit) {
        db.collection(Constants.CHATS)
            .document(swapId)
            .collection(Constants.MESSAGES)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    listener(snapshot.toObjects(Message::class.java))
                }
            }
    }
    fun getMyChats(userId: String, listener: (List<Swap>) -> Unit) {
        // Get swaps where user is requester
        db.collection(Constants.SWAPS)
            .whereEqualTo("requesterId", userId)
            .whereEqualTo("status", "Accepted")
            .addSnapshotListener { snapshot, _ ->
                val swaps = snapshot?.toObjects(Swap::class.java) ?: emptyList()
                listener(swaps)
            }
    }

    fun getMyChatsAsOwner(userId: String, listener: (List<Swap>) -> Unit) {
        db.collection(Constants.SWAPS)
            .whereEqualTo("ownerId", userId)
            .whereEqualTo("status", "Accepted")
            .addSnapshotListener { snapshot, _ ->
                val swaps = snapshot?.toObjects(Swap::class.java) ?: emptyList()
                listener(swaps)
            }
    }

    fun incrementSwapsDone(userId: String): Task<Void> {
        return db.collection(Constants.USERS).document(userId)
            .update("swapsDone", FieldValue.increment(1))
    }
    fun incrementTrustScore(userId: String): Task<Void> {
        return db.collection(Constants.USERS).document(userId)
            .update("trustScore", FieldValue.increment(0.1))
    }


}
