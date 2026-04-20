package com.torresagro.app.data.weather

import com.torresagro.app.R
import com.torresagro.app.BuildConfig
import com.torresagro.app.domain.model.DailyForecast
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
    
    // API Keys
    private var openWeatherApiKey: String = BuildConfig.OPEN_WEATHER_API_KEY
    private var visualCrossingApiKey: String = BuildConfig.VISUAL_CROSSING_API_KEY

    fun setApiKeys(owm: String, vc: String) {
        openWeatherApiKey = owm
        visualCrossingApiKey = vc
    }

    suspend fun fetchWeather(locationName: String, lat: Double? = null, lon: Double? = null): WeatherSnapshot = withContext(Dispatchers.IO) {
        val (finalLat, finalLon) = if (lat != null && lon != null) {
            lat to lon
        } else {
            val coords = geocode(locationName)
            coords.latitude to coords.longitude
        }

        // Intentar con Open-Meteo (Primario, sin API Key)
        try {
            val basicWeather = fetchFromOpenMeteo(finalLat, finalLon, locationName)
            
            // Si tenemos Visual Crossing, enriquecer con datos agrícolas
            if (visualCrossingApiKey.isNotEmpty()) {
                return@withContext enrichWithVisualCrossing(basicWeather, finalLat, finalLon)
            }
            
            return@withContext basicWeather
        } catch (e: Exception) {
            // Respaldo con OpenWeatherMap si hay API Key
            if (openWeatherApiKey.isNotEmpty()) {
                return@withContext fetchFromOpenWeatherMap(finalLat, finalLon, locationName)
            }
            throw e
        }
    }

    private suspend fun fetchFromOpenMeteo(lat: Double, lon: Double, locationName: String): WeatherSnapshot {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                "&current=temperature_2m,relative_humidity_2m,rain,wind_speed_10m" +
                "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,weather_code&timezone=auto&forecast_days=16"
        
        val connection = openConnection(url)
        val responseText = connection.readBodyOrThrow()
        val response = json.decodeFromString<OpenMeteoResponse>(responseText)

        val forecasts = response.daily.time.indices.map { i ->
            DailyForecast(
                date = response.daily.time[i],
                tempMax = response.daily.tempMax[i],
                tempMin = response.daily.tempMin[i],
                rainMm = response.daily.rainSum[i],
                conditionResId = weatherCodeToResId(response.daily.weatherCode[i])
            )
        }

        return WeatherSnapshot(
            locationLabel = if (locationName.isBlank()) "Coord: ${"%.4f".format(lat)}, ${"%.4f".format(lon)}" else locationName,
            statusResId = describeRainResId(response.current.rain),
            rainfallMm = response.current.rain.toInt(),
            temperatureC = response.current.temperature.toInt(),
            humidityPercent = response.current.humidity,
            windSpeedKph = response.current.windSpeed,
            forecast16Days = forecasts,
            online = true,
            source = "Open-Meteo",
            updatedAtEpochMillis = System.currentTimeMillis()
        )
    }

    private suspend fun enrichWithVisualCrossing(snapshot: WeatherSnapshot, lat: Double, lon: Double): WeatherSnapshot {
        if (visualCrossingApiKey.isBlank()) return snapshot
        
        try {
            val url = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/$lat,$lon" +
                    "?unitGroup=metric&elements=datetime,temp,precip,et0,soiltemp00&include=current&key=$visualCrossingApiKey&contentType=json"
            
            val connection = openConnection(url)
            val responseText = connection.readBodyOrThrow()
            val response = json.decodeFromString<VisualCrossingResponse>(responseText)
            
            val current = response.currentConditions
            return snapshot.copy(
                evapotranspiration = current.et0 ?: snapshot.evapotranspiration,
                soilTemperature = current.soiltemp00 ?: snapshot.soilTemperature,
                source = "${snapshot.source} + Visual Crossing"
            )
        } catch (e: Exception) {
            println("Visual Crossing Error: ${e.message}")
            return snapshot
        }
    }

    private suspend fun fetchFromOpenWeatherMap(lat: Double, lon: Double, locationName: String): WeatherSnapshot {
        if (openWeatherApiKey.isBlank()) {
            throw IllegalStateException("API Key de OpenWeatherMap no configurada")
        }
        
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=metric&lang=es&appid=$openWeatherApiKey"
        
        val connection = openConnection(url)
        val responseText = connection.readBodyOrThrow()
        val response = json.decodeFromString<OWMResponse>(responseText)
        
        return WeatherSnapshot(
            locationLabel = locationName.ifBlank { response.name },
            status = response.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
            statusResId = if (response.weather.isEmpty()) R.string.weather_clear else null,
            rainfallMm = (response.rain?.h1 ?: 0.0).toInt(),
            temperatureC = response.main.temp.toInt(),
            humidityPercent = response.main.humidity,
            windSpeedKph = response.wind.speed * 3.6, // m/s a km/h
            online = true,
            source = "OpenWeatherMap",
            updatedAtEpochMillis = System.currentTimeMillis()
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

    private fun describeRainResId(rain: Double): Int {
        return when {
            rain <= 0.0 -> R.string.weather_clear
            rain < 10.0 -> R.string.weather_rain
            else -> R.string.weather_rain 
        }
    }

    private fun weatherCodeToResId(code: Int): Int {
        return when (code) {
            0 -> R.string.weather_clear
            1, 2, 3 -> R.string.weather_partly_cloudy
            45, 48 -> R.string.weather_fog
            51, 53, 55 -> R.string.weather_drizzle
            61, 63, 65 -> R.string.weather_rain
            71, 73, 75 -> R.string.weather_snow
            80, 81, 82 -> R.string.weather_showers
            95, 96, 99 -> R.string.weather_thunderstorm
            else -> R.string.weather_varied
        }
    }
}

@Serializable
private data class GeoResponse(val results: List<GeoResult> = emptyList())

@Serializable
data class GeoResult(val latitude: Double, val longitude: Double)

@Serializable
private data class OpenMeteoResponse(
    val current: OpenMeteoCurrent,
    val daily: OpenMeteoDaily
)

@Serializable
private data class OpenMeteoCurrent(
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("relative_humidity_2m") val humidity: Int,
    @SerialName("wind_speed_10m") val windSpeed: Double,
    val rain: Double
)

@Serializable
private data class OpenMeteoDaily(
    val time: List<String>,
    @SerialName("temperature_2m_max") val tempMax: List<Double>,
    @SerialName("temperature_2m_min") val tempMin: List<Double>,
    @SerialName("precipitation_sum") val rainSum: List<Double>,
    @SerialName("weather_code") val weatherCode: List<Int>
)

@Serializable
private data class VisualCrossingResponse(
    val currentConditions: VisualCrossingCurrent
)

@Serializable
private data class VisualCrossingCurrent(
    val et0: Double? = null,
    val soiltemp00: Double? = null
)

@Serializable
private data class OWMResponse(
    val name: String,
    val main: OWMMain,
    val weather: List<OWMWeather>,
    val wind: OWMWind,
    val rain: OWMRain? = null
)

@Serializable
private data class OWMMain(
    val temp: Double,
    val humidity: Int
)

@Serializable
private data class OWMWeather(
    val description: String
)

@Serializable
private data class OWMWind(
    val speed: Double
)

@Serializable
private data class OWMRain(
    @SerialName("1h") val h1: Double? = null
)
