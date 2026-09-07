package com.torresagro.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.torresagro.app.data.repository.AgroRepository
import com.torresagro.app.domain.model.ActivityType
import com.torresagro.app.domain.model.AppUiState
import com.torresagro.app.domain.model.CropType
import com.torresagro.app.domain.model.TaskType
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

class AppViewModel(
    private val repository: AgroRepository
) : ViewModel() {
    val uiState: StateFlow<AppUiState> = repository.uiState
    private val syncMutex = Mutex()
    private var lastSyncAtMillis: Long = 0L

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            repository.completeTask(taskId)
        }
    }

    fun addParcel(
        name: String,
        locationName: String,
        sizeHectares: Double,
        cropType: CropType,
        variety: String,
        sowingDate: String,
        latitude: Double?,
        longitude: Double?,
        boundary: List<Pair<Double, Double>>
    ) {
        viewModelScope.launch {
            repository.addParcel(name, locationName, sizeHectares, cropType, variety, sowingDate, latitude, longitude, boundary)
        }
    }

    fun addActivity(
        parcelId: String,
        activityType: ActivityType,
        date: String,
        cost: Double,
        quantity: String,
        notes: String,
        photoUri: String?
    ) {
        viewModelScope.launch {
            repository.addActivity(parcelId, activityType, date, cost, quantity, notes, photoUri)
        }
    }

    fun updateParcel(
        parcelId: String,
        name: String,
        locationName: String,
        sizeHectares: Double,
        cropType: CropType,
        variety: String,
        sowingDate: String,
        latitude: Double?,
        longitude: Double?,
        boundary: List<Pair<Double, Double>>
    ) {
        viewModelScope.launch {
            repository.updateParcel(parcelId, name, locationName, sizeHectares, cropType, variety, sowingDate, latitude, longitude, boundary)
        }
    }

    fun deleteParcel(parcelId: String) {
        viewModelScope.launch {
            repository.deleteParcel(parcelId)
        }
    }

    fun updateActivity(
        activityId: String,
        parcelId: String,
        activityType: ActivityType,
        date: String,
        cost: Double,
        quantity: String,
        notes: String,
        photoUri: String?
    ) {
        viewModelScope.launch {
            repository.updateActivity(activityId, parcelId, activityType, date, cost, quantity, notes, photoUri)
        }
    }

    fun deleteActivity(activityId: String) {
        viewModelScope.launch {
            repository.deleteActivity(activityId)
        }
    }

    fun addTask(
        parcelId: String,
        title: String,
        dueDate: String,
        taskType: TaskType,
        priority: String,
        reminderEnabled: Boolean
    ) {
        viewModelScope.launch {
            repository.addTask(parcelId, title, dueDate, taskType, priority, reminderEnabled)
        }
    }

    fun updateTask(
        taskId: String,
        parcelId: String,
        title: String,
        dueDate: String,
        taskType: TaskType,
        priority: String,
        reminderEnabled: Boolean,
        completed: Boolean
    ) {
        viewModelScope.launch {
            repository.updateTask(taskId, parcelId, title, dueDate, taskType, priority, reminderEnabled, completed)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun refreshWeather(parcelId: String) {
        viewModelScope.launch {
            repository.refreshWeather(parcelId)
        }
    }

    fun refreshWeatherForCoordinates(locationLabel: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            repository.refreshWeatherForCoordinates(locationLabel, latitude, longitude)
        }
    }

    fun refreshSatelliteData(parcelId: String) {
        viewModelScope.launch {
            repository.refreshSatelliteData(parcelId)
        }
    }

    fun addObservation(
        parcelId: String,
        date: String,
        cropStage: String,
        generalStatus: String,
        symptoms: List<String>,
        recommendation: String,
        photoUri: String?
    ) {
        viewModelScope.launch {
            repository.addObservation(parcelId, date, cropStage, generalStatus, symptoms, recommendation, photoUri)
        }
    }

    fun updateObservation(
        observationId: String,
        parcelId: String,
        date: String,
        cropStage: String,
        generalStatus: String,
        symptoms: List<String>,
        recommendation: String,
        photoUri: String?
    ) {
        viewModelScope.launch {
            repository.updateObservation(observationId, parcelId, date, cropStage, generalStatus, symptoms, recommendation, photoUri)
        }
    }

    fun deleteObservation(observationId: String) {
        viewModelScope.launch {
            repository.deleteObservation(observationId)
        }
    }

    fun addInventoryItem(name: String, category: String, stock: Double, unit: String, minimumStock: Double) {
        viewModelScope.launch {
            repository.addInventoryItem(name, category, stock, unit, minimumStock)
        }
    }

    fun updateInventoryItem(id: String, name: String, category: String, stock: Double, unit: String, minimumStock: Double) {
        viewModelScope.launch {
            repository.updateInventoryItem(id, name, category, stock, unit, minimumStock)
        }
    }

    fun deleteInventoryItem(id: String) {
        viewModelScope.launch {
            repository.deleteInventoryItem(id)
        }
    }

    fun sync() {
        viewModelScope.launch {
            syncMutex.withLock {
                val now = System.currentTimeMillis()
                if (now - lastSyncAtMillis < 15_000) return@withLock
                lastSyncAtMillis = now
                withTimeoutOrNull(30_000L) {
                    repository.pushPendingChanges()
                    repository.pullLatestData()
                }
            }
        }
    }
}

class AppViewModelFactory(
    private val repository: AgroRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        error("Unknown ViewModel class")
    }
}
