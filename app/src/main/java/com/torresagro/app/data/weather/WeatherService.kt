package com.torresagro.app.data.weather

import com.torresagro.app.domain.model.WeatherSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class WeatherService {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchWeather(locationName: String, lat: Double? = null, lon: Double? = null): WeatherSnapshot = withContext(Dispatchers.IO) {
        val (finalLat, finalLon) = if (lat != null && lon != null) {
            lat to lon
        } else {
            val coords = geocode(locationName)
            coords.latitude to coords.longitude
        }

        val forecastUrl = buildString {
            append("https://api.open-meteo.com/v1/forecast?")
            append("latitude=$finalLat&longitude=$finalLon")
            append("&current=temperature_2m,relative_humidity_2m,rain")
        }
        
        val connection = openConnection(forecastUrl)
        val responseText = connection.readBodyOrThrow()
        val forecast = json.decodeFromString<ForecastResponse>(responseText)

        WeatherSnapshot(
            locationLabel = if (lat != null && lon != null) "GPS: ${"%.4f".format(lat)}, ${"%.4f".format(lon)}" else locationName,
            status = describeRain(forecast.current.rain),
            rainfallMm = forecast.current.rain.toInt(),
            temperatureC = forecast.current.temperature.toInt(),
            humidityPercent = forecast.current.humidity,
            online = true
        )
    }

    private suspend fun geocode(locationName: String): GeoResult = withContext(Dispatchers.IO) {
        if (locationName.isBlank()) return@withContext GeoResult(19.4326, -99.1332)
        val encoded = URLEncoder.encode(locationName, StandardCharsets.UTF_8)
        val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=1&language=es&format=json"

        val response = runCatching {
            val connection = openConnection(geoUrl)
            val text = connection.readBodyOrThrow()
            json.decodeFromString<GeoResponse>(text)
        }.getOrNull()
        response?.results?.firstOrNull() ?: GeoResult(19.4326, -99.1332)
    }

    private fun openConnection(url: String): java.net.HttpURLConnection {
        return (URL(url).openConnection() as java.net.HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("User-Agent", "TorresAgroApp/1.0")
        }
    }

    private fun java.net.HttpURLConnection.readBodyOrThrow(): String {
        val body = try {
            inputStream.bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
        }

        if (responseCode !in 200..299) {
            throw IllegalStateException("HTTP $responseCode: $body")
        }
        return body
    }

    private fun describeRain(rain: Double): String {
        return when {
            rain <= 0.0 -> "Sin lluvia esperada"
            rain < 3.0 -> "Lluvia ligera esperada"
            rain < 10.0 -> "Lluvia moderada esperada"
            else -> "Lluvia fuerte esperada"
        }
    }
}

@Serializable
private data class GeoResponse(
    val results: List<GeoResult> = emptyList()
)

@Serializable
data class GeoResult(
    val latitude: Double,
    val longitude: Double
)

@Serializable
private data class ForecastResponse(
    val current: CurrentWeather
)

@Serializable
private data class CurrentWeather(
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("relative_humidity_2m") val humidity: Int,
    val rain: Double
)
