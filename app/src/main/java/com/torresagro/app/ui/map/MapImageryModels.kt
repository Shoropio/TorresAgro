package com.torresagro.app.ui.map

enum class ImageryLayerMode(val label: String) {
    Automatic("Automatico"),
    OpenAerialMap("OpenAerialMap"),
    EsriWorldImagery("Esri World Imagery")
}

data class ImageryMetadata(
    val source: String,
    val provider: String?,
    val date: String?,
    val resolution: String?,
    val title: String? = null,
    val license: String? = null
) {
    fun summary(): String = buildList {
        add(source)
        provider?.takeIf { it.isNotBlank() }?.let { add(it) }
        date?.takeIf { it.isNotBlank() }?.let { add(it) }
        resolution?.takeIf { it.isNotBlank() }?.let { add(it) }
    }.joinToString(" | ")
}

data class OpenAerialMapLayer(
    val tileTemplate: String,
    val metadata: ImageryMetadata
)
