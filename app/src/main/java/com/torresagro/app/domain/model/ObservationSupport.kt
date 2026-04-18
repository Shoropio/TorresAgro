package com.torresagro.app.domain.model

object ObservationSupport {
    val symptomsCatalog = listOf(
        "Hojas amarillas",
        "Hojas caidas",
        "Marchitez",
        "Manchas en hojas",
        "Pudricion en raiz",
        "Bajo crecimiento",
        "Danos por plaga",
        "Suelo seco",
        "Encharcamiento",
        "Tallo debil"
    )

    fun recommendationFor(symptoms: List<String>, status: String): String {
        val notes = buildList {
            if (symptoms.any { it == "Suelo seco" || it == "Marchitez" || it == "Hojas caidas" }) {
                add("Revisar humedad del suelo y ajustar riego sin provocar encharcamiento.")
            }
            if (symptoms.any { it == "Hojas amarillas" || it == "Bajo crecimiento" || it == "Tallo debil" }) {
                add("Verificar fertilidad, drenaje y vigor general del cultivo.")
            }
            if (symptoms.any { it == "Danos por plaga" || it == "Manchas en hojas" }) {
                add("Inspeccionar incidencia de plagas o dano foliar y priorizar manejo preventivo.")
            }
            if (symptoms.any { it == "Pudricion en raiz" || it == "Encharcamiento" }) {
                add("Mejorar drenaje y revisar condiciones de la raiz para evitar perdidas.")
            }
            if (status == "Bueno" && isEmpty()) {
                add("Mantener seguimiento periodico, limpieza del lote y registro de cambios.")
            }
        }
        return notes.ifEmpty {
            listOf("Registrar observaciones adicionales y revisar el lote nuevamente en la proxima visita.")
        }.joinToString(" ")
    }
}
