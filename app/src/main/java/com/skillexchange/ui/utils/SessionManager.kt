package com.skillexchange.ui.utils

import com.skillexchange.firebase.FirebaseClient

object SessionManager {
    fun getUid(): String? {
        return FirebaseClient.currentUserId
    }

    fun isLoggedIn(): Boolean {
        return FirebaseClient.auth.currentUser != null
    }

    fun logout() {
        FirebaseClient.auth.signOut()
    }
}
