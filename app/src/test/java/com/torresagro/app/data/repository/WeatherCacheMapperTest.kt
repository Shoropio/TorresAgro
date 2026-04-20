package com.torresagro.app.data.repository

import com.torresagro.app.data.local.entity.WeatherCacheEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class WeatherCacheMapperTest {
    @Test
    fun `weather cache maps updated timestamp to domain`() {
        val entity = WeatherCacheEntity(
            id = "parcel_1",
            parcelId = "1",
            locationLabel = "Parcela Norte",
            status = "Sin lluvia esperada",
            rainfallMm = 0,
            temperatureC = 31,
            humidityPercent = 64,
            online = false,
            updatedAtEpochMillis = 1_713_456_789_000
        )

        val snapshot = entity.toDomain()

        assertEquals("Parcela Norte", snapshot.locationLabel)
        assertEquals(1_713_456_789_000, snapshot.updatedAtEpochMillis)
        assertFalse(snapshot.online)
    }
}
