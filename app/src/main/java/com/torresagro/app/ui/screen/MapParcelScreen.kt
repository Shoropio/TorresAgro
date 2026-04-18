package com.torresagro.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.torresagro.app.ui.util.AreaCalculator
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelMapScreen(
    initialPoints: List<Pair<Double, Double>> = emptyList(),
    onConfirm: (List<Pair<Double, Double>>, Double) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Initialize osmdroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    var points by remember { mutableStateOf(initialPoints.map { GeoPoint(it.first, it.second) }) }
    val area = remember(points) { AreaCalculator.calculateHectares(points.map { it.latitude to it.longitude }) }

    // Tile source for Satellite view (ESRI)
    val satelliteTileSource = XYTileSource(
        "EsriSatellite",
        0, 19, 256, ".jpg",
        arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapear Parcela (Libre)") },
                actions = {
                    IconButton(onClick = { if (points.isNotEmpty()) points = points.dropLast(1) }) {
                        Icon(Icons.Default.Undo, contentDescription = "Deshacer")
                    }
                    IconButton(onClick = { points = emptyList() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Área Calculada", style = MaterialTheme.typography.labelLarge)
                            Text(
                                "%.2f Hectáreas".format(area),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = { onConfirm(points.map { it.latitude to it.longitude }, area) },
                            enabled = points.size >= 3,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Done, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Confirmar")
                        }
                    }
                    Text(
                        "Toca el mapa para marcar los límites. Usando mapas satelitales libres.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { padding ->
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(satelliteTileSource)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)
                    if (points.isNotEmpty()) {
                        controller.setCenter(points.first())
                    } else {
                        controller.setCenter(GeoPoint(18.8, -71.2)) // Localización por defecto (RD)
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            update = { mapView ->
                mapView.overlays.clear()
                
                // Add points/markers
                points.forEachIndexed { index, geoPoint ->
                    val marker = Marker(mapView)
                    marker.position = geoPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Punto ${index + 1}"
                    mapView.overlays.add(marker)
                }
                
                // Add Polygon
                if (points.size >= 2) {
                    val polygon = Polygon(mapView)
                    polygon.points = points
                    polygon.fillPaint.color = 0x444CAF50.toInt()
                    polygon.outlinePaint.color = 0xFF4CAF50.toInt()
                    polygon.outlinePaint.strokeWidth = 4f
                    mapView.overlays.add(polygon)
                }
                
                // Click listener to add points
                val mapEventsReceiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        points = points + p
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint): Boolean = false
                }
                mapView.overlays.add(MapEventsOverlay(mapEventsReceiver))
                
                mapView.invalidate()
            }
        )
    }
}
