package com.torresagro.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parcels")
data class ParcelEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val locationName: String,
    val sizeHectares: Double,
    val cropType: String,
    val variety: String,
    val sowingDate: String,
    val expectedHarvestDate: String,
    val latitude: Double?,
    val longitude: Double?,
    val boundaryJson: String? = null,
    val offlinePendingSync: Boolean
)

@Entity(tableName = "crop_tasks")
data class CropTaskEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val parcelId: String,
    val title: String,
    val dueDate: String,
    val taskType: String,
    val completed: Boolean,
    val priority: String,
    val reminderEnabled: Boolean
)

@Entity(tableName = "activity_records")
data class ActivityRecordEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val parcelId: String,
    val activityType: String,
    val date: String,
    val cost: Double,
    val quantity: String,
    val notes: String,
    val photoUri: String?
)

@Entity(tableName = "crop_observations")
data class CropObservationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val parcelId: String,
    val date: String,
    val cropStage: String,
    val generalStatus: String,
    val symptoms: String,
    val recommendation: String,
    val photoUri: String?
)

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val category: String,
    val stock: Double,
    val unit: String,
    val minimumStock: Double,
    val offlinePendingSync: Boolean = false
)

@Entity(tableName = "harvest_records")
data class HarvestRecordEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val parcelId: String,
    val cropType: String,
    val harvestedKg: Double,
    val totalCost: Double,
    val estimatedIncome: Double
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val createdAt: String
)

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val parcelId: String? = null,
    val locationLabel: String,
    val status: String,
    val rainfallMm: Int,
    val temperatureC: Int,
    val humidityPercent: Int,
    val windSpeedKph: Double = 0.0,
    val forecastJson: String? = null,
    val online: Boolean,
    val updatedAtEpochMillis: Long
)

@Entity(tableName = "agri_data_cache")
data class AgriDataEntity(
    @PrimaryKey val parcelId: String,
    val userId: String,
    val ndvi: Double,
    val soilMoisture: Double,
    val pestJson: String? = null,
    val historicalJson: String? = null,
    val source: String,
    val lastUpdate: Long
)
