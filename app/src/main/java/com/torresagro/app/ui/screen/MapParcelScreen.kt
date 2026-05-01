package com.torresagro.app.ui.screen

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.torresagro.app.R
import com.torresagro.app.data.firebase.AnalyticsTracker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.torresagro.app.ui.map.ImageryLayerMode
import com.torresagro.app.ui.map.ImageryMetadata
import com.torresagro.app.ui.map.ImageryMetadataService
import com.torresagro.app.ui.map.OpenAerialMapLayer
import com.torresagro.app.ui.util.AreaCalculator
import com.torresagro.app.ui.util.captureCurrentLocation
import com.torresagro.app.ui.util.isLocationEnabled
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelMapScreen(
    initialPoints: List<Pair<Double, Double>> = emptyList(),
    onConfirm: (List<Pair<Double, Double>>, Double) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var points by remember { mutableStateOf(initialPoints.map { LatLng(it.first, it.second) }) }
    val area = remember(points) { AreaCalculator.calculateHectares(points.map { it.latitude to it.longitude }) }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            initialPoints.firstOrNull()?.let { LatLng(it.first, it.second) } ?: LatLng(10.35, -83.84),
            16f
        )
    }

    var selectedLayerMode by remember { mutableStateOf(ImageryLayerMode.Automatic) }
    var openAerialMapLayer by remember { mutableStateOf<OpenAerialMapLayer?>(null) }
    var activeMetadata by remember { mutableStateOf<ImageryMetadata?>(null) }
    var metadataLoading by remember { mutableStateOf(false) }
    var metadataJob by remember { mutableStateOf<Job?>(null) }
    var lastMetadataRequestKey by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            if (!isLocationEnabled(context)) {
                AnalyticsTracker.logMapLocationRequest(context, "permission_result", "location_disabled", points.size)
                Toast.makeText(context, R.string.location_disabled_message, Toast.LENGTH_LONG).show()
            } else {
                scope.launch {
                    val coords = captureCurrentLocation(context)
                    if (coords != null) {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(coords.first, coords.second), 17f))
                        AnalyticsTracker.logMapLocationRequest(context, "permission_result", "success", points.size)
                    }
                }
            }
        }
    }

    fun centerMapOnCurrentLocation() {
        if (!isLocationEnabled(context)) {
            Toast.makeText(context, R.string.location_disabled_message, Toast.LENGTH_LONG).show()
            return
        }
        scope.launch {
            val coords = captureCurrentLocation(context)
            if (coords != null) {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(coords.first, coords.second), 17f))
            } else {
                Toast.makeText(context, R.string.location_not_found_message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun loadMetadataFor(point: LatLng, mode: ImageryLayerMode = selectedLayerMode) {
        val requestKey = "${mode.name}:${"%.4f".format(point.latitude)}:${"%.4f".format(point.longitude)}"
        if (requestKey == lastMetadataRequestKey) return
        lastMetadataRequestKey = requestKey
        metadataJob?.cancel()
        metadataJob = scope.launch {
            metadataLoading = true
            val oamLayer = if (mode != ImageryLayerMode.EsriWorldImagery) {
                ImageryMetadataService.findOpenAerialMapLayer(point.latitude, point.longitude)
            } else {
                null
            }
            openAerialMapLayer = oamLayer
            activeMetadata = when (mode) {
                ImageryLayerMode.OpenAerialMap -> oamLayer?.metadata
                ImageryLayerMode.Automatic -> oamLayer?.metadata
                    ?: ImageryMetadataService.findEsriMetadata(point.latitude, point.longitude)
                ImageryLayerMode.EsriWorldImagery ->
                    ImageryMetadataService.findEsriMetadata(point.latitude, point.longitude)
            }
            metadataLoading = false
        }
    }

    LaunchedEffect(Unit) {
        val center = cameraPositionState.position.target
        loadMetadataFor(center)
        if (isLocationEnabled(context)) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_title_free)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { if (points.isNotEmpty()) points = points.dropLast(1) }) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = stringResource(R.string.undo))
                    }
                    IconButton(onClick = { points = emptyList() }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear))
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LayerModeSelector(
                        selected = selectedLayerMode,
                        onSelected = {
                            selectedLayerMode = it
                            loadMetadataFor(cameraPositionState.position.target, it)
                        }
                    )
                    ImageryMetadataPanel(
                        metadata = activeMetadata,
                        loading = metadataLoading,
                        usingFallback = selectedLayerMode == ImageryLayerMode.Automatic && openAerialMapLayer == null
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.calculated_area), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                stringResource(R.string.hectares_format, area),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                stringResource(R.string.points_marked, points.size),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (points.size < 3) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { onConfirm(points.map { it.latitude to it.longitude }, area) },
                            enabled = points.size >= 3,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Done, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.confirm_btn))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = MapType.SATELLITE,
                    isMyLocationEnabled = isLocationEnabled(context)
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false,
                    compassEnabled = true
                ),
                onMapClick = { latLng ->
                    points = points + latLng
                    if (points.size == 1) loadMetadataFor(latLng)
                }
            ) {
                points.forEachIndexed { index, latLng ->
                    Marker(
                        state = MarkerState(position = latLng),
                        title = context.getString(R.string.point_index, index + 1)
                    )
                }
                
                if (points.size >= 2) {
                    Polygon(
                        points = points,
                        fillColor = Color(0x444CAF50),
                        strokeColor = Color(0xFF4CAF50),
                        strokeWidth = 4f
                    )
                }
            }

            // FABs for Map Controls
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { if (points.isNotEmpty()) points = points.dropLast(1) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = stringResource(R.string.undo))
                }

                FloatingActionButton(
                    onClick = { centerMapOnCurrentLocation() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = stringResource(R.string.my_location))
                }
            }
        }
    }
}

@Composable
private fun LayerModeSelector(
    selected: ImageryLayerMode,
    onSelected: (ImageryLayerMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ImageryLayerMode.entries.forEach { mode ->
            FilterChip(
                selected = selected == mode,
                onClick = { onSelected(mode) },
                label = { Text(mode.label, maxLines = 1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ImageryMetadataPanel(
    metadata: ImageryMetadata?,
    loading: Boolean,
    usingFallback: Boolean
) {
    val message = when {
        loading -> "Consultando metadata de imagen..."
        metadata != null -> metadata.summary()
        else -> "Metadata de imagen no disponible para este punto."
    }
    AssistChip(
        onClick = {},
        label = {
            Text(
                if (usingFallback && metadata != null) "$message | fallback Esri" else message,
                maxLines = 2
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}
