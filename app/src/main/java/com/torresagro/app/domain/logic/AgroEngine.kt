package com.torresagro.app.domain.logic

import com.torresagro.app.domain.model.AgriData
import com.torresagro.app.domain.model.AgroAlert
import com.torresagro.app.domain.model.AlertSeverity
import com.torresagro.app.domain.model.AlertType
import com.torresagro.app.domain.model.Recommendation
import com.torresagro.app.domain.model.RecommendationType
import com.torresagro.app.domain.model.WeatherSnapshot

object AgroEngine {
    fun calculateAlerts(weather: WeatherSnapshot, agri: AgriData?): List<AgroAlert> {
        val alerts = mutableListOf<AgroAlert>()
        val rainNext3Days = weather.forecast16Days.take(3).sumOf { it.rainMm }

        if (weather.rainfallMm > 30) {
            alerts.add(
                AgroAlert(
                    id = "rain_${System.currentTimeMillis()}",
                    type = AlertType.Rain,
                    message = "Alerta de lluvia intensa: ${weather.rainfallMm}mm. En Costa Rica revisa drenajes, escorrentia y accesos al lote.",
                    severity = AlertSeverity.High,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        if (rainNext3Days >= 60) {
            alerts.add(
                AgroAlert(
                    id = "rain_forecast_${System.currentTimeMillis()}",
                    type = AlertType.Rain,
                    message = "Lluvia acumulada probable (${rainNext3Days.toInt()}mm en 3 dias). Programar drenaje, evitar aplicaciones lavables y proteger cosecha.",
                    severity = AlertSeverity.High,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        if (weather.temperatureC > 34) {
            alerts.add(
                AgroAlert(
                    id = "heat_${System.currentTimeMillis()}",
                    type = AlertType.HeatStress,
                    message = "Estres termico: ${weather.temperatureC}C. Revisar humedad del suelo, cobertura y labores en horas frescas.",
                    severity = AlertSeverity.Medium,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        weather.evapotranspiration?.let { et0 ->
            if (et0 > 6.0) {
                alerts.add(
                    AgroAlert(
                        id = "et0_${System.currentTimeMillis()}",
                        type = AlertType.HeatStress,
                        message = "Alta evapotranspiracion ($et0 mm/dia). Perdida de humedad acelerada; revisar riego o cobertura.",
                        severity = AlertSeverity.Medium,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }

        weather.soilTemperature?.let { soilTemp ->
            if (soilTemp > 30.0) {
                alerts.add(
                    AgroAlert(
                        id = "soil_temp_${System.currentTimeMillis()}",
                        type = AlertType.HeatStress,
                        message = "Temperatura de suelo alta ($soilTemp C). Revisar cobertura y humedad cerca de la raiz.",
                        severity = AlertSeverity.Low,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }

        agri?.let {
            if (it.ndvi < 0.4) {
                alerts.add(
                    AgroAlert(
                        id = "ndvi_${System.currentTimeMillis()}",
                        type = AlertType.Pests,
                        message = "Vigor vegetativo bajo (NDVI: ${"%.2f".format(it.ndvi)}). Revisar nutricion, malezas, drenaje y posibles plagas.",
                        severity = AlertSeverity.High,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }

        return alerts
    }

    fun getRecommendations(weather: WeatherSnapshot, agri: AgriData?): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()
        val rainNext3Days = weather.forecast16Days.take(3).sumOf { it.rainMm }
        val rainNext5Days = weather.forecast16Days.take(5).sumOf { it.rainMm }

        if (agri != null && agri.soilMoisture < 20 && weather.rainfallMm < 5) {
            recs.add(
                Recommendation(
                    title = "Programar riego o conservacion de humedad",
                    description = "La humedad del suelo es baja (${"%.1f".format(agri.soilMoisture)}%) y no hay lluvia suficiente. Revisar suelo antes de regar.",
                    type = RecommendationType.Irrigation
                )
            )
        }

        if (weather.temperatureC in 20..30 && rainNext5Days in 10.0..80.0) {
            recs.add(
                Recommendation(
                    title = "Ventana de siembra",
                    description = "Temperatura y lluvia previstas son manejables para establecimiento. Confirmar drenaje y acceso al lote.",
                    type = RecommendationType.Sowing
                )
            )
        }

        if (weather.humidityPercent > 85 && weather.temperatureC > 25) {
            recs.add(
                Recommendation(
                    title = "Monitoreo preventivo de hongos",
                    description = "Alta humedad y calor favorecen enfermedades fungosas. Registrar foto de hojas/frutos y crear tarea de seguimiento.",
                    type = RecommendationType.PestControl
                )
            )
        }

        if (rainNext3Days >= 35) {
            recs.add(
                Recommendation(
                    title = "Evitar aplicaciones antes de lluvia",
                    description = "Hay lluvia cercana en el pronostico. Reprogramar fertilizacion o fumigacion lavable y revisar zanjas.",
                    type = RecommendationType.Fertilization
                )
            )
        }

        return recs
    }
}
