package com.torresagro.app.domain.model

enum class CropType(val displayName: String, val cycleDays: Int) {
    Cassava("Yuca", 300),
    SweetPotato("Camote", 150),
    Yam("Name", 240),
    Corn("Maiz", 120),
    Plantain("Platano", 330)
}

enum class TaskType(val label: String) {
    SoilPrep("Preparacion del suelo"),
    Planting("Siembra"),
    Weeding("Deshierbe"),
    Fertilization("Abonado"),
    Irrigation("Riego"),
    PestControl("Control fitosanitario"),
    Harvest("Cosecha"),
    Monitoring("Monitoreo")
}

enum class ActivityType(val label: String) {
    Sowing("Siembra"),
    Irrigation("Riego"),
    Fertilization("Fertilizacion"),
    Spraying("Fumigacion"),
    Weeding("Deshierbe"),
    Harvest("Cosecha"),
    Labor("Mano de obra")
}

data class Parcel(
    val id: String,
    val name: String,
    val locationName: String,
    val sizeHectares: Double,
    val cropType: CropType,
    val variety: String,
    val sowingDate: String,
    val expectedHarvestDate: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val boundary: List<Pair<Double, Double>> = emptyList(),
    val offlinePendingSync: Boolean = false
)

data class CropTask(
    val id: String,
    val parcelId: String,
    val title: String,
    val dueDate: String,
    val taskType: TaskType,
    val completed: Boolean,
    val priority: String,
    val reminderEnabled: Boolean
)

data class ActivityRecord(
    val id: String,
    val parcelId: String,
    val activityType: ActivityType,
    val date: String,
    val cost: Double,
    val quantity: String,
    val notes: String,
    val photoUri: String? = null
)

data class CropObservation(
    val id: String,
    val parcelId: String,
    val date: String,
    val cropStage: String,
    val generalStatus: String,
    val symptoms: List<String>,
    val recommendation: String,
    val photoUri: String? = null
)

data class InventoryItem(
    val id: String,
    val name: String,
    val category: String,
    val stock: Double,
    val unit: String,
    val minimumStock: Double
)

data class HarvestSummary(
    val parcelId: String,
    val cropType: CropType,
    val harvestedKg: Double,
    val totalCost: Double,
    val estimatedIncome: Double
) {
    val profit: Double get() = estimatedIncome - totalCost
}

data class WeatherSnapshot(
    val locationLabel: String,
    val status: String,
    val rainfallMm: Int,
    val temperatureC: Int,
    val humidityPercent: Int,
    val online: Boolean
)

data class AgronomicTip(
    val cropType: CropType,
    val stage: String,
    val tip: String
)

data class AppUiState(
    val parcels: List<Parcel> = emptyList(),
    val tasks: List<CropTask> = emptyList(),
    val activities: List<ActivityRecord> = emptyList(),
    val observations: List<CropObservation> = emptyList(),
    val inventory: List<InventoryItem> = emptyList(),
    val harvests: List<HarvestSummary> = emptyList(),
    val tips: List<AgronomicTip> = emptyList(),
    val weather: WeatherSnapshot? = null
)
