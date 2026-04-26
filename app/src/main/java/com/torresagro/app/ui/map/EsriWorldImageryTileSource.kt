package com.torresagro.app.ui.map

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex

object EsriWorldImageryTileSource : OnlineTileSourceBase(
    "EsriWorldImagery",
    0,
    23,
    256,
    ".jpg",
    arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/")
) {
    override fun getTileURLString(tile: Long): String {
        val zoom = MapTileIndex.getZoom(tile)
        val x = MapTileIndex.getX(tile)
        val y = MapTileIndex.getY(tile)
        return "${getBaseUrl()}$zoom/$y/$x${imageFilenameEnding()}"
    }
}
