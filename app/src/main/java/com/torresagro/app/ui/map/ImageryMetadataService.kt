package com.torresagro.app.ui.map

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object ImageryMetadataService {
    private val openAerialMapCache = ConcurrentHashMap<String, OpenAerialMapLayer>()
    private val openAerialMapMisses = ConcurrentHashMap.newKeySet<String>()
    private val esriMetadataCache = ConcurrentHashMap<String, ImageryMetadata>()
    private val esriMetadataMisses = ConcurrentHashMap.newKeySet<String>()

    suspend fun findOpenAerialMapLayer(latitude: Double, longitude: Double): OpenAerialMapLayer? =
        withContext(Dispatchers.IO) {
            val cacheKey = cacheKey(latitude, longitude)
            if (openAerialMapCache.containsKey(cacheKey)) return@withContext openAerialMapCache[cacheKey]
            if (openAerialMapMisses.contains(cacheKey)) return@withContext null
            runCatching {
                val delta = 0.03
                val bbox = listOf(
                    longitude - delta,
                    latitude - delta,
                    longitude + delta,
                    latitude + delta
                ).joinToString(",") { String.format(Locale.US, "%.6f", it) }
                val json = getJson("https://api.openaerialmap.org/meta?bbox=$bbox&limit=10")
                val results = json.optJSONArray("results") ?: return@withContext null
                val candidates = (0 until results.length())
                    .mapNotNull { index -> results.optJSONObject(index) }
                    .filter { item ->
                        val bboxArray = item.optJSONArray("bbox")
                        val props = item.optJSONObject("properties")
                        props?.optString("tms").orEmpty().isNotBlank() &&
                            bboxArray != null &&
                            longitude >= bboxArray.optDouble(0) &&
                            latitude >= bboxArray.optDouble(1) &&
                            longitude <= bboxArray.optDouble(2) &&
                            latitude <= bboxArray.optDouble(3)
                    }
                    .sortedWith(
                        compareByDescending<JSONObject> { parseInstantMillis(it.optString("acquisition_start")) ?: 0L }
                            .thenBy { it.optDouble("gsd", Double.MAX_VALUE) }
                    )

                val selected = candidates.firstOrNull() ?: return@withContext null
                val props = selected.optJSONObject("properties")
                val tms = props?.optString("tms").orEmpty()
                OpenAerialMapLayer(
                    tileTemplate = tms,
                    metadata = ImageryMetadata(
                        source = "OpenAerialMap",
                        provider = selected.optString("provider").takeIf { it.isNotBlank() },
                        date = formatIsoDate(selected.optString("acquisition_start")),
                        resolution = selected.optDoubleOrNull("gsd")
                            ?.let { String.format(Locale.US, "%.2f m", it) }
                            ?: props?.optDoubleOrNull("resolution_in_meters")
                                ?.let { String.format(Locale.US, "%.2f m", it) },
                        title = selected.optString("title").takeIf { it.isNotBlank() },
                        license = props?.optString("license").takeIf { !it.isNullOrBlank() }
                    )
                )
            }.getOrNull().also {
                if (it != null) openAerialMapCache[cacheKey] = it else openAerialMapMisses.add(cacheKey)
            }
        }

    suspend fun findEsriMetadata(latitude: Double, longitude: Double): ImageryMetadata? =
        withContext(Dispatchers.IO) {
            val cacheKey = cacheKey(latitude, longitude)
            if (esriMetadataCache.containsKey(cacheKey)) return@withContext esriMetadataCache[cacheKey]
            if (esriMetadataMisses.contains(cacheKey)) return@withContext null
            runCatching {
                val geometry = URLEncoder.encode(
                    String.format(Locale.US, "%.6f,%.6f", longitude, latitude),
                    StandardCharsets.UTF_8.name()
                )
                val url = "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/0/query" +
                    "?f=json&geometry=$geometry&geometryType=esriGeometryPoint&inSR=4326" +
                    "&spatialRel=esriSpatialRelIntersects&outFields=*&returnGeometry=false"
                val json = getJson(url)
                val attributes = json.optJSONArray("features")
                    ?.optJSONObject(0)
                    ?.optJSONObject("attributes")
                    ?: return@withContext null
                ImageryMetadata(
                    source = "Esri World Imagery",
                    provider = attributes.optString("NICE_DESC").takeIf { it.isNotBlank() },
                    date = formatYyyyMmDd(attributes.optString("SRC_DATE")),
                    resolution = attributes.optDoubleOrNull("SRC_RES")?.let {
                        String.format(Locale.US, "%.2f m", it)
                    },
                    title = attributes.optString("NICE_NAME").takeIf { it.isNotBlank() },
                    license = attributes.optString("ReleaseName").takeIf { it.isNotBlank() }
                )
            }.getOrNull().also {
                if (it != null) esriMetadataCache[cacheKey] = it else esriMetadataMisses.add(cacheKey)
            }
        }

    private fun getJson(url: String): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 4000
        connection.readTimeout = 6000
        connection.setRequestProperty("User-Agent", "TorresAgro")
        return try {
            connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
        } finally {
            connection.disconnect()
        }
    }

    private fun cacheKey(latitude: Double, longitude: Double): String =
        String.format(Locale.US, "%.4f,%.4f", latitude, longitude)

    private fun JSONObject.optDoubleOrNull(name: String): Double? =
        if (has(name) && !isNull(name)) optDouble(name).takeIf { !it.isNaN() } else null

    private fun parseInstantMillis(value: String): Long? =
        runCatching { Instant.parse(value).toEpochMilli() }.getOrNull()

    private fun formatIsoDate(value: String): String? =
        runCatching {
            Instant.parse(value).atZone(ZoneOffset.UTC).toLocalDate().toString()
        }.getOrNull()

    private fun formatYyyyMmDd(value: String): String? {
        if (value.length != 8) return null
        return runCatching {
            DateTimeFormatter.BASIC_ISO_DATE.parse(value)
            "${value.substring(0, 4)}-${value.substring(4, 6)}-${value.substring(6, 8)}"
        }.getOrNull()
    }
}
