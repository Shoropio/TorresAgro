package com.torresagro.app.domain.model

object CostaRicaAgroGuide {
    const val sourceSummary: String =
        "Referencias operativas: MAG Costa Rica, INTA, SFE y buenas practicas FAO para tropico humedo."

    val rainySeasonMonths = setOf(5, 6, 7, 8, 9, 10, 11)

    fun zoneHint(locationName: String): String {
        val normalized = locationName.lowercase()
        return when {
            listOf("limon", "talamanca", "pococi", "guacimo", "siquirres", "matina").any { normalized.contains(it) } ->
                "Caribe: vigilar drenaje, enfermedades foliares y accesos por lluvia frecuente."
            listOf("guanacaste", "nicoya", "liberia", "santa cruz", "canas").any { normalized.contains(it) } ->
                "Pacifico Norte: priorizar agua disponible, cobertura de suelo y ventanas de siembra."
            listOf("perez zeledon", "san isidro", "coto brus", "corredores", "osa", "golfito").any { normalized.contains(it) } ->
                "Pacifico Sur: combinar drenaje con monitoreo de hongos en periodos humedos."
            listOf("cartago", "zarcero", "san carlos", "alfaro ruiz").any { normalized.contains(it) } ->
                "Zona alta: revisar temperatura, viento y sanidad en periodos de alta humedad."
            else ->
                "Costa Rica: ajustar manejo segun lluvia local, drenaje del lote y observaciones semanales."
        }
    }

    fun checklistFor(cropType: CropType): List<String> {
        val common = listOf(
            "Registrar GPS, foto y observacion semanal por parcela.",
            "Revisar drenajes antes de lluvias fuertes.",
            "Separar tareas planificadas de actividades ya realizadas."
        )
        val cropSpecific = when (cropType) {
            CropType.Cassava, CropType.SweetPotato, CropType.Yam -> listOf(
                "Usar material vegetativo sano y evitar encharcamiento.",
                "Monitorear pudriciones y vigor despues de lluvias prolongadas."
            )
            CropType.Corn -> listOf(
                "Verificar poblacion y malezas temprano.",
                "Cuidar humedad en floracion y llenado."
            )
            CropType.Squash, CropType.Cucumber, CropType.Watermelon, CropType.Melon -> listOf(
                "Monitorear hongos foliares cuando hay calor y humedad.",
                "Proteger floracion, polinizacion y calidad de fruto."
            )
            CropType.Plantain -> listOf(
                "Revisar hojas funcionales, deshije y drenaje.",
                "Monitorear sigatoka y danos por viento."
            )
        }
        return cropSpecific + common
    }
}
