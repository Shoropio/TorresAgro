package com.torresagro.app.ui.map

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex

class OpenAerialMapTileSource(
    private val tileTemplate: String
) : OnlineTileSourceBase(
    "OpenAerialMap",
    0,
    23,
    256,
    "",
    arrayOf(tileTemplate)
) {
    override fun getTileURLString(tile: Long): String {
        val zoom = MapTileIndex.getZoom(tile).toString()
        val x = MapTileIndex.getX(tile).toString()
        val y = MapTileIndex.getY(tile).toString()
        return tileTemplate
            .replace("{z}", zoom)
            .replace("{x}", x)
            .replace("{y}", y)
    }
}
