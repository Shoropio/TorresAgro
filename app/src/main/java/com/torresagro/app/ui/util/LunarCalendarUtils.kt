package com.torresagro.app.ui.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class MoonPhase(val label: String, val description: String, val symbol: String) {
    NEW_MOON("Luna Nueva", "Ideal para control de malezas y poda de limpieza. La savia está en las raíces.", "🌑"),
    WAXING_CRESCENT("Luna Creciente", "Buen momento para sembrar hortalizas de hoja y aplicar fertilizantes.", "🌒"),
    FIRST_QUARTER("Cuarto Creciente", "Estimula el crecimiento vegetativo. Ideal para injertos y siembra.", "🌓"),
    WAXING_GIBBOUS("Gibosa Creciente", "La savia sube con fuerza. Momento óptimo para trasplantes.", "🌔"),
    FULL_MOON("Luna Llena", "Pico de savia en las partes aéreas. Ideal para cosechar frutos jugosos.", "🌕"),
    WANING_GIBBOUS("Gibosa Menguante", "La savia empieza a bajar. Buen momento para control de plagas.", "🌖"),
    LAST_QUARTER("Cuarto Menguante", "Ideal para siembra de raíces y tubérculos (papa, zanahoria).", "🌗"),
    WANING_CRESCENT("Luna Menguante", "Periodo de reposo. Ideal para podas que busquen frenar el crecimiento.", "🌘")
}

object LunarCalendarUtils {
    // Reference date for a New Moon: 2000-01-06 18:14 UTC
    private val REFERENCE_NEW_MOON = LocalDate.of(2000, 1, 6)
    private const val SYNODIC_MONTH = 29.530588853

    fun getMoonPhase(date: LocalDate = LocalDate.now()): MoonPhase {
        val daysSince = ChronoUnit.DAYS.between(REFERENCE_NEW_MOON, date)
        val phase = (daysSince % SYNODIC_MONTH) / SYNODIC_MONTH
        
        return when {
            phase < 0.03 || phase > 0.97 -> MoonPhase.NEW_MOON
            phase < 0.22 -> MoonPhase.WAXING_CRESCENT
            phase < 0.28 -> MoonPhase.FIRST_QUARTER
            phase < 0.47 -> MoonPhase.WAXING_GIBBOUS
            phase < 0.53 -> MoonPhase.FULL_MOON
            phase < 0.72 -> MoonPhase.WANING_GIBBOUS
            phase < 0.78 -> MoonPhase.LAST_QUARTER
            else -> MoonPhase.WANING_CRESCENT
        }
    }

    fun getRecommendations(phase: MoonPhase): List<String> {
        return when (phase) {
            MoonPhase.NEW_MOON -> listOf(
                "Evitar siembras de hortalizas de fruto.",
                "Ideal para deshierbe y limpieza de terreno.",
                "Aplicar tratamientos contra insectos del suelo.",
                "Poda de árboles enfermos."
            )
            MoonPhase.WAXING_CRESCENT, MoonPhase.FIRST_QUARTER -> listOf(
                "Siembra de hortalizas de hoja (lechuga, espinaca).",
                "Realizar injertos de frutales.",
                "Aplicar abonos foliares para crecimiento rápido.",
                "Trasplante de plántulas de invernadero."
            )
            MoonPhase.WAXING_GIBBOUS, MoonPhase.FULL_MOON -> listOf(
                "Cosecha de frutas para consumo inmediato (mayor sabor).",
                "Evitar podas drásticas (mucho sangrado de savia).",
                "Riego profundo debido a alta transpiración.",
                "Siembra de plantas de crecimiento rápido."
            )
            MoonPhase.WANING_GIBBOUS, MoonPhase.LAST_QUARTER, MoonPhase.WANING_CRESCENT -> listOf(
                "Siembra de raíces y tubérculos (papa, yuca, zanahoria).",
                "Poda para controlar el tamaño de los árboles.",
                "Cosecha de granos y semillas para almacenamiento.",
                "Preparación de compost y abonos orgánicos."
            )
        }
    }
    fun getLunarForecast(days: Int = 14): List<Pair<LocalDate, MoonPhase>> {
        val today = LocalDate.now()
        return (0 until days).map { dayOffset ->
            val date = today.plusDays(dayOffset.toLong())
            date to getMoonPhase(date)
        }
    }
}
