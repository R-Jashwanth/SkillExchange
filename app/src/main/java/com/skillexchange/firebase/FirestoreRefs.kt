package com.skillexchange.firebase

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreRefs {
    private val db = FirebaseFirestore.getInstance()
    
    val users = db.collection("users")
    val posts = db.collection("posts")
    val swaps = db.collection("swaps")
    val messages = db.collection("messages")
    val notifications = db.collection("notifications")
}
