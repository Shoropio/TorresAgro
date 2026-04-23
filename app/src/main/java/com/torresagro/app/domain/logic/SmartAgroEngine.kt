package com.torresagro.app.domain.logic

import com.torresagro.app.domain.model.ActivityRecord
import com.torresagro.app.domain.model.ActivityType
import com.torresagro.app.domain.model.AgriData
import com.torresagro.app.domain.model.AgronomicRule
import com.torresagro.app.domain.model.CropObservation
import com.torresagro.app.domain.model.CropTask
import com.torresagro.app.domain.model.Parcel
import com.torresagro.app.domain.model.PhenologicalStage
import com.torresagro.app.domain.model.SmartCropCatalog
import com.torresagro.app.domain.model.SmartParcelAnalysis
import com.torresagro.app.domain.model.SmartSuggestion
import com.torresagro.app.domain.model.SmartSuggestionSource
import com.torresagro.app.domain.model.WeatherSnapshot
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.min

object SmartAgroEngine {
    fun analyze(
        parcels: List<Parcel>,
        tasks: List<CropTask>,
        activities: List<ActivityRecord>,
        observations: List<CropObservation>,
        weatherByParcel: Map<String, WeatherSnapshot>,
        agriByParcel: Map<String, AgriData>
    ): List<SmartParcelAnalysis> {
        val today = LocalDate.now()
        return parcels.map { parcel ->
            val profile = SmartCropCatalog.profileFor(parcel.cropType)
            val daysAfterSowing = runCatching {
                ChronoUnit.DAYS.between(LocalDate.parse(parcel.sowingDate), today)
            }.getOrDefault(0L).coerceAtLeast(0)
            val stage = profile?.stages?.currentStage(daysAfterSowing)
            val parcelTasks = tasks.filter { it.parcelId == parcel.id }
            val parcelActivities = activities.filter { it.parcelId == parcel.id }
            val parcelObservations = observations.filter { it.parcelId == parcel.id }
            val weather = weatherByParcel[parcel.id]
            val agri = agriByParcel[parcel.id]

            val suggestions = buildList {
                if (profile != null) {
                    addAll(
                        profile.rules.mapNotNull { rule ->
                            rule.evaluate(parcel, stage, weather, agri, parcelActivities, parcelObservations)
                        }
                    )
                    addAll(stageTaskSuggestions(parcel, stage, daysAfterSowing, parcelTasks, profile.criticalTasks))
                }
                addAll(learnFromHistory(parcel, parcels, activities, tasks, daysAfterSowing))
            }.distinctBy { it.title + it.source }.sortedWith(
                compareByDescending<SmartSuggestion> { it.priority == "Alta" }
                    .thenByDescending { it.confidence }
            )

            SmartParcelAnalysis(
                parcelId = parcel.id,
                parcelName = parcel.name,
                cropType = parcel.cropType,
                currentStage = stage,
                daysAfterSowing = daysAfterSowing,
                openTasks = parcelTasks.count { !it.completed },
                completedActivities = parcelActivities.size,
                suggestions = suggestions.take(8),
                technicalSheet = profile?.technicalSheet
            )
        }
    }

    private fun List<PhenologicalStage>.currentStage(daysAfterSowing: Long): PhenologicalStage? =
        firstOrNull { daysAfterSowing in it.startDay.toLong()..it.endDay.toLong() } ?: maxByOrNull { it.endDay }

    private fun AgronomicRule.evaluate(
        parcel: Parcel,
        stage: PhenologicalStage?,
        weather: WeatherSnapshot?,
        agri: AgriData?,
        activities: List<ActivityRecord>,
        observations: List<CropObservation>
    ): SmartSuggestion? {
        if (stageId != null && stage?.id != stageId) return null
        if (minRainMm != null && (weather?.rainfallMm ?: 0) < minRainMm) return null
        if (maxRainMm != null && (weather?.rainfallMm ?: Int.MAX_VALUE) > maxRainMm) return null
        if (minTempC != null && (weather?.temperatureC ?: Int.MIN_VALUE) < minTempC) return null
        if (maxTempC != null && (weather?.temperatureC ?: Int.MAX_VALUE) > maxTempC) return null
        if (minHumidityPercent != null && (weather?.humidityPercent ?: 0) < minHumidityPercent) return null
        if (maxSoilMoisture != null && (agri?.soilMoisture ?: Double.MAX_VALUE) > maxSoilMoisture) return null
        if (minNdvi != null && (agri?.ndvi ?: 0.0) < minNdvi) return null
        if (maxNdvi != null && (agri?.ndvi ?: Double.MAX_VALUE) > maxNdvi) return null

        if (symptomKeywords.isNotEmpty()) {
            val found = observations.any { observation ->
                val text = (observation.symptoms.joinToString(" ") + " " + observation.generalStatus).lowercase()
                symptomKeywords.any { text.contains(it.lowercase()) }
            }
            if (!found) return null
        }

        if (requiredActivityGapDays != null) {
            val recent = activities.any { activity ->
                activity.activityType == (activityType ?: ActivityType.Labor) &&
                    runCatching {
                        ChronoUnit.DAYS.between(LocalDate.parse(activity.date), LocalDate.now())
                    }.getOrDefault(Long.MAX_VALUE) <= requiredActivityGapDays
            }
            if (recent) return null
        }

        val confidence = listOfNotNull(
            weather?.let { 0.18 },
            agri?.let { 0.18 },
            stage?.let { 0.14 },
            observations.takeIf { it.isNotEmpty() }?.let { 0.10 }
        ).sum() + 0.45

        return SmartSuggestion(
            parcelId = parcel.id,
            title = title,
            description = recommendation,
            actionWindow = stage?.name ?: "Segun condicion actual",
            priority = priority,
            confidence = min(confidence, 0.92),
            source = SmartSuggestionSource.TechnicalRule,
            sourceDetail = source
        )
    }

    private fun stageTaskSuggestions(
        parcel: Parcel,
        stage: PhenologicalStage?,
        daysAfterSowing: Long,
        existingTasks: List<CropTask>,
        templates: List<com.torresagro.app.domain.model.StageTaskTemplate>
    ): List<SmartSuggestion> {
        return templates.filter { template ->
            val inWindow = daysAfterSowing in (template.dayOffset - 5).toLong()..(template.dayOffset + 10).toLong()
            val alreadyTracked = existingTasks.any {
                it.taskType == template.taskType && it.title.contains(template.title.take(8), ignoreCase = true)
            }
            inWindow && !alreadyTracked
        }.map { template ->
            SmartSuggestion(
                parcelId = parcel.id,
                title = template.title,
                description = "Labor critica esperada para ${parcel.cropType.displayName} cerca del dia ${template.dayOffset} del ciclo.",
                actionWindow = stage?.name ?: "Dia $daysAfterSowing del ciclo",
                priority = "Media",
                confidence = 0.72,
                source = SmartSuggestionSource.TechnicalRule,
                sourceDetail = "Calendario agronomico dinamico por cultivo"
            )
        }
    }

    private fun learnFromHistory(
        parcel: Parcel,
        parcels: List<Parcel>,
        activities: List<ActivityRecord>,
        tasks: List<CropTask>,
        daysAfterSowing: Long
    ): List<SmartSuggestion> {
        val similarParcelIds = parcels
            .filter { it.id != parcel.id && it.cropType == parcel.cropType }
            .map { it.id }
            .toSet()
        if (similarParcelIds.isEmpty()) return emptyList()

        val learnedActivities = activities
            .filter { it.parcelId in similarParcelIds }
            .groupingBy { it.activityType }
            .eachCount()
            .filterValues { it >= 2 }

        val learnedTasks = tasks
            .filter { it.parcelId in similarParcelIds && it.completed }
            .groupingBy { it.taskType }
            .eachCount()
            .filterValues { it >= 2 }

        val fromActivities = learnedActivities.map { (type, count) ->
            SmartSuggestion(
                parcelId = parcel.id,
                title = "Patron historico: ${type.label}",
                description = "Otras parcelas de ${parcel.cropType.displayName} registraron esta labor con frecuencia. Revisar si aplica al lote actual.",
                actionWindow = "Dia $daysAfterSowing del ciclo",
                priority = if (count >= 4) "Alta" else "Media",
                confidence = min(0.55 + (count * 0.08), 0.88),
                source = SmartSuggestionSource.HistoricalLearning,
                sourceDetail = "$count actividades similares en historial operativo"
            )
        }

        val fromTasks = learnedTasks.map { (type, count) ->
            SmartSuggestion(
                parcelId = parcel.id,
                title = "Procedimiento repetido: ${type.label}",
                description = "El historial muestra esta tarea completada en parcelas del mismo cultivo; considerar programarla si el contexto coincide.",
                actionWindow = "Segun avance fenologico",
                priority = "Media",
                confidence = min(0.50 + (count * 0.07), 0.82),
                source = SmartSuggestionSource.SimilarParcelPattern,
                sourceDetail = "$count tareas completadas en parcelas similares"
            )
        }

        return (fromActivities + fromTasks).sortedByDescending { it.confidence }.take(4)
    }
}
