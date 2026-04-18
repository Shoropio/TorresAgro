package com.torresagro.app.data.repository

import com.torresagro.app.ui.util.AreaCalculator
import com.torresagro.app.data.local.dao.AgroDao
import com.torresagro.app.data.local.util.TaskReminderScheduler
import com.torresagro.app.data.weather.WeatherService
import com.torresagro.app.domain.model.AgronomicTip
import com.torresagro.app.domain.model.AppUiState
import com.torresagro.app.domain.model.ActivityType
import com.torresagro.app.domain.model.CropCatalog
import com.torresagro.app.domain.model.CropType
import com.torresagro.app.domain.model.TaskType
import com.torresagro.app.domain.model.WeatherSnapshot
import com.torresagro.app.data.local.entity.ActivityRecordEntity
import com.torresagro.app.data.local.entity.CropObservationEntity
import com.torresagro.app.data.local.entity.CropTaskEntity
import com.torresagro.app.data.local.entity.ParcelEntity
import com.torresagro.app.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.util.UUID

class RoomAgroRepository(
    private val dao: AgroDao,
    private val syncGateway: SyncGateway? = null,
    private val reminderScheduler: TaskReminderScheduler? = null,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : AgroRepository {
    private val weatherService = WeatherService()
    private val weatherState = MutableStateFlow<WeatherSnapshot?>(null)

    suspend fun pushPendingChangesForStartup() {
        syncGateway?.pushPendingChanges()
    }

    suspend fun pullLatestDataForStartup() {
        syncGateway?.pullLatestData()
    }

    override val uiState: StateFlow<AppUiState> =
        combine(
            combine(
                dao.observeParcels(),
                dao.observeTasks(),
                dao.observeActivities()
            ) { parcels, tasks, activities ->
                Triple(parcels, tasks, activities)
            },
            combine(
                dao.observeObservations(),
                dao.observeInventory(),
                dao.observeHarvests()
            ) { observations, inventory, harvests ->
                Triple(observations, inventory, harvests)
            },
            weatherState
        ) { left, right, weather ->
            val (parcels, tasks, activities) = left
            val (observations, inventory, harvests) = right
            AppUiState(
                parcels = parcels.map { it.toDomain() },
                tasks = tasks.map { it.toDomain() },
                activities = activities.map { it.toDomain() },
                observations = observations.map { it.toDomain() },
                inventory = inventory.map { it.toDomain() },
                harvests = harvests.map { it.toDomain() },
                tips = buildTips(),
                weather = weather ?: WeatherSnapshot(
                    locationLabel = parcels.firstOrNull()?.locationName ?: "Sin ubicacion",
                    status = "Dato local guardado",
                    rainfallMm = 12,
                    temperatureC = 28,
                    humidityPercent = 80,
                    online = false
                )
            )
        }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), AppUiState())

    override suspend fun completeTask(taskId: String) {
        dao.markTaskCompleted(taskId)
        reminderScheduler?.cancel(taskId)
        syncGateway?.pushPendingChanges()
    }

    override suspend fun addTask(
        parcelId: String,
        title: String,
        dueDate: String,
        taskType: TaskType,
        priority: String,
        reminderEnabled: Boolean
    ) {
        val id = UUID.randomUUID().toString()
        dao.upsertTasks(
            listOf(
                CropTaskEntity(
                    id = id,
                    parcelId = parcelId,
                    title = title,
                    dueDate = dueDate,
                    taskType = taskType.name,
                    completed = false,
                    priority = priority,
                    reminderEnabled = reminderEnabled
                )
            )
        )
        if (reminderEnabled) {
            reminderScheduler?.schedule(
                task = com.torresagro.app.domain.model.CropTask(
                    id = id,
                    parcelId = parcelId,
                    title = title,
                    dueDate = dueDate,
                    taskType = taskType,
                    completed = false,
                    priority = priority,
                    reminderEnabled = true
                ),
                parcelName = uiState.value.parcels.firstOrNull { it.id == parcelId }?.name.orEmpty()
            )
        }
        dao.enqueueSync(listOf(SyncQueueEntity(UUID.randomUUID().toString(), "task", id, "UPSERT", "${dueDate}T08:00:00")))
        syncGateway?.pushPendingChanges()
    }

    override suspend fun addParcel(
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
        val id = UUID.randomUUID().toString()
        val expectedHarvestDate = LocalDate.parse(sowingDate).plusDays(cropType.cycleDays.toLong()).toString()
        dao.upsertParcels(
            listOf(
                ParcelEntity(
                    id = id,
                    name = name,
                    locationName = locationName,
                    sizeHectares = sizeHectares,
                    cropType = cropType.name,
                    variety = variety,
                    sowingDate = sowingDate,
                    expectedHarvestDate = expectedHarvestDate,
                    latitude = latitude,
                    longitude = longitude,
                    boundaryJson = AreaCalculator.serializePoints(boundary),
                    offlinePendingSync = true
                )
            )
        )
        dao.enqueueSync(listOf(SyncQueueEntity(UUID.randomUUID().toString(), "parcel", id, "UPSERT", "${sowingDate}T08:00:00")))
        syncGateway?.pushPendingChanges()
    }

    override suspend fun addActivity(
        parcelId: String,
        activityType: ActivityType,
        date: String,
        cost: Double,
        quantity: String,
        notes: String,
        photoUri: String?
    ) {
        val id = UUID.randomUUID().toString()
        dao.upsertActivities(
            listOf(
                ActivityRecordEntity(
                    id = id,
                    parcelId = parcelId,
                    activityType = activityType.name,
                    date = date,
                    cost = cost,
                    quantity = quantity,
                    notes = notes,
                    photoUri = photoUri
                )
            )
        )
        dao.enqueueSync(listOf(SyncQueueEntity(UUID.randomUUID().toString(), "activity", id, "UPSERT", "${date}T08:00:00")))
        syncGateway?.pushPendingChanges()
    }

    override suspend fun updateParcel(
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
        val expectedHarvestDate = LocalDate.parse(sowingDate).plusDays(cropType.cycleDays.toLong()).toString()
        dao.upsertParcels(
            listOf(
                ParcelEntity(
                    id = parcelId,
                    name = name,
                    locationName = locationName,
                    sizeHectares = sizeHectares,
                    cropType = cropType.name,
                    variety = variety,
                    sowingDate = sowingDate,
                    expectedHarvestDate = expectedHarvestDate,
                    latitude = latitude,
                    longitude = longitude,
                    boundaryJson = AreaCalculator.serializePoints(boundary),
                    offlinePendingSync = true
                )
            )
        )
        dao.enqueueSync(listOf(SyncQueueEntity(UUID.randomUUID().toString(), "parcel", parcelId, "UPSERT", "${sowingDate}T08:00:00")))
        syncGateway?.pushPendingChanges()
    }

    override suspend fun deleteParcel(parcelId: String) {
        uiState.value.tasks.filter { it.parcelId == parcelId }.forEach { task ->
            reminderScheduler?.cancel(task.id)
        }
        dao.deleteActivitiesByParcel(parcelId)
        dao.deleteObservationsByParcel(parcelId)
        dao.deleteTasksByParcel(parcelId)
        dao.deleteParcel(parcelId)
        dao.enqueueSync(listOf(SyncQueueEntity(UUID.randomUUID().toString(), "parcel", parcelId, "DELETE", "2026-04-17T08:00:00")))
        syncGateway?.pushPendingChanges()
    }

    override suspend fun updateActivity(
        activityId: String,
        parcelId: String,
        activityType: ActivityType,
        date: String,
        cost: Double,
        quantity: String,
        notes: String,
        photoUri: String?
    ) {
        dao.upsertActivities(
            listOf(
                ActivityRecordEntity(
                    id = activityId,
                    parcelId = parcelId,
                    activityType = activityType.name,
                    date = date,
                    cost = cost,
                    quantity = quantity,
                    notes = notes,
                    photoUri = photoUri
                )
            )
        )
        dao.enqueueSync(listOf(SyncQueueEntity(UUID.randomUUID().toString(), "activity", activityId, "UPSERT", "${date}T08:00:00")))
        syncGateway?.pushPendingChanges()
    }

    override suspend fun deleteActivity(activityId: String) {
        dao.deleteActivity(activityId)
        dao.enqueueSync(listOf(SyncQueueEntity(UUID.randomUUID().toString(), "activity", activityId, "DELETE", "2026-04-17T08:00:00")))
        syncGateway?.pushPendingChanges()
    }

    override suspend fun updateTask(
        taskId: String,
        parcelId: String,
        title: String,
        dueDate: String,
        taskType: TaskType,
        priority: String,
        reminderEnabled: Boolean,
        completed: Boolean
    ) {
        dao.upsertTasks(
            listOf(
                CropTaskEntity(
                    id = taskId,
                    parcelId = parcelId,
                    title = title,
                    dueDate = dueDate,
                    taskType = taskType.name,
                    completed = completed,
                    priority = priority,
                    reminderEnabled = reminderEnabled
                )
            )
        )
        if (reminderEnabled && !completed) {
            reminderScheduler?.schedule(
                task = com.torresagro.app.domain.model.CropTask(
                    id = taskId,
                    parcelId = parcelId,
                    title = title,
                    dueDate = dueDate,
                    taskType = taskType,
                    completed = completed,
                    priority = priority,
                    reminderEnabled = reminderEnabled
                ),
                parcelName = uiState.value.parcels.firstOrNull { it.id == parcelId }?.name.orEmpty()
            )
        } else {
            reminderScheduler?.cancel(taskId)
        }
        dao.enqueueSync(listOf(SyncQueueEntity(UUID.randomUUID().toString(), "task", taskId, "UPSERT", "${dueDate}T08:00:00")))
        syncGateway?.pushPendingChanges()
    }

    override suspend fun deleteTask(taskId: String) {
        dao.deleteTask(taskId)
        reminderScheduler?.cancel(taskId)
        dao.enqueueSync(listOf(SyncQueueEntity(UUID.randomUUID().toString(), "task", taskId, "DELETE", "2026-04-17T08:00:00")))
        syncGateway?.pushPendingChanges()
    }

    override suspend fun refreshWeather(parcelId: String) {
        val parcel = uiState.value.parcels.firstOrNull { it.id == parcelId } ?: return
        weatherState.value = runCatching {
            weatherService.fetchWeather(parcel.locationName, parcel.latitude, parcel.longitude)
        }.getOrElse {
            WeatherSnapshot(
                locationLabel = parcel.locationName,
                status = "Sin internet: ultimo dato guardado",
                rainfallMm = weatherState.value?.rainfallMm ?: 12,
                temperatureC = weatherState.value?.temperatureC ?: 28,
                humidityPercent = weatherState.value?.humidityPercent ?: 80,
                online = false
            )
        }
    }

    override suspend fun addObservation(
        parcelId: String,
        date: String,
        cropStage: String,
        generalStatus: String,
        symptoms: List<String>,
        recommendation: String,
        photoUri: String?
    ) {
        val id = UUID.randomUUID().toString()
        dao.upsertObservations(
            listOf(
                CropObservationEntity(
                    id = id,
                    parcelId = parcelId,
                    date = date,
                    cropStage = cropStage,
                    generalStatus = generalStatus,
                    symptoms = symptoms.joinToString("|"),
                    recommendation = recommendation,
                    photoUri = photoUri
                )
            )
        )
        dao.enqueueSync(listOf(SyncQueueEntity(UUID.randomUUID().toString(), "observation", id, "UPSERT", "${date}T08:00:00")))
        syncGateway?.pushPendingChanges()
    }

    override suspend fun updateObservation(
        observationId: String,
        parcelId: String,
        date: String,
        cropStage: String,
        generalStatus: String,
        symptoms: List<String>,
        recommendation: String,
        photoUri: String?
    ) {
        dao.upsertObservations(
            listOf(
                CropObservationEntity(
                    id = observationId,
                    parcelId = parcelId,
                    date = date,
                    cropStage = cropStage,
                    generalStatus = generalStatus,
                    symptoms = symptoms.joinToString("|"),
                    recommendation = recommendation,
                    photoUri = photoUri
                )
            )
        )
        dao.enqueueSync(listOf(SyncQueueEntity(UUID.randomUUID().toString(), "observation", observationId, "UPSERT", "${date}T08:00:00")))
        syncGateway?.pushPendingChanges()
    }

    override suspend fun deleteObservation(observationId: String) {
        dao.deleteObservation(observationId)
        dao.enqueueSync(listOf(SyncQueueEntity(UUID.randomUUID().toString(), "observation", observationId, "DELETE", "2026-04-17T08:00:00")))
        syncGateway?.pushPendingChanges()
    }

    private fun buildTips(): List<AgronomicTip> {
        return CropCatalog.templates.flatMap { template ->
            template.recommendations.mapIndexed { index, tip ->
                AgronomicTip(
                    cropType = template.cropType,
                    stage = template.stages.getOrNull(index)?.taskType?.label ?: "Manejo general",
                    tip = tip
                )
            }
        }
    }
}
