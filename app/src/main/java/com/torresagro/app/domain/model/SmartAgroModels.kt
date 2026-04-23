package com.torresagro.app.domain.model

data class ProducerProfile(
    val id: String,
    val name: String,
    val phone: String? = null,
    val community: String? = null
)

data class FarmProfile(
    val id: String,
    val producerId: String,
    val name: String,
    val locationName: String,
    val altitudeMeters: Int? = null
)

data class CropVariety(
    val name: String,
    val cycleDays: Int,
    val notes: String
)

data class PhenologicalStage(
    val id: String,
    val name: String,
    val startDay: Int,
    val endDay: Int,
    val fieldFocus: List<String>
)

data class CropRiskProfile(
    val name: String,
    val triggers: List<String>,
    val suggestedMonitoring: String
)

data class CropTechnicalSheet(
    val cropType: CropType,
    val sourceSummary: String,
    val generalManagement: List<String>,
    val stages: List<PhenologicalStage>,
    val criticalFactors: List<String>,
    val suggestedPractices: List<String>,
    val fieldNotes: List<String>
)

data class AgronomicRule(
    val id: String,
    val cropType: CropType,
    val stageId: String? = null,
    val title: String,
    val recommendation: String,
    val priority: String,
    val source: String,
    val minRainMm: Int? = null,
    val maxRainMm: Int? = null,
    val minTempC: Int? = null,
    val maxTempC: Int? = null,
    val minHumidityPercent: Int? = null,
    val maxSoilMoisture: Double? = null,
    val minNdvi: Double? = null,
    val maxNdvi: Double? = null,
    val symptomKeywords: List<String> = emptyList(),
    val requiredActivityGapDays: Int? = null,
    val activityType: ActivityType? = null
)

enum class SmartSuggestionSource {
    TechnicalRule,
    HistoricalLearning,
    SimilarParcelPattern
}

data class SmartSuggestion(
    val parcelId: String,
    val title: String,
    val description: String,
    val actionWindow: String,
    val priority: String,
    val confidence: Double,
    val source: SmartSuggestionSource,
    val sourceDetail: String
)

data class SmartParcelAnalysis(
    val parcelId: String,
    val parcelName: String,
    val cropType: CropType,
    val currentStage: PhenologicalStage?,
    val daysAfterSowing: Long,
    val openTasks: Int,
    val completedActivities: Int,
    val suggestions: List<SmartSuggestion>,
    val technicalSheet: CropTechnicalSheet?
)

data class CropProfile(
    val cropType: CropType,
    val varieties: List<CropVariety>,
    val stages: List<PhenologicalStage>,
    val risks: List<CropRiskProfile>,
    val criticalTasks: List<StageTaskTemplate>,
    val rules: List<AgronomicRule>,
    val technicalSheet: CropTechnicalSheet
)
