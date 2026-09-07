package com.torresagro.app.ui.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

suspend fun captureCurrentLocation(context: Context): Pair<Double, Double>? {
    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!hasFine && !hasCoarse) return null
    if (!isLocationEnabled(context)) return null

    val gmsStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
    if (gmsStatus == ConnectionResult.SUCCESS) {
        val client = LocationServices.getFusedLocationProviderClient(context)
        try {
            val lastLocation = withTimeoutOrNull(5_000L) { client.lastLocation.await() }
            if (lastLocation != null) {
                return lastLocation.latitude to lastLocation.longitude
            }

            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()
            val freshLocation = withTimeoutOrNull(10_000L) { client.getCurrentLocation(request, null).await() }
            if (freshLocation != null) {
                return freshLocation.latitude to freshLocation.longitude
            }
        } catch (e: Exception) {
            android.util.Log.w("LocationCapture", "FusedLocationProvider falló, usando LocationManager", e)
        }
    }

    return fallbackToLocationManager(context)
}

fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
    return runCatching {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }.getOrDefault(false)
}

private fun fallbackToLocationManager(context: Context): Pair<Double, Double>? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER
    )

    val bestLocation = providers
        .filter { provider -> runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false) }
        .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
        .maxByOrNull(Location::getTime)

    return bestLocation?.latitude?.let { lat ->
        bestLocation.longitude.let { lon -> lat to lon }
    }
}
