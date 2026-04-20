package com.torresagro.app.data.repository

import com.torresagro.app.domain.model.ActivityRecord
import com.torresagro.app.domain.model.ActivityType
import com.torresagro.app.domain.model.AgronomicTip
import com.torresagro.app.domain.model.AppUiState
import com.torresagro.app.domain.model.CropObservation
import com.torresagro.app.domain.model.CropTask
import com.torresagro.app.domain.model.CropType
import com.torresagro.app.domain.model.HarvestSummary
import com.torresagro.app.domain.model.InventoryItem
import com.torresagro.app.domain.model.Parcel
import com.torresagro.app.domain.model.TaskType
import com.torresagro.app.domain.model.WeatherSnapshot
import com.torresagro.app.data.weather.WeatherService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.util.UUID

class InMemoryAgroRepository : AgroRepository {
    private val state = MutableStateFlow(seedState())
    private val weatherService = WeatherService()
    override val uiState: StateFlow<AppUiState> = state.asStateFlow()

    override suspend fun completeTask(taskId: String) {
        state.update { current ->
            current.copy(
                tasks = current.tasks.map { task ->
                    if (task.id == taskId) task.copy(completed = true) else task
                }
            )
        }
    }

    override suspend fun addTask(
        parcelId: String,
        title: String,
        dueDate: String,
        taskType: TaskType,
        priority: String,
        reminderEnabled: Boolean
    ) {
        state.update { current ->
            current.copy(
                tasks = current.tasks + CropTask(
                    id = UUID.randomUUID().toString(),
                    parcelId = parcelId,
                    title = title,
                    dueDate = dueDate,
                    taskType = taskType,
                    completed = false,
                    priority = priority,
                    reminderEnabled = reminderEnabled
                )
            )
        }
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
        val expectedHarvestDate = LocalDate.parse(sowingDate).plusDays(cropType.cycleDays.toLong()).toString()
        state.update { current ->
            current.copy(
                parcels = current.parcels + Parcel(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    locationName = locationName,
                    sizeHectares = sizeHectares,
                    cropType = cropType,
                    variety = variety,
                    sowingDate = sowingDate,
                    expectedHarvestDate = expectedHarvestDate,
                    latitude = latitude,
                    longitude = longitude,
                    boundary = boundary,
                    offlinePendingSync = true
                )
            )
        }
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
        state.update { current ->
            current.copy(
                activities = current.activities + ActivityRecord(
                    id = UUID.randomUUID().toString(),
                    parcelId = parcelId,
                    activityType = activityType,
                    date = date,
                    cost = cost,
                    quantity = quantity,
                    notes = notes,
                    photoUri = photoUri
                )
            )
        }
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
        state.update { current ->
            current.copy(
                parcels = current.parcels.map { parcel ->
                    if (parcel.id == parcelId) {
                        parcel.copy(
                            name = name,
                            locationName = locationName,
                            sizeHectares = sizeHectares,
                            cropType = cropType,
                            variety = variety,
                            sowingDate = sowingDate,
                            expectedHarvestDate = expectedHarvestDate,
                            latitude = latitude,
                            longitude = longitude,
                            boundary = boundary,
                            offlinePendingSync = true
                        )
                    } else parcel
                }
            )
        }
    }

    override suspend fun deleteParcel(parcelId: String) {
        state.update { current ->
            current.copy(
                parcels = current.parcels.filterNot { it.id == parcelId },
                activities = current.activities.filterNot { it.parcelId == parcelId },
                observations = current.observations.filterNot { it.parcelId == parcelId },
                tasks = current.tasks.filterNot { it.parcelId == parcelId },
                harvests = current.harvests.filterNot { it.parcelId == parcelId }
            )
        }
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
        state.update { current ->
            current.copy(
                activities = current.activities.map { activity ->
                    if (activity.id == activityId) {
                        activity.copy(
                            parcelId = parcelId,
                            activityType = activityType,
                            date = date,
                            cost = cost,
                            quantity = quantity,
                            notes = notes,
                            photoUri = photoUri
                        )
                    } else activity
                }
            )
        }
    }

    override suspend fun deleteActivity(activityId: String) {
        state.update { current ->
            current.copy(activities = current.activities.filterNot { it.id == activityId })
        }
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
        state.update { current ->
            current.copy(
                tasks = current.tasks.map { task ->
                    if (task.id == taskId) {
                        task.copy(
                            parcelId = parcelId,
                            title = title,
                            dueDate = dueDate,
                            taskType = taskType,
                            priority = priority,
                            reminderEnabled = reminderEnabled,
                            completed = completed
                        )
                    } else task
                }
            )
        }
    }

    override suspend fun deleteTask(taskId: String) {
        state.update { current ->
            current.copy(tasks = current.tasks.filterNot { it.id == taskId })
        }
    }

    override suspend fun refreshWeather(parcelId: String) {
        val parcel = state.value.parcels.firstOrNull { it.id == parcelId } ?: return
        val updated = runCatching { weatherService.fetchWeather(parcel.locationName, parcel.latitude, parcel.longitude) }
            .getOrElse {
                WeatherSnapshot(
                    locationLabel = parcel.locationName,
                    status = "Sin internet: ultimo dato local",
                    rainfallMm = 18,
                    temperatureC = 29,
                    humidityPercent = 82,
                    windSpeedKph = 5.0,
                    evapotranspiration = null,
                    soilTemperature = null,
                    forecast16Days = emptyList(),
                    online = false,
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
        }
        state.update { current ->
            current.copy(parcelWeatherById = current.parcelWeatherById + (parcelId to updated))
        }
    }

    override suspend fun refreshWeatherForCoordinates(locationLabel: String, latitude: Double, longitude: Double) {
        val updated = runCatching { weatherService.fetchWeather(locationLabel, latitude, longitude) }
            .getOrElse {
                WeatherSnapshot(
                    locationLabel = locationLabel,
                    status = "Sin internet: ultimo dato local",
                    rainfallMm = 18,
                    temperatureC = 29,
                    humidityPercent = 82,
                    windSpeedKph = 5.0,
                    evapotranspiration = null,
                    soilTemperature = null,
                    forecast16Days = emptyList(),
                    online = false,
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
            }
        state.update { current -> current.copy(currentLocationWeather = updated) }
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
        state.update { current ->
            current.copy(
                observations = current.observations + CropObservation(
                    id = UUID.randomUUID().toString(),
                    parcelId = parcelId,
                    date = date,
                    cropStage = cropStage,
                    generalStatus = generalStatus,
                    symptoms = symptoms,
                    recommendation = recommendation,
                    photoUri = photoUri
                )
            )
        }
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
        state.update { current ->
            current.copy(
                observations = current.observations.map { observation ->
                    if (observation.id == observationId) {
                        observation.copy(
                            parcelId = parcelId,
                            date = date,
                            cropStage = cropStage,
                            generalStatus = generalStatus,
                            symptoms = symptoms,
                            recommendation = recommendation,
                            photoUri = photoUri
                        )
                    } else observation
                }
            )
        }
    }

    override suspend fun deleteObservation(observationId: String) {
        state.update { current ->
            current.copy(observations = current.observations.filterNot { it.id == observationId })
        }
    }

    override suspend fun addInventoryItem(name: String, category: String, stock: Double, unit: String, minimumStock: Double) {
        state.update { current ->
            current.copy(
                inventory = current.inventory + InventoryItem(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    category = category,
                    stock = stock,
                    unit = unit,
                    minimumStock = minimumStock
                )
            )
        }
    }

    override suspend fun updateInventoryItem(id: String, name: String, category: String, stock: Double, unit: String, minimumStock: Double) {
        state.update { current ->
            current.copy(
                inventory = current.inventory.map { item ->
                    if (item.id == id) {
                        item.copy(
                            name = name,
                            category = category,
                            stock = stock,
                            unit = unit,
                            minimumStock = minimumStock
                        )
                    } else item
                }
            )
        }
    }

    override suspend fun deleteInventoryItem(id: String) {
        state.update { current ->
            current.copy(inventory = current.inventory.filterNot { it.id == id })
        }
    }

    private fun seedState(): AppUiState {
        val parcels = listOf(
            Parcel("p1", "Lote La Esperanza", "San Juan de la Maguana", 1.8, CropType.Cassava, "Valencia", "2026-02-10", "2026-12-10", 18.8070, -71.2280, emptyList(), true),
            Parcel("p2", "Parcela El Mango", "Moca", 1.2, CropType.SweetPotato, "Beauregard", "2026-03-05", "2026-08-02", 19.3949, -70.5250, emptyList(), false),
            Parcel("p3", "Loma Verde", "La Vega", 2.0, CropType.Corn, "Maiz amarillo ICTA", "2026-01-20", "2026-05-25", 19.2241, -70.5296, emptyList(), false),
            Parcel("p4", "Las Palmas", "Bonao", 0.9, CropType.Yam, "Diamantes", "2026-02-28", "2026-10-25", 18.9420, -70.4096, emptyList(), false)
        )

        return AppUiState(
            parcels = parcels,
            tasks = listOf(
                CropTask("t1", "p1", "Aplicar deshierbe temprano", "2026-04-16", TaskType.Weeding, false, "Alta", true),
                CropTask("t2", "p2", "Revisar humedad del surco", "2026-04-17", TaskType.Irrigation, false, "Alta", true),
                CropTask("t3", "p3", "Fertilizacion de cobertura", "2026-04-19", TaskType.Fertilization, false, "Media", true),
                CropTask("t4", "p4", "Monitoreo de hojas amarillas", "2026-04-20", TaskType.Monitoring, false, "Media", false),
                CropTask("t5", "p1", "Preparar plan de cosecha", "2026-11-20", TaskType.Harvest, false, "Baja", false)
            ),
            activities = listOf(
                ActivityRecord("a1", "p1", ActivityType.Sowing, "2026-02-10", 180.0, "1200 estacas", "Siembra con surco alto."),
                ActivityRecord("a2", "p2", ActivityType.Irrigation, "2026-04-14", 25.0, "4 horas", "Riego liviano por falta de lluvia."),
                ActivityRecord("a3", "p3", ActivityType.Fertilization, "2026-04-10", 140.0, "3 quintales", "Se aplico mezcla NPK al voleo."),
                ActivityRecord("a4", "p4", ActivityType.Labor, "2026-04-12", 70.0, "2 jornales", "Aporque y limpieza de pasillos.")
            ),
            observations = listOf(
                CropObservation("o1", "p1", "2026-04-15", "Desarrollo vegetativo", "Bueno", listOf("Sin sintomas graves"), "Mantener control de maleza y revisar drenaje.", null),
                CropObservation("o2", "p2", "2026-04-14", "Formacion de guias", "Regular", listOf("Hojas algo caidas"), "Verificar humedad del suelo y evitar encharcamiento.", null),
                CropObservation("o3", "p4", "2026-04-13", "Desarrollo inicial", "Alerta", listOf("Hojas amarillas"), "Revisar fertilidad, drenaje y presencia de danos en raiz o tallo.", null)
            ),
            inventory = listOf(
                InventoryItem("i1", "Estacas de yuca", "Material de siembra", 350.0, "unidad", 200.0),
                InventoryItem("i2", "Fertilizante NPK 15-15-15", "Fertilizante", 6.0, "sacos", 3.0),
                InventoryItem("i3", "Machete", "Herramienta", 4.0, "unidad", 2.0),
                InventoryItem("i4", "Bioinsumo foliar", "Proteccion", 1.0, "galon", 2.0)
            ),
            harvests = listOf(
                HarvestSummary("p1", CropType.Cassava, 0.0, 420.0, 0.0),
                HarvestSummary("p2", CropType.SweetPotato, 0.0, 260.0, 0.0),
                HarvestSummary("p3", CropType.Corn, 1250.0, 510.0, 920.0),
                HarvestSummary("p4", CropType.Yam, 0.0, 390.0, 0.0)
            ),
            tips = listOf(
                AgronomicTip(CropType.Cassava, "Siembra", "Usar estacas sanas, maduras y con buen vigor. Evitar material con pudricion o lesiones."),
                AgronomicTip(CropType.SweetPotato, "Crecimiento", "Mantener suelo suelto y con humedad uniforme para favorecer el desarrollo de raices."),
                AgronomicTip(CropType.Yam, "Monitoreo", "Revisar drenaje y tutorado cuando aplique; el exceso de agua aumenta problemas en raiz."),
                AgronomicTip(CropType.Corn, "Abonado", "Fraccionar fertilizacion segun estado del cultivo y humedad del suelo.")
            ),
            currentLocationWeather = WeatherSnapshot(
                locationLabel = "Ubicación actual",
                status = "Nublado con lluvias aisladas",
                statusResId = null,
                rainfallMm = 18,
                temperatureC = 29,
                humidityPercent = 82,
                windSpeedKph = 5.0,
                evapotranspiration = null,
                soilTemperature = null,
                forecast16Days = emptyList(),
                online = false,
                source = "Local",
                updatedAtEpochMillis = System.currentTimeMillis()
            ),
            parcelWeatherById = mapOf(
                "p1" to WeatherSnapshot(
                    locationLabel = "Lote La Esperanza",
                    status = "Nublado con lluvias aisladas",
                    statusResId = null,
                    rainfallMm = 18,
                    temperatureC = 29,
                    humidityPercent = 82,
                    windSpeedKph = 10.0,
                    evapotranspiration = null,
                    soilTemperature = null,
                    forecast16Days = emptyList(),
                    online = false,
                    source = "Local",
                    updatedAtEpochMillis = System.currentTimeMillis()
                ),
                "p2" to WeatherSnapshot(
                    locationLabel = "Parcela El Mango",
                    status = "Parcialmente soleado",
                    statusResId = null,
                    rainfallMm = 4,
                    temperatureC = 30,
                    humidityPercent = 70,
                    windSpeedKph = 12.0,
                    evapotranspiration = null,
                    soilTemperature = null,
                    forecast16Days = emptyList(),
                    online = false,
                    source = "Local",
                    updatedAtEpochMillis = System.currentTimeMillis()
                ),
                "p3" to WeatherSnapshot(
                    locationLabel = "Loma Verde",
                    status = "Sin lluvia esperada",
                    statusResId = null,
                    rainfallMm = 0,
                    temperatureC = 27,
                    humidityPercent = 66,
                    windSpeedKph = 8.0,
                    evapotranspiration = null,
                    soilTemperature = null,
                    forecast16Days = emptyList(),
                    online = false,
                    source = "Local",
                    updatedAtEpochMillis = System.currentTimeMillis()
                ),
                "p4" to WeatherSnapshot(
                    locationLabel = "Las Palmas",
                    status = "Lluvia ligera esperada",
                    statusResId = null,
                    rainfallMm = 2,
                    temperatureC = 28,
                    humidityPercent = 79,
                    windSpeedKph = 15.0,
                    evapotranspiration = null,
                    soilTemperature = null,
                    forecast16Days = emptyList(),
                    online = false,
                    source = "Local",
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
            )
        )
    }

    override suspend fun refreshSatelliteData(parcelId: String) {}
    override suspend fun refreshPestPredictions(parcelId: String) {}
    override suspend fun refreshHistoricalGrids(parcelId: String) {}
    override suspend fun calculateAgroInsights(parcelId: String) {}
}
