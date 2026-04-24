package com.torresagro.app.ui.util

import com.torresagro.app.domain.model.ActivityType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val amountFormatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
private val quantityFormatter = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US))

fun formatCurrencyCrc(amount: Double): String = "₡${amountFormatter.format(amount)}"

fun formatQuantity(value: Double): String = quantityFormatter.format(value)

object CostaRicaMeasureUnits {
    val inventoryUnits = listOf(
        "kg",
        "g",
        "qq",
        "t",
        "L",
        "mL",
        "gal",
        "ha",
        "m²",
        "saco",
        "bolsa",
        "caja",
        "unidad",
        "racimo",
        "plántula",
        "estaca"
    )

    fun activityQuantityExamples(activityType: ActivityType): String = when (activityType) {
        ActivityType.Sowing -> "Ejemplos CR: 2 qq, 4 sacos, 250 estacas o 3 bandejas."
        ActivityType.Irrigation -> "Ejemplos CR: 800 L, 2 gal o 1.5 m³."
        ActivityType.Fertilization -> "Ejemplos CR: 25 kg, 1 qq, 2 sacos o 500 g."
        ActivityType.Spraying -> "Ejemplos CR: 1 L, 500 mL, 2 gal o 20 bombas."
        ActivityType.Weeding -> "Ejemplos CR: 1 ha, 2.5 ha o 1 lote."
        ActivityType.Harvest -> "Ejemplos CR: 35 kg, 12 cajas, 4 sacos o 18 racimos."
        ActivityType.Labor -> "Ejemplos CR: 1 jornada, 2 jornales o 6 horas."
    }
}
