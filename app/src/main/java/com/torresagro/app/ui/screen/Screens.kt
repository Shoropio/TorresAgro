package com.torresagro.app.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.torresagro.app.R
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.torresagro.app.ui.map.EsriWorldImageryTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import com.torresagro.app.domain.model.*
import com.torresagro.app.ui.component.ClickableCard
import com.torresagro.app.ui.component.InfoCard
import com.torresagro.app.ui.component.SectionTitle
import com.torresagro.app.ui.util.AreaCalculator
import com.torresagro.app.ui.util.captureCurrentLocation
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: AppUiState,
    onCompleteTask: (String) -> Unit,
    onAddParcel: () -> Unit,
    onAddActivity: () -> Unit,
    onAlertsClick: () -> Unit,
    onOpenAgriMap: () -> Unit,
    onRefreshWeather: (String) -> Unit,
    onRefreshCurrentLocationWeather: (String, Double, Double) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val requestCurrentLocationWeather: () -> Unit = {
        scope.launch {
            captureCurrentLocation(context)?.let { coords ->
                onRefreshCurrentLocationWeather("Ubicación actual", coords.first, coords.second)
            } ?: run {
                state.parcels.firstOrNull()?.id?.let(onRefreshWeather)
            }
        }
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            scope.launch {
                captureCurrentLocation(context)?.let { coords ->
                    onRefreshCurrentLocationWeather("Ubicación actual", coords.first, coords.second)
                }
            }
        } else if (state.currentLocationWeather?.online != true) {
            state.parcels.firstOrNull()?.id?.let(onRefreshWeather)
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

    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            scope.launch {
                requestCurrentLocationWeather()
                kotlinx.coroutines.delay(1000)
                isRefreshing = false
            }
        },
        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.hello_farmer),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                )
                Text(
                    text = stringResource(R.string.welcome_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(
                    title = stringResource(R.string.nav_parcels),
                    value = state.parcels.size.toString(),
                    supporting = stringResource(R.string.parcels_subtitle),
                    modifier = Modifier.weight(1f),
                    accent = Color(0xFF4CAF50)
                )
                InfoCard(
                    title = stringResource(R.string.alerts_title),
                    value = state.alerts.size.toString(),
                    supporting = stringResource(R.string.detected_risks),
                    modifier = Modifier.weight(1f),
                    accent = if (state.alerts.any { it.severity == com.torresagro.app.domain.model.AlertSeverity.Critical || it.severity == com.torresagro.app.domain.model.AlertSeverity.High }) Color.Red else Color(0xFFFFC107),
                    onClick = onAlertsClick
                )
            }
        }

        if (state.alerts.isNotEmpty()) {
            item {
                SectionTitle(stringResource(R.string.field_alerts))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.alerts.take(3).forEach { alert ->
                        AgroAlertItem(alert)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                stringResource(R.string.weather_in_your_area),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                            state.currentLocationWeather?.let {
                                Text(
                                    it.locationLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                                )
                                Text(
                                    formatWeatherUpdatedAt(it.updatedAtEpochMillis, context),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                                )
                            }
                        }
                        if (state.parcels.isNotEmpty()) {
                            IconButton(
                                onClick = requestCurrentLocationWeather,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditCalendar, // Changed from refresh for simplicity if not imported
                                    contentDescription = stringResource(R.string.update),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    state.currentLocationWeather?.let { weather ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${weather.temperatureC}°",
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-2).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "C",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light),
                                    modifier = Modifier.padding(top = 12.dp, start = 2.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    weather.statusResId?.let { stringResource(it) } ?: weather.status,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    WeatherDetailItem(label = stringResource(R.string.humidity), value = "${weather.humidityPercent}%")
                                    Text("|", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                                    WeatherDetailItem(label = stringResource(R.string.rainfall), value = "${weather.rainfallMm}mm")
                                }
                            }
                        }
                    } ?: run {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.weather_not_available),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(stringResource(R.string.farm_dashboard))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionItem(
                    title = stringResource(R.string.ndvi_map),
                    icon = Icons.Default.Map,
                    onClick = onOpenAgriMap,
                    modifier = Modifier.weight(1f)
                )
                QuickActionItem(
                    title = stringResource(R.string.new_lot),
                    icon = Icons.Default.AddLocation,
                    onClick = onAddParcel,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (state.recommendations.isNotEmpty()) {
            item {
                SectionTitle(stringResource(R.string.smart_recommendations))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(state.recommendations) { rec ->
                        RecommendationCard(rec)
                    }
                }
            }
        }

        if (state.tasks.any { !it.completed }) {
            item {
                SectionTitle(stringResource(R.string.next_tasks))
            }
            items(state.tasks.filter { !it.completed }.take(3), key = { it.id }) { task ->
                TaskMinimalCard(task = task, onCompleteTask = onCompleteTask)
            }
        }

        item {
            SectionTitle(stringResource(R.string.tips_of_the_day))
        }
        items(state.tips.take(2), key = { "${it.cropType.name}-${it.stage}" }) { tip ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${tip.cropType.displayName} • ${tip.stage}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(tip.tip, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
    }
}

@Composable
fun QuickActionItem(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TaskMinimalCard(task: CropTask, onCompleteTask: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    "${task.dueDate} • ${task.priority}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { onCompleteTask(task.id) },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(stringResource(R.string.close_btn), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun ParcelsScreen(
    state: AppUiState,
    onOpenParcel: (String) -> Unit,
    onAddParcel: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(stringResource(R.string.nav_parcels), stringResource(R.string.parcels_subtitle))
        }
        item {
            Button(onClick = onAddParcel, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.add_parcel_btn))
            }
        }
        items(state.parcels, key = { it.id }) { parcel ->
            ClickableCard(
                title = parcel.name,
                subtitle = "${parcel.cropType.displayName} | ${parcel.variety} | ${parcel.sizeHectares} ha",
                extra = stringResource(R.string.sowing_date_label, parcel.sowingDate) + " | " + stringResource(R.string.expected_harvest_label, parcel.expectedHarvestDate),
                onClick = { onOpenParcel(parcel.id) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParcelDetailScreen(
    parcel: Parcel?,
    weather: com.torresagro.app.domain.model.WeatherSnapshot?,
    agriData: com.torresagro.app.domain.model.AgriData?,
    activities: List<ActivityRecord>,
    observations: List<CropObservation>,
    onAddActivity: () -> Unit,
    onEditParcel: () -> Unit,
    onEditActivity: (String) -> Unit,
    onAddObservation: () -> Unit,
    onEditObservation: (String) -> Unit,
    onDeleteParcel: (String) -> Unit,
    onBack: () -> Unit,
    onRefreshWeather: (String) -> Unit,
    onRefreshSatellite: () -> Unit
) {
    if (parcel == null) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.parcel_detail_not_found))
        }
        return
    }

    LaunchedEffect(parcel.id) {
        onRefreshWeather(parcel.id)
        onRefreshSatellite()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.back), modifier = Modifier.rotate(180f)) }
                SectionTitle(parcel.name, "${parcel.cropType.displayName} | ${parcel.variety}")
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.satellite_health_ndvi), style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = if (agriData != null) "${"%.2f".format(agriData.ndvi)}" else stringResource(R.string.not_available_short),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = if ((agriData?.ndvi ?: 0.0) > 0.6) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                    VerticalDivider(modifier = Modifier.height(40.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.soil_moisture), style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = if (agriData != null) "${"%.1f".format(agriData.soilMoisture)}%" else stringResource(R.string.not_available_short),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        item {
            SectionTitle(stringResource(R.string.forecast_16_days))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(weather?.forecast16Days ?: emptyList()) { forecast ->
                    Card(
                        modifier = Modifier.width(100.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(forecast.date.split("-").last(), style = MaterialTheme.typography.labelSmall)
                            if (forecast.conditionResId != null) {
                                Text(stringResource(forecast.conditionResId), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            } else if (forecast.condition.isNotBlank()) {
                                Text(forecast.condition, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text("${forecast.tempMax.toInt()}°", fontWeight = FontWeight.Bold)
                            Text("${forecast.tempMin.toInt()}°", style = MaterialTheme.typography.bodySmall)
                            Text("${forecast.rainMm}mm", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        if (agriData?.pestPredictions?.isNotEmpty() == true) {
            item {
                SectionTitle(stringResource(R.string.pest_prediction_title))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    agriData.pestPredictions.forEach { pest ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (pest.probability > 0.6) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(pest.pestName, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${(pest.probability * 100).toInt()}%",
                                        color = if (pest.probability > 0.6) Color.Red else Color.Gray,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                if (pest.description.isNotBlank()) {
                                    Text(pest.description, style = MaterialTheme.typography.bodySmall)
                                }
                                if (pest.preventiveAction.isNotBlank()) {
                                    Text(
                                        stringResource(R.string.preventive_action_label, pest.preventiveAction),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (agriData?.historicalGrids?.isNotEmpty() == true) {
            item {
                SectionTitle(stringResource(R.string.historical_precip_title))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(agriData.historicalGrids) { grid ->
                        Card(
                            modifier = Modifier.width(120.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(grid.date, style = MaterialTheme.typography.labelSmall)
                                Text("${grid.precipitation}mm", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("${grid.tempMax.toInt()}° / ${grid.tempMin.toInt()}°", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onRefreshWeather(parcel.id); onRefreshSatellite() }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.update_data_weather_sat))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onEditParcel, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.edit_parcel_btn))
                    }
                    OutlinedButton(
                        onClick = { onDeleteParcel(parcel.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.delete_btn))
                    }
                }
            }
        }
        if (parcel.boundary.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            MapView(ctx).apply {
                                setTileSource(EsriWorldImageryTileSource)
                                controller.setZoom(16.0)
                                val pts = parcel.boundary.map { GeoPoint(it.first, it.second) }
                                controller.setCenter(pts.first())
                                val polygon = Polygon(this)
                                polygon.points = pts
                                polygon.fillPaint.color = 0x444CAF50.toInt()
                                polygon.outlinePaint.color = 0xFF4CAF50.toInt()
                                polygon.outlinePaint.strokeWidth = 4f
                                overlays.add(polygon)
                                setMultiTouchControls(false) // Disable interaction for preview
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        item {
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.location_label, parcel.locationName))
                    Text(stringResource(R.string.size_label, parcel.sizeHectares.toString()))
                    Text(stringResource(R.string.sowing_date_label, parcel.sowingDate))
                    Text(stringResource(R.string.expected_harvest_label, parcel.expectedHarvestDate))
                    if (parcel.latitude != null && parcel.longitude != null) {
                        Text(stringResource(R.string.gps_coords, parcel.latitude, parcel.longitude))
                    }
                    Text(if (parcel.offlinePendingSync) stringResource(R.string.sync_pending) else stringResource(R.string.sync_done))
                }
            }
        }
        item { SectionTitle(stringResource(R.string.activity_history)) }
        items(activities, key = { it.id }) { activity ->
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(activity.activityType.label, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.date_format_label) + ": ${activity.date}")
                    Text(stringResource(R.string.cost_label) + ": ₡${activity.cost} | " + stringResource(R.string.quantity_label) + ": ${activity.quantity}")
                    Text(activity.notes)
                    activity.photoUri?.let { photoUri ->
                        AsyncImage(
                            model = photoUri,
                            contentDescription = stringResource(R.string.activity_photo_desc),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(top = 4.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Button(
                        onClick = { onEditActivity(activity.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.edit_activity_title))
                    }
                }
            }
        }
        item { SectionTitle(stringResource(R.string.crop_monitoring)) }
        items(observations, key = { it.id }) { observation ->
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${observation.cropStage} | ${observation.generalStatus}", fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.date_format_label) + ": ${observation.date}")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        observation.symptoms.forEach { symptom ->
                            AssistChip(onClick = {}, label = { Text(symptom) })
                        }
                    }
                    Text(observation.recommendation)
                    observation.photoUri?.let { photoUri ->
                        AsyncImage(
                            model = photoUri,
                            contentDescription = stringResource(R.string.monitoring_photo_label),
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Button(
                        onClick = { onEditObservation(observation.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.edit_observation_title))
                    }
                }
            }
        }
    }
}

@Composable
fun TasksScreen(
    state: AppUiState,
    onCompleteTask: (String) -> Unit,
    onAddTask: () -> Unit,
    onEditTask: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(stringResource(R.string.agricultural_calendar), stringResource(R.string.agricultural_calendar_desc))
        }
        item {
            Button(onClick = onAddTask, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.new_task))
            }
        }
        items(state.tasks, key = { it.id }) { task ->
            TaskCard(task = task, onCompleteTask = onCompleteTask, onEditTask = onEditTask)
        }
    }
}

@Composable
private fun TaskCard(task: CropTask, onCompleteTask: (String) -> Unit, onEditTask: (String) -> Unit) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(task.title, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.date_format_label) + ": ${task.dueDate}")
            Text(stringResource(R.string.task_type, task.taskType.label))
            Text(stringResource(R.string.task_priority, task.priority, if (task.completed) stringResource(R.string.task_completed) else stringResource(R.string.task_pending)))
            Text(stringResource(R.string.task_reminder, if (task.reminderEnabled) stringResource(R.string.active) else stringResource(R.string.inactive)))
            if (!task.completed) {
                Button(onClick = { onCompleteTask(task.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.complete_task))
                }
            }
            Button(onClick = { onEditTask(task.id) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.edit_task))
            }
        }
    }
}

@Composable
fun InventoryScreen(state: AppUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionTitle(stringResource(R.string.inventory_and_supplies), stringResource(R.string.inventory_desc))
        }
        items(state.inventory, key = { it.id }) { item ->
            val lowStock = item.stock <= item.minimumStock
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (lowStock) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f) 
                                     else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).background(
                            if (lowStock) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            CircleShape
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SquareFoot, // Placeholder icon
                            contentDescription = null,
                            tint = if (lowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(R.string.current_stock_label, item.stock.toString(), item.unit),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (lowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (lowStock) {
                            Text(
                                stringResource(R.string.restock_soon, item.minimumStock.toString()),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                stringResource(R.string.suggested_minimum, item.minimumStock.toString(), item.unit),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportsScreen(state: AppUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionTitle(stringResource(R.string.production_profitability), stringResource(R.string.analysis_desc))
        }
        items(state.harvests, key = { it.parcelId }) { report ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(report.cropType.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                            Text(stringResource(R.string.harvest_label, report.harvestedKg), style = MaterialTheme.typography.labelMedium)
                        }
                        Box(
                            modifier = Modifier.background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(stringResource(R.string.profitable), color = Color(0xFF2E7D32), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(stringResource(R.string.invested_cost), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₡${report.totalCost}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(stringResource(R.string.net_profit), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₡${report.profit}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherDetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun AgroAlertItem(alert: com.torresagro.app.domain.model.AgroAlert) {
    val color = when (alert.severity) {
        com.torresagro.app.domain.model.AlertSeverity.Critical -> Color.Red
        com.torresagro.app.domain.model.AlertSeverity.High -> Color(0xFFE65100)
        com.torresagro.app.domain.model.AlertSeverity.Medium -> Color(0xFFFFB300)
        com.torresagro.app.domain.model.AlertSeverity.Low -> Color(0xFF2E7D32)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Text(alert.message, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun RecommendationCard(rec: com.torresagro.app.domain.model.Recommendation) {
    val icon = when (rec.type) {
        com.torresagro.app.domain.model.RecommendationType.Irrigation -> Icons.Default.SquareFoot // Placeholder
        com.torresagro.app.domain.model.RecommendationType.Sowing -> Icons.Default.AddLocation
        else -> Icons.Default.EditCalendar
    }
    
    Card(
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(rec.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
            }
            Text(rec.description, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun AgriMapScreen(
    state: AppUiState,
    onBack: () -> Unit,
    onOpenParcel: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(EsriWorldImageryTileSource)
                    controller.setZoom(14.0)
                    
                    state.parcels.firstOrNull()?.let { p ->
                        p.latitude?.let { lat -> 
                            p.longitude?.let { lon -> 
                                controller.setCenter(GeoPoint(lat, lon))
                            }
                        }
                    }

                    state.parcels.forEach { parcel ->
                        if (parcel.boundary.isNotEmpty()) {
                            val pts = parcel.boundary.map { GeoPoint(it.first, it.second) }
                            val polygon = Polygon(this)
                            polygon.points = pts
                            
                            val agri = state.parcelAgriData[parcel.id]
                            val ndvi = agri?.ndvi ?: 0.5
                            
                            val color = when {
                                ndvi > 0.7 -> 0x882E7D32
                                ndvi > 0.5 -> 0x884CAF50
                                ndvi > 0.3 -> 0x88FFC107
                                else -> 0x88E65100
                            }
                            
                            polygon.fillPaint.color = color.toInt()
                            polygon.outlinePaint.color = 0xFFFFFFFF.toInt()
                            polygon.outlinePaint.strokeWidth = 3f
                            polygon.title = "${parcel.name}\nNDVI: ${"%.2f".format(ndvi)}"
                            polygon.setOnClickListener { _, _, _ ->
                                onOpenParcel(parcel.id)
                                true
                            }
                            overlays.add(polygon)
                        }
                    }
                    setMultiTouchControls(true)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        SmallFloatingActionButton(
            onClick = onBack,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.back), modifier = Modifier.rotate(180f))
        }

        Card(
            modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.ndvi_legend), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LegendItem(Color(0xFF2E7D32), stringResource(R.string.high_vigor))
                LegendItem(Color(0xFF4CAF50), stringResource(R.string.medium_vigor))
                LegendItem(Color(0xFFE65100), stringResource(R.string.low_vigor))
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

private fun formatWeatherUpdatedAt(updatedAtEpochMillis: Long, context: android.content.Context): String {
    val zone = ZoneId.systemDefault()
    val dateTime = Instant.ofEpochMilli(updatedAtEpochMillis).atZone(zone)
    val today = Instant.now().atZone(zone).toLocalDate()
    val pattern = if (dateTime.toLocalDate() == today) "HH:mm" else "dd/MM HH:mm"
    val formattedTime = dateTime.format(DateTimeFormatter.ofPattern(pattern))
    return context.getString(R.string.updated_at, formattedTime)
}
@Composable
fun AlertsCenterScreen(
    uiState: AppUiState,
    onParcelClick: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(stringResource(R.string.alerts_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.rotate(180f))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.detected_risks),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.alerts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            stringResource(R.string.no_pests_detected),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(uiState.alerts) { alert ->
                    val color = when (alert.severity) {
                        AlertSeverity.Critical -> Color(0xFFD32F2F)
                        AlertSeverity.High -> Color(0xFFF57C00)
                        else -> Color(0xFFFBC02D)
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    alert.severity.name,
                                    color = color,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Text(alert.message, style = MaterialTheme.typography.bodyLarge)
                            
                            // Encontrar a qué parcela pertenece si es posible (basado en el mensaje por ahora o ID)
                            // Para esta demo, permitimos ir a las parcelas que podrían tener el problema
                            TextButton(
                                onClick = { 
                                    // Navegar a la primera parcela que coincida o una genérica
                                    uiState.parcels.firstOrNull()?.id?.let(onParcelClick)
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(stringResource(R.string.enter_panel))
                            }
                        }
                    }
                }
            }

            item {
                SectionTitle(stringResource(R.string.smart_recommendations))
            }

            items(uiState.recommendations) { rec ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.TipsAndUpdates,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(rec.title, fontWeight = FontWeight.Bold)
                            Text(rec.description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
