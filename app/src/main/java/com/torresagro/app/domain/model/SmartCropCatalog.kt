package com.torresagro.app.domain.model

object SmartCropCatalog {
    private val commonSource = "FAO: semillas vegetativas y manejo de raices/tuberculos; MAG Costa Rica: guias tecnicas, ciclos de cultivo y sanidad vegetal."

    val profiles: List<CropProfile> = listOf(
        rootCrop(CropType.Cassava, 300, listOf("Valencia", "Se\u00f1orita"), "estacas sanas"),
        rootCrop(CropType.SweetPotato, 150, listOf("Beauregard", "Criollo"), "guias vigorosas"),
        rootCrop(CropType.Yam, 240, listOf("Diamantes", "Criollo"), "semilla o trozos sanos"),
        grainCrop(),
        cucurbit(CropType.Squash, 110, listOf("Criollo", "Butternut")),
        cucurbit(CropType.Cucumber, 90, listOf("Marketmore", "Hibrido local")),
        cucurbit(CropType.Watermelon, 75, listOf("Crimson Sweet", "Charleston Gray")),
        cucurbit(CropType.Melon, 90, listOf("Cantaloupe", "Honey Dew")),
        plantainCrop()
    )

    val technicalSheets: List<CropTechnicalSheet> = profiles.map { it.technicalSheet }

    fun profileFor(cropType: CropType): CropProfile? = profiles.firstOrNull { it.cropType == cropType }

    private fun rootCrop(
        cropType: CropType,
        cycleDays: Int,
        varieties: List<String>,
        plantingMaterial: String
    ): CropProfile {
        val stages = listOf(
            PhenologicalStage("soil", "Preparacion y seleccion de semilla", 0, 7, listOf("Drenaje", "material limpio", "trazado")),
            PhenologicalStage("establishment", "Establecimiento", 8, 45, listOf("Humedad estable", "resiembra", "malezas")),
            PhenologicalStage("growth", "Crecimiento vegetativo", 46, cycleDays / 2, listOf("Nutricion", "sanidad", "cobertura")),
            PhenologicalStage("filling", "Engrosamiento de raiz/tuberculo", (cycleDays / 2) + 1, cycleDays - 20, listOf("Humedad", "drenaje", "vigor")),
            PhenologicalStage("harvest", "Cosecha y poscosecha", cycleDays - 19, cycleDays + 30, listOf("Madurez", "danos mecanicos", "seleccion"))
        )
        val criticalTasks = listOf(
            StageTaskTemplate(0, TaskType.SoilPrep, "Preparar camas o lomillos con buen drenaje"),
            StageTaskTemplate(1, TaskType.Planting, "Sembrar $plantingMaterial"),
            StageTaskTemplate(25, TaskType.Weeding, "Control temprano de malezas"),
            StageTaskTemplate(45, TaskType.Fertilization, "Ajustar nutricion segun desarrollo"),
            StageTaskTemplate(cycleDays - 20, TaskType.Harvest, "Planificar cosecha y seleccion")
        )
        val rules = baseRules(cropType) + listOf(
            AgronomicRule(
                id = "${cropType.name}_drainage",
                cropType = cropType,
                stageId = "filling",
                title = "Revisar drenaje del lote",
                recommendation = "Raices y tuberculos son sensibles a encharcamiento; prioriza desagues y monitoreo de pudriciones.",
                priority = "Alta",
                source = "Regla tecnica FAO/MAG",
                minRainMm = 35
            )
        )
        val sheet = CropTechnicalSheet(
            cropType = cropType,
            sourceSummary = commonSource,
            generalManagement = listOf(
                "Usar material de siembra sano y uniforme.",
                "Evitar suelos compactados o con drenaje deficiente.",
                "Mantener control temprano de malezas para reducir competencia."
            ),
            stages = stages,
            criticalFactors = listOf("Drenaje", "sanidad de semilla", "humedad estable", "manejo poscosecha"),
            suggestedPractices = criticalTasks.map { it.title },
            fieldNotes = listOf("Registrar procedencia del material vegetativo.", "Comparar vigor entre parcelas del mismo cultivo.")
        )
        return CropProfile(
            cropType = cropType,
            varieties = varieties.map { CropVariety(it, cycleDays, "Ajustar ciclo segun zona y mercado.") },
            stages = stages,
            risks = listOf(
                CropRiskProfile("Pudricion", listOf("lluvia alta", "mal drenaje"), "Revisar base de tallo y raiz."),
                CropRiskProfile("Bajo vigor", listOf("NDVI bajo", "malezas"), "Comparar con parcelas similares.")
            ),
            criticalTasks = criticalTasks,
            rules = rules,
            technicalSheet = sheet
        )
    }

    private fun grainCrop(): CropProfile {
        val stages = listOf(
            PhenologicalStage("soil", "Preparacion", 0, 7, listOf("surcado", "humedad", "semilla")),
            PhenologicalStage("emergence", "Emergencia", 8, 20, listOf("poblacion", "malezas", "humedad")),
            PhenologicalStage("vegetative", "Desarrollo vegetativo", 21, 45, listOf("fertilizacion", "cogollo", "malezas")),
            PhenologicalStage("flowering", "Floracion y llenado", 46, 90, listOf("agua", "calor", "sanidad")),
            PhenologicalStage("harvest", "Cosecha y secado", 91, 130, listOf("madurez", "humedad de grano", "almacenamiento"))
        )
        val tasks = listOf(
            StageTaskTemplate(0, TaskType.SoilPrep, "Preparar suelo y surcos"),
            StageTaskTemplate(1, TaskType.Planting, "Siembra con densidad uniforme"),
            StageTaskTemplate(20, TaskType.Weeding, "Control temprano de malezas"),
            StageTaskTemplate(25, TaskType.Fertilization, "Primera fertilizacion"),
            StageTaskTemplate(45, TaskType.Fertilization, "Segunda fertilizacion")
        )
        val sheet = CropTechnicalSheet(
            cropType = CropType.Corn,
            sourceSummary = commonSource,
            generalManagement = listOf("Asegurar poblacion uniforme.", "Priorizar nutricion en crecimiento vegetativo.", "Evitar estres hidrico en floracion."),
            stages = stages,
            criticalFactors = listOf("Humedad en floracion", "fertilidad", "control temprano de malezas", "plagas de cogollo"),
            suggestedPractices = tasks.map { it.title },
            fieldNotes = listOf("Registrar densidad real de plantas.", "Comparar rendimiento por fecha de siembra.")
        )
        return CropProfile(CropType.Corn, listOf(CropVariety("Amarillo", 105, "Ciclo medio.")), stages, listOf(CropRiskProfile("Estres en floracion", listOf("calor", "baja humedad"), "Priorizar riego o conservacion de humedad.")), tasks, baseRules(CropType.Corn), sheet)
    }

    private fun cucurbit(cropType: CropType, cycleDays: Int, varieties: List<String>): CropProfile {
        val stages = listOf(
            PhenologicalStage("soil", "Preparacion y siembra", 0, 7, listOf("camas", "semilla", "drenaje")),
            PhenologicalStage("establishment", "Establecimiento", 8, 25, listOf("riego", "resiembra", "malezas")),
            PhenologicalStage("vegetative", "Guia y desarrollo", 26, 45, listOf("nutricion", "guias", "monitoreo")),
            PhenologicalStage("flowering", "Floracion y cuaje", 46, cycleDays - 20, listOf("polinizacion", "humedad", "hongos")),
            PhenologicalStage("harvest", "Cosecha", cycleDays - 19, cycleDays + 15, listOf("madurez", "calidad", "cortes"))
        )
        val tasks = listOf(
            StageTaskTemplate(0, TaskType.SoilPrep, "Preparar camas y drenajes"),
            StageTaskTemplate(1, TaskType.Planting, "Sembrar con humedad uniforme"),
            StageTaskTemplate(20, TaskType.Weeding, "Control de malezas"),
            StageTaskTemplate(35, TaskType.Monitoring, "Monitorear plagas y enfermedades"),
            StageTaskTemplate(50, TaskType.Irrigation, "Ajustar riego en floracion")
        )
        val rules = baseRules(cropType) + listOf(
            AgronomicRule(
                id = "${cropType.name}_fungus",
                cropType = cropType,
                stageId = "flowering",
                title = "Monitoreo preventivo de hongos",
                recommendation = "Alta humedad en cucurbitaceas aumenta riesgo de enfermedades foliares; revisar hojas y ventilacion.",
                priority = "Alta",
                source = "Regla tecnica MAG/SFE",
                minHumidityPercent = 85,
                minTempC = 24
            )
        )
        val sheet = CropTechnicalSheet(
            cropType = cropType,
            sourceSummary = commonSource,
            generalManagement = listOf("Mantener camas drenadas.", "Evitar humedad foliar prolongada.", "Cuidar floracion, polinizacion y sanidad."),
            stages = stages,
            criticalFactors = listOf("Humedad", "polinizacion", "enfermedades foliares", "calidad de cosecha"),
            suggestedPractices = tasks.map { it.title },
            fieldNotes = listOf("Registrar incidencia por cama.", "Comparar rendimiento por ventana de siembra.")
        )
        return CropProfile(cropType, varieties.map { CropVariety(it, cycleDays, "Ciclo ajustable por clima.") }, stages, listOf(CropRiskProfile("Hongos foliares", listOf("humedad alta", "calor"), "Muestrear hojas y frutos.")), tasks, rules, sheet)
    }

    private fun plantainCrop(): CropProfile {
        val stages = listOf(
            PhenologicalStage("establishment", "Establecimiento", 0, 60, listOf("semilla", "drenaje", "sombra")),
            PhenologicalStage("growth", "Crecimiento", 61, 210, listOf("nutricion", "deshije", "sanidad")),
            PhenologicalStage("flowering", "Emision y llenado", 211, 330, listOf("agua", "viento", "sigatoka")),
            PhenologicalStage("harvest", "Cosecha escalonada", 331, 420, listOf("edad racimo", "calidad", "renovacion"))
        )
        val tasks = listOf(
            StageTaskTemplate(0, TaskType.Planting, "Establecer material sano"),
            StageTaskTemplate(45, TaskType.Fertilization, "Nutricion de arranque"),
            StageTaskTemplate(90, TaskType.Monitoring, "Monitoreo de hojas y drenaje"),
            StageTaskTemplate(180, TaskType.Weeding, "Manejo de malezas y deshije"),
            StageTaskTemplate(330, TaskType.Harvest, "Programar cosecha escalonada")
        )
        val sheet = CropTechnicalSheet(
            cropType = CropType.Plantain,
            sourceSummary = commonSource,
            generalManagement = listOf("Usar material vegetativo sano.", "Mantener nutricion y drenaje.", "Monitorear enfermedades foliares."),
            stages = stages,
            criticalFactors = listOf("Drenaje", "viento", "nutricion", "sanidad foliar"),
            suggestedPractices = tasks.map { it.title },
            fieldNotes = listOf("Registrar deshije y numero de hojas funcionales.", "Comparar racimos por lote y fecha.")
        )
        return CropProfile(CropType.Plantain, listOf(CropVariety("Curare", 365, "Ciclo anual segun manejo.")), stages, listOf(CropRiskProfile("Sanidad foliar", listOf("humedad alta"), "Revisar hojas jovenes y drenaje.")), tasks, baseRules(CropType.Plantain), sheet)
    }

    private fun baseRules(cropType: CropType) = listOf(
        AgronomicRule(
            id = "${cropType.name}_low_moisture",
            cropType = cropType,
            title = "Priorizar humedad del suelo",
            recommendation = "La humedad estimada es baja y no hay lluvia suficiente; revisar suelo antes de programar riego.",
            priority = "Media",
            source = "Regla tecnica parametrizable",
            maxRainMm = 5,
            maxSoilMoisture = 22.0
        ),
        AgronomicRule(
            id = "${cropType.name}_low_vigor",
            cropType = cropType,
            title = "Verificar bajo vigor del cultivo",
            recommendation = "El indice de vigor esta bajo para la parcela; validar nutricion, plagas, malezas o drenaje.",
            priority = "Alta",
            source = "Regla tecnica parametrizable",
            maxNdvi = 0.42
        ),
        AgronomicRule(
            id = "${cropType.name}_no_monitoring",
            cropType = cropType,
            title = "Programar monitoreo de campo",
            recommendation = "No hay suficientes observaciones recientes; levantar datos de etapa, sintomas y estado general.",
            priority = "Media",
            source = "Regla operativa parametrizable",
            requiredActivityGapDays = 21,
            activityType = ActivityType.Labor
        )
    )
}
