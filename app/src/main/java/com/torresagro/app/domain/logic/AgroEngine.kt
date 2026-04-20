package com.torresagro.app.domain.logic

import com.torresagro.app.domain.model.*

/**
 * Motor de reglas e indicadores para agricultura avanzada.
 * Implementa cálculos de riesgo y recomendaciones basadas en datos climáticos y satelitales.
 */
object AgroEngine {
    
    /**
     * Calcula alertas basadas en umbrales de riesgo.
     */
    fun calculateAlerts(weather: WeatherSnapshot, agri: AgriData?): List<AgroAlert> {
        val alerts = mutableListOf<AgroAlert>()
        
        // 1. Riesgo por lluvia intensa (Umbral: > 30mm en pronóstico o actual)
        if (weather.rainfallMm > 30) {
            alerts.add(AgroAlert(
                id = "rain_${System.currentTimeMillis()}",
                type = AlertType.Rain,
                message = "Alerta de Lluvia Intensa: ${weather.rainfallMm}mm. Riesgo de erosión o inundación.",
                severity = AlertSeverity.High,
                timestamp = System.currentTimeMillis()
            ))
        }

        // 2. Estrés térmico (Umbral: > 34°C)
        if (weather.temperatureC > 34) {
            alerts.add(AgroAlert(
                id = "heat_${System.currentTimeMillis()}",
                type = AlertType.HeatStress,
                message = "Estrés Térmico: ${weather.temperatureC}°C. El cultivo podría cerrar estomas.",
                severity = AlertSeverity.Medium,
                timestamp = System.currentTimeMillis()
            ))
        }

        // 3. Salud del cultivo (NDVI bajo)
        agri?.let {
            if (it.ndvi < 0.4) {
                alerts.add(AgroAlert(
                    id = "ndvi_${System.currentTimeMillis()}",
                    type = AlertType.Pests,
                    message = "Vigor Vegetativo Bajo (NDVI: ${"%.2f".format(it.ndvi)}). Revisar posible presencia de plagas.",
                    severity = AlertSeverity.High,
                    timestamp = System.currentTimeMillis()
                ))
            }
        }

        return alerts
    }

    /**
     * Motor de recomendaciones basado en reglas agrícolas.
     */
    fun getRecommendations(weather: WeatherSnapshot, agri: AgriData?): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()
        
        // Sugerencia de Riego
        if (agri != null && agri.soilMoisture < 20 && weather.rainfallMm < 5) {
            recs.add(Recommendation(
                title = "Programar Riego",
                description = "La humedad del suelo es crítica (${"%.1f".format(agri.soilMoisture)}%) y no se esperan lluvias significativas.",
                type = RecommendationType.Irrigation
            ))
        }

        // Recomendación de Siembra
        if (weather.temperatureC in 20..28 && weather.rainfallMm in 2..15) {
            recs.add(Recommendation(
                title = "Ventana de Siembra",
                description = "Condiciones de temperatura y humedad óptimas para la germinación y establecimiento.",
                type = RecommendationType.Sowing
            ))
        }

        // Control de Plagas Preventive
        if (weather.humidityPercent > 85 && weather.temperatureC > 25) {
            recs.add(Recommendation(
                title = "Monitoreo de Hongos",
                description = "Alta humedad y calor favorecen la proliferación de patógenos fúngicos.",
                type = RecommendationType.PestControl
            ))
        }

        return recs
    }
}
