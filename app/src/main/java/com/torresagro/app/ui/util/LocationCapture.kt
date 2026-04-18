package com.torresagro.app.ui.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

suspend fun captureCurrentLocation(context: Context): Pair<Double, Double>? {
    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!hasFine && !hasCoarse) return null

    val client = LocationServices.getFusedLocationProviderClient(context)
    return try {
        val lastLocation = client.lastLocation.await()
        if (lastLocation != null) {
            lastLocation.latitude to lastLocation.longitude
        } else {
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()
            val freshLocation = client.getCurrentLocation(request, null).await()
            if (freshLocation != null) {
                freshLocation.latitude to freshLocation.longitude
            } else {
                null
            }
        }
    } catch (e: Exception) {
        null
    }
}
