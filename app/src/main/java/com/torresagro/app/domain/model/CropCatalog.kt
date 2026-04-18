package com.torresagro.app.domain.model

data class CropStageTemplate(
    val cropType: CropType,
    val varietyExample: String,
    val recommendedCycleDays: Int,
    val stages: List<StageTaskTemplate>,
    val recommendations: List<String>
)

data class StageTaskTemplate(
    val dayOffset: Int,
    val taskType: TaskType,
    val title: String
)

object CropCatalog {
    val templates = listOf(
        CropStageTemplate(
            cropType = CropType.Cassava,
            varietyExample = "Valencia",
            recommendedCycleDays = 300,
            stages = listOf(
                StageTaskTemplate(0, TaskType.SoilPrep, "Preparar suelo con buen drenaje"),
                StageTaskTemplate(1, TaskType.Planting, "Sembrar estacas sanas"),
                StageTaskTemplate(30, TaskType.Weeding, "Primer control de malezas"),
                StageTaskTemplate(45, TaskType.Fertilization, "Aplicar abonado de arranque"),
                StageTaskTemplate(120, TaskType.Monitoring, "Revisar vigor y drenaje"),
                StageTaskTemplate(300, TaskType.Harvest, "Planificar cosecha")
            ),
            recommendations = listOf(
                "Evitar material de siembra con pudricion o lesiones.",
                "Favorecer drenaje para reducir problemas en raiz.",
                "Controlar malezas temprano para evitar competencia."
            )
        ),
        CropStageTemplate(
            cropType = CropType.SweetPotato,
            varietyExample = "Beauregard",
            recommendedCycleDays = 150,
            stages = listOf(
                StageTaskTemplate(0, TaskType.SoilPrep, "Aflojar el suelo y formar camellones"),
                StageTaskTemplate(1, TaskType.Planting, "Sembrar guias vigorosas"),
                StageTaskTemplate(20, TaskType.Irrigation, "Revisar humedad uniforme"),
                StageTaskTemplate(35, TaskType.Weeding, "Control de malezas"),
                StageTaskTemplate(45, TaskType.Fertilization, "Refuerzo nutricional ligero"),
                StageTaskTemplate(150, TaskType.Harvest, "Cosecha de raices comerciales")
            ),
            recommendations = listOf(
                "Evitar encharcamiento para no afectar las raices.",
                "Mantener humedad estable durante el establecimiento.",
                "Usar suelo suelto para favorecer llenado uniforme."
            )
        ),
        CropStageTemplate(
            cropType = CropType.Yam,
            varietyExample = "Diamantes",
            recommendedCycleDays = 240,
            stages = listOf(
                StageTaskTemplate(0, TaskType.SoilPrep, "Preparar lomillos o monticulos"),
                StageTaskTemplate(1, TaskType.Planting, "Sembrar semilla o trozos sanos"),
                StageTaskTemplate(40, TaskType.Weeding, "Limpieza de malezas"),
                StageTaskTemplate(60, TaskType.Fertilization, "Abonado de sosten"),
                StageTaskTemplate(120, TaskType.Monitoring, "Revisar hojas amarillas y drenaje"),
                StageTaskTemplate(240, TaskType.Harvest, "Cosecha escalonada")
            ),
            recommendations = listOf(
                "Priorizar drenaje para proteger la raiz.",
                "Vigilar material de siembra y sanidad del tallo.",
                "Ajustar tutorado o guia segun manejo local."
            )
        ),
        CropStageTemplate(
            cropType = CropType.Corn,
            varietyExample = "Maiz amarillo ICTA",
            recommendedCycleDays = 120,
            stages = listOf(
                StageTaskTemplate(0, TaskType.SoilPrep, "Preparar suelo y surcado"),
                StageTaskTemplate(1, TaskType.Planting, "Siembra con densidad uniforme"),
                StageTaskTemplate(20, TaskType.Weeding, "Control temprano de malezas"),
                StageTaskTemplate(25, TaskType.Fertilization, "Primera fertilizacion"),
                StageTaskTemplate(45, TaskType.Fertilization, "Segunda fertilizacion"),
                StageTaskTemplate(120, TaskType.Harvest, "Cosecha y secado")
            ),
            recommendations = listOf(
                "Fertilizar segun etapa y humedad del suelo.",
                "Vigilar malezas durante el primer tercio del ciclo.",
                "Revisar dano de plagas en hojas y cogollo."
            )
        )
    )
}
