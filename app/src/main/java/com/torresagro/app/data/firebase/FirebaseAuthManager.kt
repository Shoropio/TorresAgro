package com.torresagro.app.data.firebase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun ensureSignedIn(): String? {
        val current = auth.currentUser
        if (current != null) return current.uid
        return auth.signInAnonymously().await().user?.uid
    }

    fun currentUid(): String? = auth.currentUser?.uid
}
