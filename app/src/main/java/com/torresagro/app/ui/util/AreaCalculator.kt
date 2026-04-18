package com.torresagro.app.ui.util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.*

object AreaCalculator {
    private val json = Json
    private const val EARTH_RADIUS = 6371000.0 // Meters

    /**
     * Calculates the area of a polygon defined by lat/lon points using the spherical shoelace formula.
     * Returns the area in hectares.
     */
    fun calculateHectares(points: List<Pair<Double, Double>>): Double {
        if (points.size < 3) return 0.0
        
        var totalArea = 0.0
        for (i in points.indices) {
            val p1 = points[i]
            val p2 = points[(i + 1) % points.size]
            
            val lat1 = Math.toRadians(p1.first)
            val lon1 = Math.toRadians(p1.second)
            val lat2 = Math.toRadians(p2.first)
            val lon2 = Math.toRadians(p2.second)
            
            totalArea += (lon2 - lon1) * (2 + sin(lat1) + sin(lat2))
        }
        
        val areaSqMeters = abs(totalArea * EARTH_RADIUS * EARTH_RADIUS / 2.0)
        return areaSqMeters / 10000.0
    }

    fun serializePoints(points: List<Pair<Double, Double>>): String {
        return json.encodeToString(points)
    }

    fun deserializePoints(jsonString: String?): List<Pair<Double, Double>> {
        if (jsonString.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<Pair<Double, Double>>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
