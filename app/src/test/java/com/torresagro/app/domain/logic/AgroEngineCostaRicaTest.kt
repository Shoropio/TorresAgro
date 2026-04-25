package com.torresagro.app.domain.logic

import com.torresagro.app.domain.model.AgriData
import com.torresagro.app.domain.model.AlertType
import com.torresagro.app.domain.model.CostaRicaAgroGuide
import com.torresagro.app.domain.model.CropType
import com.torresagro.app.domain.model.DailyForecast
import com.torresagro.app.domain.model.RecommendationType
import com.torresagro.app.domain.model.WeatherSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgroEngineCostaRicaTest {
    @Test
    fun `rainy forecast creates drainage alert for Costa Rica`() {
        val weather = weather(
            forecast = listOf(
                DailyForecast("2026-05-01", 30.0, 24.0, 22.0),
                DailyForecast("2026-05-02", 29.0, 24.0, 21.0),
                DailyForecast("2026-05-03", 29.0, 23.0, 20.0)
            )
        )

        val alerts = AgroEngine.calculateAlerts(weather, agri = null)

        assertTrue(alerts.any { it.type == AlertType.Rain && it.message.contains("drenaje", ignoreCase = true) })
    }

    @Test
    fun `humid hot weather recommends fungal monitoring`() {
        val recommendations = AgroEngine.getRecommendations(
            weather = weather(humidity = 90, temperature = 27),
            agri = AgriData("p1", ndvi = 0.7, soilMoisture = 40.0, lastUpdate = 1L)
        )

        assertTrue(recommendations.any { it.type == RecommendationType.PestControl })
    }

    @Test
    fun `Costa Rica guide identifies Caribe locations`() {
        assertTrue(CostaRicaAgroGuide.zoneHint("Pococi, Limon").contains("Caribe"))
        assertEquals(5, CostaRicaAgroGuide.checklistFor(CropType.Cassava).size)
    }

    private fun weather(
        humidity: Int = 80,
        temperature: Int = 28,
        forecast: List<DailyForecast> = emptyList()
    ) = WeatherSnapshot(
        locationLabel = "Costa Rica",
        status = "Variable",
        rainfallMm = 0,
        temperatureC = temperature,
        humidityPercent = humidity,
        forecast16Days = forecast,
        online = true,
        updatedAtEpochMillis = 1L
    )
}
