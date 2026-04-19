package com.torresagro.app.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.torresagro.app.ui.map.EsriWorldImageryTileSource
import com.torresagro.app.ui.util.AreaCalculator
import com.torresagro.app.ui.util.captureCurrentLocation
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
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
    val scope = rememberCoroutineScope()
    
    // Initialize osmdroid configuration
    val userAgent = context.packageName
    Configuration.getInstance().userAgentValue = userAgent
    Configuration.getInstance().cacheMapTileCount = 12

    var points by remember { mutableStateOf(initialPoints.map { GeoPoint(it.first, it.second) }) }
    var currentCenter by remember { mutableStateOf<GeoPoint?>(initialPoints.firstOrNull()?.let { GeoPoint(it.first, it.second) }) }
    val area = remember(points) { AreaCalculator.calculateHectares(points.map { it.latitude to it.longitude }) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            scope.launch {
                captureCurrentLocation(context)?.let { coords ->
                    currentCenter = GeoPoint(coords.first, coords.second)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapear Parcela (Libre)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { if (points.isNotEmpty()) points = points.dropLast(1) }) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Deshacer")
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
                    setTileSource(EsriWorldImageryTileSource)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)
                    minZoomLevel = 3.0
                    maxZoomLevel = 19.0
                    // Ensure the map can load tiles online
                    isVerticalMapRepetitionEnabled = false
                    isHorizontalMapRepetitionEnabled = false
                    if (points.isNotEmpty()) {
                        controller.setCenter(points.first())
                    } else if (currentCenter != null) {
                        controller.setCenter(currentCenter)
                    } else {
                        controller.setCenter(GeoPoint(19.4326, -99.1332))
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            update = { mapView ->
                if (points.isNotEmpty()) {
                    mapView.controller.animateTo(points.first())
                } else {
                    currentCenter?.let { mapView.controller.animateTo(it) }
                }
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
