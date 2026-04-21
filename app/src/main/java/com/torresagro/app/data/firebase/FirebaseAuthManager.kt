package com.torresagro.app.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    suspend fun signInWithGoogleIdToken(idToken: String): String? {
        val a = auth ?: return null
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return a.signInWithCredential(credential).await().user?.uid
    }

    fun uidFlow(): Flow<String?> = callbackFlow {
        val a = auth
        if (a == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }
        a.addAuthStateListener(listener)
        trySend(a.currentUser?.uid)

        awaitClose {
            a.removeAuthStateListener(listener)
        }
    }
    
    fun signOut() {
        auth?.signOut()
    }
}
