package com.torresagro.app.data.repository

import com.torresagro.app.data.local.entity.ActivityRecordEntity
import com.torresagro.app.data.local.entity.AgriDataEntity
import com.torresagro.app.data.local.entity.CropObservationEntity
import com.torresagro.app.data.local.entity.CropTaskEntity
import com.torresagro.app.data.local.entity.HarvestRecordEntity
import com.torresagro.app.data.local.entity.InventoryItemEntity
import com.torresagro.app.data.local.entity.ParcelEntity
import com.torresagro.app.data.local.entity.WeatherCacheEntity
import com.torresagro.app.domain.model.ActivityRecord
import com.torresagro.app.domain.model.ActivityType
import com.torresagro.app.domain.model.AgriData
import com.torresagro.app.domain.model.CropObservation
import com.torresagro.app.domain.model.CropTask
import com.torresagro.app.domain.model.CropType
import com.torresagro.app.domain.model.DailyForecast
import com.torresagro.app.domain.model.HarvestSummary
import com.torresagro.app.domain.model.HistoricalGrid
import com.torresagro.app.domain.model.InventoryItem
import com.torresagro.app.domain.model.Parcel
import com.torresagro.app.domain.model.PestPrediction
import com.torresagro.app.domain.model.TaskType
import com.torresagro.app.domain.model.WeatherSnapshot
import com.torresagro.app.ui.util.AreaCalculator
import kotlinx.serialization.json.Json

private inline fun <reified T : Enum<T>> safeEnumOf(value: String): T =
    enumValues<T>().find { it.name == value } ?: enumValues<T>().first()

fun ParcelEntity.toDomain() = Parcel(
    id = id,
    name = name,
    locationName = locationName,
    sizeHectares = sizeHectares,
    cropType = safeEnumOf<CropType>(cropType),
    variety = variety,
    sowingDate = sowingDate,
    expectedHarvestDate = expectedHarvestDate,
    latitude = latitude,
    longitude = longitude,
    boundary = AreaCalculator.deserializePoints(boundaryJson),
    offlinePendingSync = offlinePendingSync
)

fun CropTaskEntity.toDomain() = CropTask(
    id = id,
    parcelId = parcelId,
    title = title,
    dueDate = dueDate,
    taskType = safeEnumOf<TaskType>(taskType),
    completed = completed,
    priority = priority,
    reminderEnabled = reminderEnabled
)

fun ActivityRecordEntity.toDomain() = ActivityRecord(
    id = id,
    parcelId = parcelId,
    activityType = safeEnumOf<ActivityType>(activityType),
    date = date,
    cost = cost,
    quantity = quantity,
    notes = notes,
    photoUri = photoUri
)

fun CropObservationEntity.toDomain() = CropObservation(
    id = id,
    parcelId = parcelId,
    date = date,
    cropStage = cropStage,
    generalStatus = generalStatus,
    symptoms = symptoms.split("|").map { it.trim() }.filter { it.isNotEmpty() },
    recommendation = recommendation,
    photoUri = photoUri
)

fun InventoryItemEntity.toDomain() = InventoryItem(
    id = id,
    name = name,
    category = category,
    stock = stock,
    unit = unit,
    minimumStock = minimumStock,
    offlinePendingSync = offlinePendingSync
)

fun HarvestRecordEntity.toDomain() = HarvestSummary(
    parcelId = parcelId,
    cropType = safeEnumOf<CropType>(cropType),
    harvestedKg = harvestedKg,
    totalCost = totalCost,
    estimatedIncome = estimatedIncome
)


private val jsonConv = Json { ignoreUnknownKeys = true }

fun WeatherCacheEntity.toDomain() = WeatherSnapshot(
    locationLabel = locationLabel,
    status = status,
    rainfallMm = rainfallMm,
    temperatureC = temperatureC,
    humidityPercent = humidityPercent,
    windSpeedKph = windSpeedKph,
    forecast16Days = forecastJson?.let { 
        runCatching { jsonConv.decodeFromString<List<DailyForecast>>(it) }.getOrDefault(emptyList())
    } ?: emptyList(),
    online = online,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun AgriDataEntity.toDomain() = AgriData(
    parcelId = parcelId,
    ndvi = ndvi,
    soilMoisture = soilMoisture,
    pestPredictions = pestJson?.let {
        runCatching { Json.decodeFromString<List<PestPrediction>>(it) }.getOrDefault(emptyList())
    } ?: emptyList(),
    historicalGrids = historicalJson?.let {
        runCatching { Json.decodeFromString<List<HistoricalGrid>>(it) }.getOrDefault(emptyList())
    } ?: emptyList(),
    satelliteSource = source,
    lastUpdate = lastUpdate
)
