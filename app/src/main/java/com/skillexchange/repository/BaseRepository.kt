package com.skillexchange.repository

import com.google.firebase.firestore.FirebaseFirestore

abstract class BaseRepository {
    internal val db = FirebaseFirestore.getInstance()
}