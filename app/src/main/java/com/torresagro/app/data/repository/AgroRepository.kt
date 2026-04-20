package com.torresagro.app.data.repository

import com.torresagro.app.domain.model.AppUiState
import com.torresagro.app.domain.model.ActivityType
import com.torresagro.app.domain.model.CropType
import com.torresagro.app.domain.model.TaskType
import kotlinx.coroutines.flow.StateFlow

interface AgroRepository {
    val uiState: StateFlow<AppUiState>
    suspend fun completeTask(taskId: String)
    suspend fun addParcel(
        name: String,
        locationName: String,
        sizeHectares: Double,
        cropType: CropType,
        variety: String,
        sowingDate: String,
        latitude: Double?,
        longitude: Double?,
        boundary: List<Pair<Double, Double>> = emptyList()
    )

    suspend fun addActivity(
        parcelId: String,
        activityType: ActivityType,
        date: String,
        cost: Double,
        quantity: String,
        notes: String,
        photoUri: String?
    )
    suspend fun updateParcel(
        parcelId: String,
        name: String,
        locationName: String,
        sizeHectares: Double,
        cropType: CropType,
        variety: String,
        sowingDate: String,
        latitude: Double?,
        longitude: Double?,
        boundary: List<Pair<Double, Double>> = emptyList()
    )

    suspend fun deleteParcel(parcelId: String)

    suspend fun updateActivity(
        activityId: String,
        parcelId: String,
        activityType: ActivityType,
        date: String,
        cost: Double,
        quantity: String,
        notes: String,
        photoUri: String?
    )

    suspend fun deleteActivity(activityId: String)
    suspend fun addTask(
        parcelId: String,
        title: String,
        dueDate: String,
        taskType: TaskType,
        priority: String,
        reminderEnabled: Boolean
    )

    suspend fun updateTask(
        taskId: String,
        parcelId: String,
        title: String,
        dueDate: String,
        taskType: TaskType,
        priority: String,
        reminderEnabled: Boolean,
        completed: Boolean
    )

    suspend fun deleteTask(taskId: String)
    suspend fun refreshWeather(parcelId: String)
    suspend fun refreshWeatherForCoordinates(locationLabel: String, latitude: Double, longitude: Double)
    suspend fun addObservation(
        parcelId: String,
        date: String,
        cropStage: String,
        generalStatus: String,
        symptoms: List<String>,
        recommendation: String,
        photoUri: String?
    )

    suspend fun updateObservation(
        observationId: String,
        parcelId: String,
        date: String,
        cropStage: String,
        generalStatus: String,
        symptoms: List<String>,
        recommendation: String,
        photoUri: String?
    )

    suspend fun deleteObservation(observationId: String)
    suspend fun addInventoryItem(name: String, category: String, stock: Double, unit: String, minimumStock: Double)
    suspend fun updateInventoryItem(id: String, name: String, category: String, stock: Double, unit: String, minimumStock: Double)
    suspend fun deleteInventoryItem(id: String)
    suspend fun refreshSatelliteData(parcelId: String)
    suspend fun refreshPestPredictions(parcelId: String)
    suspend fun refreshHistoricalGrids(parcelId: String)
    suspend fun calculateAgroInsights(parcelId: String)
}
