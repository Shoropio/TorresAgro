package com.torresagro.app.data.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.torresagro.app.BuildConfig

object FirebaseBootstrap {
    fun initializeIfPossible(context: Context): Boolean {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) return true
        } catch (e: Exception) { }

        if (!isConfigured()) {
            android.util.Log.w("FirebaseBootstrap", "Firebase no configurado en local.properties, esperando google-services.json")
            return try {
                FirebaseApp.getInstance()
                true
            } catch (e: Exception) {
                false
            }
        }

        val options = FirebaseOptions.Builder()
            .setApiKey(BuildConfig.FIREBASE_API_KEY)
            .setApplicationId(BuildConfig.FIREBASE_APP_ID)
            .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
            .setStorageBucket(BuildConfig.FIREBASE_STORAGE_BUCKET)
            .setGcmSenderId(BuildConfig.FIREBASE_GCM_SENDER_ID)
            .build()

        FirebaseApp.initializeApp(context, options)
        return true
    }

    fun isConfigured(): Boolean {
        return BuildConfig.FIREBASE_API_KEY.isNotBlank() &&
            BuildConfig.FIREBASE_APP_ID.isNotBlank() &&
            BuildConfig.FIREBASE_PROJECT_ID.isNotBlank()
    }
}
