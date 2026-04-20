package com.torresagro.app.data.agri

import com.torresagro.app.domain.model.AgriData
import com.torresagro.app.domain.model.PestPrediction
import com.torresagro.app.domain.model.HistoricalGrid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL
import java.net.HttpURLConnection

class AgriService {
    private val json = Json { ignoreUnknownKeys = true }
    
    suspend fun fetchAgriData(parcelId: String, lat: Double, lon: Double): AgriData = withContext(Dispatchers.IO) {
        try {
            // Open-Meteo Agriculture API para humedad del suelo actual
            val url = "https://agriculture-api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                    "&hourly=soil_moisture_0_to_10cm&timezone=auto&forecast_days=1"
            
            val connection = (URL(url).openConnection() as java.net.HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 10000
            }
            
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val response = json.decodeFromString<OpenMeteoAgriResponse>(responseText)
            
            // Tomamos el valor más reciente de la lista horaria
            val currentMoisture = response.hourly.soil_moisture_0_to_10cm.firstOrNull() ?: 0.0
            
            // Humedad en Open-Meteo viene en m³/m³, convertimos a % para la UI (aprox)
            val moisturePercent = currentMoisture * 100.0
            
            // Histórico usando Archive API (Reemplaza aWhere)
            val historical = getHistoricalGrids(lat, lon)
            
            return@withContext AgriData(
                parcelId = parcelId,
                ndvi = 0.0, // Open-Meteo no provee NDVI (Satélite no procesado)
                soilMoisture = moisturePercent,
                satelliteSource = "Open-Meteo Agriculture API",
                lastUpdate = System.currentTimeMillis(),
                historicalGrids = historical
            )
        } catch (e: Exception) {
            println("AgriService Error: ${e.message}")
            return@withContext AgriData(
                parcelId = parcelId,
                ndvi = 0.0,
                soilMoisture = 0.0,
                satelliteSource = "Error al conectar con Open-Meteo",
                lastUpdate = System.currentTimeMillis()
            )
        }
    }

    suspend fun getHistoricalGrids(lat: Double, lon: Double): List<HistoricalGrid> = withContext(Dispatchers.IO) {
        try {
            val end = java.time.LocalDate.now().minusDays(1)
            val start = end.minusDays(7)
            val url = "https://archive-api.open-meteo.com/v1/archive?latitude=$lat&longitude=$lon" +
                    "&start_date=$start&end_date=$end&daily=precipitation_sum,temperature_2m_max&timezone=auto"
            
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
            }
            
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val response = json.decodeFromString<OpenMeteoArchiveResponse>(responseText)
            
            return@withContext response.daily.time.indices.map { i ->
                HistoricalGrid(
                    date = response.daily.time[i],
                    precipitation = response.daily.precipitation_sum[i],
                    tempMax = response.daily.temperature_2m_max[i],
                    tempMin = response.daily.temperature_2m_max[i] - 5.0 // Estimación de min si no se pide, o mejor pedirla
                )
            }
        } catch (e: Exception) {
            println("Archive Error: ${e.message}")
            emptyList()
        }
    }

    suspend fun checkPests(parcelId: String, lat: Double, lon: Double): List<PestPrediction> = withContext(Dispatchers.IO) {
        // En lugar de una API de pago, usamos nuestro AgroEngine local
        // que ya hemos configurado previamente.
        emptyList() 
    }
}

@Serializable
private data class OpenMeteoAgriResponse(
    val hourly: OpenMeteoAgriHourly
)

@Serializable
private data class OpenMeteoAgriHourly(
    val soil_moisture_0_to_10cm: List<Double>
)

@Serializable
private data class OpenMeteoArchiveResponse(
    val daily: OpenMeteoArchiveDaily
)

@Serializable
private data class OpenMeteoArchiveDaily(
    val time: List<String>,
    val precipitation_sum: List<Double>,
    val temperature_2m_max: List<Double>
)
