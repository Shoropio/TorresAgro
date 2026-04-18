package com.torresagro.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.views.overlay.Polygon
import com.torresagro.app.domain.model.ActivityRecord
import com.torresagro.app.domain.model.AppUiState
import com.torresagro.app.domain.model.CropObservation
import com.torresagro.app.domain.model.CropTask
import com.torresagro.app.domain.model.Parcel
import com.torresagro.app.ui.component.ClickableCard
import com.torresagro.app.ui.component.InfoCard
import com.torresagro.app.ui.component.SectionTitle
import com.torresagro.app.ui.util.AreaCalculator

@Composable
fun HomeScreen(
    state: AppUiState,
    onCompleteTask: (String) -> Unit,
    onAddParcel: () -> Unit,
    onAddActivity: () -> Unit,
    onRefreshWeather: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Hola, Agricultor",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                )
                Text(
                    text = "Bienvenido a Torres Agro",
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
                    title = "Parcelas",
                    value = state.parcels.size.toString(),
                    supporting = "Lotes registrados",
                    modifier = Modifier.weight(1f),
                    accent = Color(0xFF4CAF50)
                )
                InfoCard(
                    title = "Tareas",
                    value = state.tasks.count { !it.completed }.toString(),
                    supporting = "Pendientes hoy",
                    modifier = Modifier.weight(1f),
                    accent = Color(0xFFFFC107)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Clima actual",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        if (state.parcels.isNotEmpty()) {
                            Button(
                                onClick = { onRefreshWeather(state.parcels.first().id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Actualizar", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    state.weather?.let { weather ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "${weather.temperatureC}°",
                                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Column {
                                Text(
                                    weather.status,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    weather.locationLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } ?: run {
                        Text(
                            "Crea una parcela para ver el clima local.",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        item {
            SectionTitle("Acciones rápidas")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionItem(
                    title = "Añadir Lote",
                    icon = Icons.Default.AddLocation,
                    onClick = onAddParcel,
                    modifier = Modifier.weight(1f)
                )
                QuickActionItem(
                    title = "Actividad",
                    icon = Icons.Default.EditCalendar,
                    onClick = onAddActivity,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (state.tasks.any { !it.completed }) {
            item {
                SectionTitle("Próximas tareas")
            }
            items(state.tasks.filter { !it.completed }.take(3), key = { it.id }) { task ->
                TaskMinimalCard(task = task, onCompleteTask = onCompleteTask)
            }
        }

        item {
            SectionTitle("Consejos del día")
        }
        items(state.tips.take(2), key = { "${it.cropType.name}-${it.stage}" }) { tip ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                Text("Cerrar", style = MaterialTheme.typography.labelSmall)
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
            SectionTitle("Parcelas", "Cada lote guarda ubicacion, variedad, fecha de siembra e historial.")
        }
        item {
            Button(onClick = onAddParcel, modifier = Modifier.fillMaxWidth()) {
                Text("Agregar parcela")
            }
        }
        items(state.parcels, key = { it.id }) { parcel ->
            ClickableCard(
                title = parcel.name,
                subtitle = "${parcel.cropType.displayName} | ${parcel.variety} | ${parcel.sizeHectares} ha",
                extra = "Siembra: ${parcel.sowingDate} | Cosecha estimada: ${parcel.expectedHarvestDate}",
                onClick = { onOpenParcel(parcel.id) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParcelDetailScreen(
    parcel: Parcel?,
    activities: List<ActivityRecord>,
    observations: List<CropObservation>,
    onAddActivity: () -> Unit,
    onEditParcel: () -> Unit,
    onEditActivity: (String) -> Unit,
    onAddObservation: () -> Unit,
    onEditObservation: (String) -> Unit
) {
    if (parcel == null) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Parcela no encontrada")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SectionTitle(parcel.name, "${parcel.cropType.displayName} | ${parcel.variety}") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAddActivity, modifier = Modifier.fillMaxWidth()) {
                    Text("Registrar actividad en esta parcela")
                }
                Button(onClick = onAddObservation, modifier = Modifier.fillMaxWidth()) {
                    Text("Registrar monitoreo")
                }
                Button(onClick = onEditParcel, modifier = Modifier.fillMaxWidth()) {
                    Text("Editar esta parcela")
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
                    val satelliteTileSource = XYTileSource(
                        "EsriSatellite",
                        0, 19, 256, ".jpg",
                        arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/")
                    )
                    AndroidView(
                        factory = { ctx ->
                            MapView(ctx).apply {
                                setTileSource(satelliteTileSource)
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
                    Text("Ubicacion: ${parcel.locationName}")
                    Text("Tamano: ${parcel.sizeHectares} hectareas")
                    Text("Siembra: ${parcel.sowingDate}")
                    Text("Cosecha estimada: ${parcel.expectedHarvestDate}")
                    if (parcel.latitude != null && parcel.longitude != null) {
                        Text("GPS: ${"%.5f".format(parcel.latitude)}, ${"%.5f".format(parcel.longitude)}")
                    }
                    Text(if (parcel.offlinePendingSync) "Pendiente de sincronizar" else "Sincronizada")
                }
            }
        }
        item { SectionTitle("Historial de actividades") }
        items(activities, key = { it.id }) { activity ->
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(activity.activityType.label, fontWeight = FontWeight.Bold)
                    Text("Fecha: ${activity.date}")
                    Text("Costo: $${activity.cost} | Cantidad: ${activity.quantity}")
                    Text(activity.notes)
                    activity.photoUri?.let { photoUri ->
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Foto de actividad",
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
                        Text("Editar actividad")
                    }
                }
            }
        }
        item { SectionTitle("Monitoreo del cultivo") }
        items(observations, key = { it.id }) { observation ->
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${observation.cropStage} | ${observation.generalStatus}", fontWeight = FontWeight.Bold)
                    Text("Fecha: ${observation.date}")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        observation.symptoms.forEach { symptom ->
                            AssistChip(onClick = {}, label = { Text(symptom) })
                        }
                    }
                    Text(observation.recommendation)
                    observation.photoUri?.let { photoUri ->
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Foto de monitoreo",
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Button(
                        onClick = { onEditObservation(observation.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Editar monitoreo")
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
            SectionTitle("Calendario agricola", "Cronograma editable segun cultivo, fecha de siembra y labores pendientes.")
        }
        item {
            Button(onClick = onAddTask, modifier = Modifier.fillMaxWidth()) {
                Text("Nueva tarea")
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
            Text("Fecha: ${task.dueDate}")
            Text("Tipo: ${task.taskType.label}")
            Text("Prioridad: ${task.priority} | ${if (task.completed) "Realizada" else "Pendiente"}")
            Text("Recordatorio: ${if (task.reminderEnabled) "Activo" else "Inactivo"}")
            if (!task.completed) {
                Button(onClick = { onCompleteTask(task.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Completar")
                }
            }
            Button(onClick = { onEditTask(task.id) }, modifier = Modifier.fillMaxWidth()) {
                Text("Editar tarea")
            }
        }
    }
}

@Composable
fun InventoryScreen(state: AppUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle("Inventario e insumos", "Semillas, estacas, fertilizantes, bioinsumos y herramientas.")
        }
        items(state.inventory, key = { it.id }) { item ->
            val lowStock = item.stock <= item.minimumStock
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (lowStock) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.name, fontWeight = FontWeight.Bold)
                    Text("${item.stock} ${item.unit} disponibles")
                    Text("Minimo sugerido: ${item.minimumStock} ${item.unit}")
                    if (lowStock) {
                        Text("Alerta: este insumo esta por agotarse.")
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle("Produccion y rentabilidad", "Comparacion simple entre cultivos y costos por parcela.")
        }
        items(state.harvests, key = { it.parcelId }) { report ->
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(report.cropType.displayName, fontWeight = FontWeight.Bold)
                    Text("Cosechado: ${report.harvestedKg} kg")
                    Text("Costo total: $${report.totalCost}")
                    Text("Ingreso estimado: $${report.estimatedIncome}")
                    Text("Ganancia: $${report.profit}")
                }
            }
        }
    }
}
