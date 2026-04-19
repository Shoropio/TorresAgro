package com.torresagro.app.data.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {
    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun ensureSignedIn(): String? {
        val a = auth ?: return null
        val current = a.currentUser
        if (current != null) return current.uid
        return try {
            a.signInAnonymously().await().user?.uid
        } catch (e: Exception) {
            null
        }
    }

    fun currentUid(): String? = auth?.currentUser?.uid
    
    fun signOut() {
        auth?.signOut()
    }
}
