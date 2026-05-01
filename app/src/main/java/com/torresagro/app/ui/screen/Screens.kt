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
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.torresagro.app.ui.util.LunarCalendarUtils
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.torresagro.app.ui.map.EsriWorldImageryTileSource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.torresagro.app.domain.model.*
import com.torresagro.app.ui.component.ClickableCard
import com.torresagro.app.ui.component.EmptyStateCard
import com.torresagro.app.ui.component.InfoCard
import com.torresagro.app.ui.component.SectionTitle
import com.torresagro.app.ui.component.SurfaceStatChip
import com.torresagro.app.ui.theme.AccentGold
import com.torresagro.app.ui.theme.AccentSky
import com.torresagro.app.ui.util.AreaCalculator
import com.torresagro.app.ui.util.captureCurrentLocation
import com.torresagro.app.ui.util.formatCurrencyCrc
import com.torresagro.app.ui.util.formatQuantity
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

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
    val pendingTasks = remember(state.tasks) { state.tasks.count { !it.completed } }
    val nextOpenTasks = remember(state.tasks) { state.tasks.filter { !it.completed }.take(3) }
    var requestedInitialWeather by rememberSaveable { mutableStateOf(false) }
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

    LaunchedEffect(state.parcels.isNotEmpty(), state.currentLocationWeather?.online) {
        if (!requestedInitialWeather &&
            state.parcels.isNotEmpty() &&
            state.currentLocationWeather?.online != true
        ) {
            requestedInitialWeather = true
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SectionTitle(
                    title = stringResource(R.string.hello_farmer),
                    subtitle = stringResource(R.string.welcome_message)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SurfaceStatChip(
                        label = "Tareas abiertas",
                        value = pendingTasks.toString(),
                        icon = Icons.Default.EditCalendar,
                        modifier = Modifier.weight(1f),
                        accent = AccentSky
                    )
                    SurfaceStatChip(
                        label = "Clima",
                        value = state.currentLocationWeather?.temperatureC?.let { "${it}C" } ?: "--",
                        icon = Icons.Default.Refresh,
                        modifier = Modifier.weight(1f),
                        accent = AccentGold
                    )
                }
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
                        key(alert.id) {
                            AgroAlertItem(alert)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.update),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    state.currentLocationWeather?.let { weather ->
                        var showForecast16Days by rememberSaveable { mutableStateOf(false) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WeatherConditionIcon(
                                    conditionResId = weather.statusResId,
                                    conditionLabel = weather.status,
                                    modifier = Modifier.size(52.dp)
                                )
                                Spacer(Modifier.width(12.dp))
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
                        if (weather.forecast16Days.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pronostico de 16 dias",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { showForecast16Days = !showForecast16Days }) {
                                    Text(if (showForecast16Days) "Ocultar" else "Mostrar")
                                }
                            }
                            if (showForecast16Days) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(top = 2.dp)
                                ) {
                                    items(
                                        items = weather.forecast16Days.take(16),
                                        key = { it.date }
                                    ) { forecast ->
                                        CompactForecastChip(forecast)
                                    }
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
                QuickActionItem(
                    title = stringResource(R.string.activity_action),
                    icon = Icons.Default.EditCalendar,
                    onClick = onAddActivity,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            LunarPhaseCard()
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
            items(nextOpenTasks, key = { it.id }) { task ->
                TaskMinimalCard(task = task, onCompleteTask = onCompleteTask)
            }
        } else {
            item {
                EmptyStateCard(
                    title = "Agenda controlada",
                    description = "No hay tareas pendientes por ahora. Puedes registrar la siguiente actividad cuando la necesites.",
                    icon = Icons.Default.EditCalendar
                )
            }
        }

        item {
            SectionTitle(stringResource(R.string.tips_of_the_day))
        }
        items(state.tips.take(2), key = { "${it.cropType.name}-${it.stage}" }) { tip ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
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

@Composable
fun QuickActionItem(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SurfaceStatChip(
                    label = "Parcelas",
                    value = state.parcels.size.toString(),
                    icon = Icons.Default.Map,
                    modifier = Modifier.weight(1f),
                    accent = AccentSky
                )
                SurfaceStatChip(
                    label = "Alertas",
                    value = state.alerts.size.toString(),
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f),
                    accent = AccentGold
                )
            }
        }
        item {
            Button(onClick = onAddParcel, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                Text(stringResource(R.string.add_parcel_btn))
            }
        }
        if (state.parcels.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Sin parcelas registradas",
                    description = "Crea tu primera parcela para empezar a monitorear clima, actividades y rendimiento.",
                    icon = Icons.Default.AddLocation,
                    actionLabel = stringResource(R.string.add_parcel_btn),
                    onAction = onAddParcel
                )
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
    onAddTask: () -> Unit,
    onAddActivity: () -> Unit,
    onEditParcel: () -> Unit,
    onEditActivity: (String) -> Unit,
    onAddObservation: () -> Unit,
    onEditObservation: (String) -> Unit,
    onDeleteParcel: (String) -> Unit,
    onBack: () -> Unit,
    onRefreshWeather: (String) -> Unit,
    onRefreshSatellite: () -> Unit,
    onGenerateReport: (Parcel, AgriData?) -> Unit
) {
    if (parcel == null) {
        Column(modifier = Modifier.padding(16.dp)) {
            EmptyStateCard(
                title = "Parcela no disponible",
                description = stringResource(R.string.parcel_detail_not_found),
                icon = Icons.Default.Map
            )
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SurfaceStatChip(
                    label = "Tamano",
                    value = "${parcel.sizeHectares} ha",
                    icon = Icons.Default.SquareFoot,
                    modifier = Modifier.weight(1f),
                    accent = AccentSky
                )
                SurfaceStatChip(
                    label = "Actividades",
                    value = activities.size.toString(),
                    icon = Icons.Default.EditCalendar,
                    modifier = Modifier.weight(1f),
                    accent = AccentGold
                )
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.satellite_health_ndvi), style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = if (agriData != null && agriData.ndvi > 0) "${"%.2f".format(agriData.ndvi)}" else stringResource(R.string.not_available_short),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = if ((agriData?.ndvi ?: 0.0) > 0.6) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                    VerticalDivider(modifier = Modifier.height(40.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.soil_moisture), style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = if (agriData != null && agriData.soilMoisture > 0) "${"%.1f".format(agriData.soilMoisture)}%" else stringResource(R.string.not_available_short),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        item {
            CostaRicaFieldReadinessCard(parcel = parcel, weather = weather, agriData = agriData)
        }

        item {
            ParcelWorkflowCard(
                onAddTask = onAddTask,
                onAddActivity = onAddActivity,
                onAddObservation = onAddObservation
            )
        }

        item {
            SectionTitle(stringResource(R.string.forecast_16_days))
            if (weather?.forecast16Days.isNullOrEmpty()) {
                EmptyStateCard(
                    title = "Pronostico pendiente",
                    description = if (parcel.latitude == null || parcel.longitude == null) {
                        "Captura GPS o ubica la parcela en el mapa para traer clima por coordenadas de Costa Rica."
                    } else {
                        "Toca actualizar datos para descargar el pronostico y los iconos del clima."
                    },
                    icon = Icons.Default.Refresh,
                    actionLabel = stringResource(R.string.update),
                    onAction = { onRefreshWeather(parcel.id) }
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    items = weather?.forecast16Days ?: emptyList(),
                    key = { it.date }
                ) { forecast ->
                    Card(
                        modifier = Modifier.width(110.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(forecastDayLabel(forecast.date), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            WeatherConditionIcon(
                                conditionResId = forecast.conditionResId,
                                conditionLabel = forecast.condition,
                                modifier = Modifier.size(28.dp)
                            )
                            Text("${forecast.tempMax.toInt()}°", fontWeight = FontWeight.Bold)
                            Text("${forecast.tempMin.toInt()}°", style = MaterialTheme.typography.bodySmall)
                            Text("${forecast.rainMm}mm", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        }
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
                    items(agriData.historicalGrids, key = { it.date }) { grid ->
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
                Button(
                    onClick = { onRefreshWeather(parcel.id); onRefreshSatellite() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.update_data_weather_sat))
                }
                
                OutlinedButton(
                    onClick = { onGenerateReport(parcel, agriData) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Generar Reporte PDF")
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onEditParcel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                        Text(stringResource(R.string.edit_parcel_btn))
                    }
                    OutlinedButton(
                        onClick = { onDeleteParcel(parcel.id) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
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
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    val pts = remember(parcel.boundary) { parcel.boundary.map { LatLng(it.first, it.second) } }
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(pts.firstOrNull() ?: LatLng(0.0, 0.0), 16f)
                    }
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(mapType = MapType.SATELLITE),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            scrollGesturesEnabled = false,
                            zoomGesturesEnabled = false,
                            tiltGesturesEnabled = false,
                            rotationGesturesEnabled = false
                        )
                    ) {
                        if (pts.size >= 2) {
                            Polygon(
                                points = pts,
                                fillColor = Color(0x444CAF50),
                                strokeColor = Color(0xFF4CAF50),
                                strokeWidth = 4f
                            )
                        }
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(8.dp), border = CardDefaults.outlinedCardBorder()) {
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
        if (activities.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Sin actividades registradas",
                    description = "Documenta labores, costos y evidencias para tener la historia operativa de esta parcela.",
                    icon = Icons.Default.EditCalendar,
                    actionLabel = stringResource(R.string.activity_action),
                    onAction = onAddActivity
                )
            }
        }
        items(activities, key = { it.id }) { activity ->
            Card(shape = RoundedCornerShape(8.dp), border = CardDefaults.outlinedCardBorder()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(activity.activityType.label, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.date_format_label) + ": ${activity.date}")
                    Text(stringResource(R.string.cost_label) + ": " + formatCurrencyCrc(activity.cost) + " | " + stringResource(R.string.quantity_label) + ": ${activity.quantity}")
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.edit_activity_title))
                    }
                }
            }
        }
        item { SectionTitle(stringResource(R.string.crop_monitoring)) }
        if (observations.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Sin observaciones aun",
                    description = "Agrega hallazgos de campo para dejar trazabilidad del estado del cultivo y sus sintomas.",
                    icon = Icons.Default.Warning,
                    actionLabel = stringResource(R.string.crop_monitoring),
                    onAction = onAddObservation
                )
            }
        }
        items(observations, key = { it.id }) { observation ->
            Card(shape = RoundedCornerShape(8.dp), border = CardDefaults.outlinedCardBorder()) {
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SurfaceStatChip(
                    label = "Pendientes",
                    value = state.tasks.count { !it.completed }.toString(),
                    icon = Icons.Default.EditCalendar,
                    modifier = Modifier.weight(1f),
                    accent = AccentGold
                )
                SurfaceStatChip(
                    label = "Completadas",
                    value = state.tasks.count { it.completed }.toString(),
                    icon = Icons.Default.Refresh,
                    modifier = Modifier.weight(1f),
                    accent = AccentSky
                )
            }
        }
        item {
            Button(onClick = onAddTask, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                Text(stringResource(R.string.new_task))
            }
        }
        if (state.tasks.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Calendario sin tareas",
                    description = "Crea recordatorios y labores para mantener el ciclo agricola siempre bajo control.",
                    icon = Icons.Default.EditCalendar,
                    actionLabel = stringResource(R.string.new_task),
                    onAction = onAddTask
                )
            }
        }
        items(state.tasks, key = { it.id }) { task ->
            TaskCard(task = task, onCompleteTask = onCompleteTask, onEditTask = onEditTask)
        }
    }
}

@Composable
private fun TaskCard(task: CropTask, onCompleteTask: (String) -> Unit, onEditTask: (String) -> Unit) {
    val statusColor = if (task.completed) MaterialTheme.colorScheme.primary else AccentGold
    Card(shape = RoundedCornerShape(8.dp), border = CardDefaults.outlinedCardBorder()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(task.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (task.completed) stringResource(R.string.task_completed) else stringResource(R.string.task_pending),
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(stringResource(R.string.date_format_label) + ": ${task.dueDate}")
            Text(stringResource(R.string.task_type, task.taskType.label))
            Text(stringResource(R.string.task_reminder, if (task.reminderEnabled) stringResource(R.string.active) else stringResource(R.string.inactive)))
            if (!task.completed) {
                Button(onClick = { onCompleteTask(task.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                    Text(stringResource(R.string.complete_task))
                }
            }
            OutlinedButton(onClick = { onEditTask(task.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
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
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (lowStock) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f) 
                                     else MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            stringResource(R.string.current_stock_label, formatQuantity(item.stock), item.unit),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (lowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (lowStock) {
                            Text(
                                stringResource(R.string.restock_soon, "${formatQuantity(item.minimumStock)} ${item.unit}"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                stringResource(R.string.suggested_minimum, formatQuantity(item.minimumStock), item.unit),
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
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            Text(formatCurrencyCrc(report.totalCost), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(stringResource(R.string.net_profit), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatCurrencyCrc(report.profit), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CostaRicaFieldReadinessCard(
    parcel: Parcel,
    weather: WeatherSnapshot?,
    agriData: AgriData?
) {
    val readiness = listOf(
        "GPS" to if (parcel.latitude != null && parcel.longitude != null) {
            "Listo para clima por coordenadas"
        } else {
            "Falta capturar ubicacion"
        },
        "Clima" to if (!weather?.forecast16Days.isNullOrEmpty()) {
            "Pronostico de 16 dias cargado"
        } else {
            "Actualizar para ver lluvia e iconos"
        },
        "Satelite" to if (agriData != null && agriData.ndvi > 0) {
            "NDVI y humedad disponibles"
        } else {
            "Pendiente de lectura satelital"
        }
    )
    val checklist = CostaRicaAgroGuide.checklistFor(parcel.cropType).take(3)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Preparacion de campo Costa Rica", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                CostaRicaAgroGuide.zoneHint(parcel.locationName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            readiness.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                }
            }
            HorizontalDivider()
            checklist.forEach { item ->
                Text("- $item", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ParcelWorkflowCard(
    onAddTask: () -> Unit,
    onAddActivity: () -> Unit,
    onAddObservation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Siguiente accion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onAddTask, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                    Text("Tarea")
                }
                OutlinedButton(onClick = onAddActivity, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                    Text("Actividad")
                }
            }
            OutlinedButton(onClick = onAddObservation, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                Text("Monitoreo con foto")
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
private fun CompactForecastChip(forecast: DailyForecast) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.32f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = forecastDayLabel(forecast.date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                fontWeight = FontWeight.Bold
            )
            WeatherConditionIcon(
                conditionResId = forecast.conditionResId,
                conditionLabel = forecast.condition,
                tintOverride = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "${forecast.tempMax.toInt()}°·${forecast.tempMin.toInt()}°",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun WeatherConditionIcon(
    conditionResId: Int?,
    conditionLabel: String,
    modifier: Modifier = Modifier,
    tintOverride: Color? = null
) {
    val iconType = weatherVisualType(conditionResId, conditionLabel)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when (iconType) {
            WeatherVisualType.Clear -> Icon(Icons.Default.WbSunny, contentDescription = null, tint = tintOverride ?: Color(0xFFFFB300), modifier = Modifier.fillMaxSize())
            WeatherVisualType.PartlyCloudy -> {
                Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.fillMaxSize(0.82f).offset(x = (-6).dp, y = (-4).dp))
                Icon(Icons.Default.Cloud, contentDescription = null, tint = tintOverride ?: Color(0xFFCFD8DC), modifier = Modifier.fillMaxSize())
            }
            WeatherVisualType.Cloudy,
            WeatherVisualType.Fog -> Icon(Icons.Default.Cloud, contentDescription = null, tint = tintOverride ?: Color(0xFFCFD8DC), modifier = Modifier.fillMaxSize())
            WeatherVisualType.Rain -> {
                Icon(Icons.Default.Cloud, contentDescription = null, tint = tintOverride ?: Color(0xFFCFD8DC), modifier = Modifier.fillMaxSize())
                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF1E88E5), modifier = Modifier.fillMaxSize(0.42f).offset(y = 9.dp))
            }
            WeatherVisualType.Storm -> {
                Icon(Icons.Default.Cloud, contentDescription = null, tint = tintOverride ?: Color(0xFFB0BEC5), modifier = Modifier.fillMaxSize())
                Icon(Icons.Default.Thunderstorm, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.fillMaxSize(0.62f).offset(y = 3.dp))
            }
            WeatherVisualType.Snow -> {
                Icon(Icons.Default.Cloud, contentDescription = null, tint = tintOverride ?: Color(0xFFCFD8DC), modifier = Modifier.fillMaxSize())
                Icon(Icons.Default.AcUnit, contentDescription = null, tint = Color(0xFF90CAF9), modifier = Modifier.fillMaxSize(0.40f).offset(y = 9.dp))
            }
        }
    }
}

private enum class WeatherVisualType {
    Clear, PartlyCloudy, Cloudy, Rain, Storm, Snow, Fog
}

private fun weatherVisualType(conditionResId: Int?, conditionLabel: String): WeatherVisualType {
    return when {
        conditionResId == R.string.weather_thunderstorm -> WeatherVisualType.Storm
        conditionResId == R.string.weather_rain || conditionResId == R.string.weather_drizzle || conditionResId == R.string.weather_showers -> WeatherVisualType.Rain
        conditionResId == R.string.weather_snow -> WeatherVisualType.Snow
        conditionResId == R.string.weather_fog -> WeatherVisualType.Fog
        conditionResId == R.string.weather_partly_cloudy -> WeatherVisualType.PartlyCloudy
        conditionResId == R.string.weather_clear -> WeatherVisualType.Clear
        conditionLabel.contains("torment", ignoreCase = true) -> WeatherVisualType.Storm
        conditionLabel.contains("lluv", ignoreCase = true) || conditionLabel.contains("chub", ignoreCase = true) -> WeatherVisualType.Rain
        conditionLabel.contains("nieve", ignoreCase = true) -> WeatherVisualType.Snow
        conditionLabel.contains("niebla", ignoreCase = true) -> WeatherVisualType.Fog
        conditionLabel.contains("nublado", ignoreCase = true) || conditionLabel.contains("cloud", ignoreCase = true) -> WeatherVisualType.Cloudy
        else -> WeatherVisualType.PartlyCloudy
    }
}

private fun forecastDayLabel(date: String): String {
    val parsed = runCatching { LocalDate.parse(date) }.getOrNull() ?: return date.takeLast(2)
    return parsed.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "MX"))
        .replace(".", "")
        .lowercase()
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
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
        val cameraPositionState = rememberCameraPositionState {
            val firstParcel = state.parcels.firstOrNull { it.latitude != null && it.longitude != null }
            position = CameraPosition.fromLatLngZoom(
                firstParcel?.let { LatLng(it.latitude!!, it.longitude!!) } ?: LatLng(10.35, -83.84),
                12f
            )
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapType = MapType.SATELLITE),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            state.parcels.forEach { parcel ->
                if (parcel.boundary.isNotEmpty()) {
                    val pts = parcel.boundary.map { LatLng(it.first, it.second) }
                    val agri = state.parcelAgriData[parcel.id]
                    val ndvi = agri?.ndvi ?: 0.5
                    
                    val color = when {
                        ndvi > 0.7 -> Color(0x882E7D32)
                        ndvi > 0.5 -> Color(0x884CAF50)
                        ndvi > 0.3 -> Color(0x88FFC107)
                        else -> Color(0x88E65100)
                    }
                    
                    Polygon(
                        points = pts,
                        fillColor = color,
                        strokeColor = Color.White,
                        strokeWidth = 3f,
                        clickable = true,
                        onClick = { onOpenParcel(parcel.id) }
                    )
                }
            }
        }
        
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
fun LunarPhaseCard() {
    val currentPhase = remember { LunarCalendarUtils.getMoonPhase() }
    val recommendations = remember(currentPhase) { LunarCalendarUtils.getRecommendations(currentPhase) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Brightness2,
                        contentDescription = null,
                        tint = Color(0xFFFBC02D),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Calendario Lunar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        currentPhase.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Text(
                currentPhase.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            HorizontalDivider(modifier = Modifier.alpha(0.3f))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Recomendaciones Agrícolas:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                recommendations.forEach { recommendation ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                        Text(
                            recommendation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.alpha(0.3f))
            
            val forecast = remember { LunarCalendarUtils.getLunarForecast(8) }
            Text(
                "Planificación Lunar (Próximos días):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                items(forecast) { (date, phase) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.width(60.dp)
                    ) {
                        Text(
                            text = if (date == LocalDate.now()) "Hoy" else date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale("es", "MX")).uppercase().replace(".", ""),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (date == LocalDate.now()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (date == LocalDate.now()) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(phase.symbol, fontSize = 24.sp)
                        Text(
                            phase.label.replace(" ", "\n"),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            maxLines = 2,
                            lineHeight = 9.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
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
    onCreateTask: (String) -> Unit = {},
    onCreateObservation: (String) -> Unit = {},
    onBack: () -> Unit,
    onAlertOpen: (AlertSeverity) -> Unit = {},
    onRecommendationOpen: (RecommendationType) -> Unit = {},
    onScreenViewed: (Int, Int) -> Unit = { _, _ -> }
) {
    LaunchedEffect(uiState.alerts.size, uiState.recommendations.size) {
        onScreenViewed(uiState.alerts.size, uiState.recommendations.size)
    }
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
                SectionTitle(
                    title = stringResource(R.string.alerts_title),
                    subtitle = stringResource(R.string.detected_risks)
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SurfaceStatChip(
                        label = "Alertas",
                        value = uiState.alerts.size.toString(),
                        icon = Icons.Default.Warning,
                        modifier = Modifier.weight(1f),
                        accent = AccentGold
                    )
                    SurfaceStatChip(
                        label = "Recomendaciones",
                        value = uiState.recommendations.size.toString(),
                        icon = Icons.Default.TipsAndUpdates,
                        modifier = Modifier.weight(1f),
                        accent = AccentSky
                    )
                }
            }

            if (uiState.alerts.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "Sin alertas activas",
                        description = stringResource(R.string.no_pests_detected),
                        icon = Icons.Default.TipsAndUpdates
                    )
                }
            } else {
                items(uiState.alerts) { alert ->
                    val targetParcel = uiState.parcels.firstOrNull { parcel ->
                        alert.message.contains(parcel.name, ignoreCase = true)
                    } ?: uiState.parcels.firstOrNull()
                    val color = when (alert.severity) {
                        AlertSeverity.Critical -> Color(0xFFD32F2F)
                        AlertSeverity.High -> Color(0xFFF57C00)
                        else -> Color(0xFFFBC02D)
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        onAlertOpen(alert.severity)
                                        targetParcel?.id?.let(onParcelClick)
                                    },
                                    enabled = targetParcel != null
                                ) {
                                    Text("Ver parcela")
                                }
                                TextButton(
                                    onClick = {
                                        onAlertOpen(alert.severity)
                                        targetParcel?.id?.let(onCreateObservation)
                                    },
                                    enabled = targetParcel != null
                                ) {
                                    Text("Monitorear")
                                }
                                TextButton(
                                    onClick = {
                                        onAlertOpen(alert.severity)
                                        targetParcel?.id?.let(onCreateTask)
                                    },
                                    enabled = targetParcel != null
                                ) {
                                    Text("Crear tarea")
                                }
                            }
                        }
                    }
                }
            }

            item {
                SectionTitle(stringResource(R.string.smart_recommendations))
            }

            items(uiState.recommendations) { rec ->
                val targetParcel = uiState.parcels.firstOrNull()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRecommendationOpen(rec.type) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
                    border = CardDefaults.outlinedCardBorder()
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
                            TextButton(
                                onClick = { targetParcel?.id?.let(onCreateTask) },
                                enabled = targetParcel != null,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Programar")
                            }
                        }
                    }
                }
            }
        }
    }
}

