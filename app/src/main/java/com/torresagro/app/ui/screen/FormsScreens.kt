package com.torresagro.app.ui.screen

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.torresagro.app.domain.model.ActivityType
import com.torresagro.app.domain.model.ActivityRecord
import com.torresagro.app.domain.model.CropTask
import com.torresagro.app.domain.model.CropObservation
import com.torresagro.app.domain.model.CropType
import com.torresagro.app.domain.model.ObservationSupport
import com.torresagro.app.domain.model.Parcel
import com.torresagro.app.domain.model.TaskType
import com.torresagro.app.ui.component.SectionTitle
import com.torresagro.app.ui.util.createTempImageUri
import com.torresagro.app.ui.util.captureCurrentLocation

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionTitle("Torres Agro", "Herramienta simple para planificar y registrar la finca.")
    }
}

@Composable
fun QuickAccessScreen(onContinue: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionTitle("Acceso rapido", "Entrar sin cuenta o activar sincronizacion mas adelante.")
        Button(onClick = onContinue) { Text("Entrar al panel") }
    }
}

@Composable
fun NewParcelScreen(
    initialParcel: Parcel? = null,
    calculatedArea: Double? = null,
    updatedBoundary: List<Pair<Double, Double>>? = null,
    onSave: (String, String, Double, CropType, String, String, Double?, Double?, List<Pair<Double, Double>>) -> Unit,
    onOpenMap: (List<Pair<Double, Double>>) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(initialParcel?.name.orEmpty()) }
    var locationName by remember { mutableStateOf(initialParcel?.locationName.orEmpty()) }
    var sizeText by remember { mutableStateOf(initialParcel?.sizeHectares?.toString().orEmpty()) }
    var variety by remember { mutableStateOf(initialParcel?.variety.orEmpty()) }
    var sowingDate by remember { mutableStateOf(initialParcel?.sowingDate ?: "2026-04-17") }
    var cropType by remember { mutableStateOf(initialParcel?.cropType ?: CropType.Cassava) }
    var latitude by remember { mutableStateOf(initialParcel?.latitude) }
    var longitude by remember { mutableStateOf(initialParcel?.longitude) }
    var boundary by remember { mutableStateOf(initialParcel?.boundary ?: emptyList()) }

    LaunchedEffect(calculatedArea) {
        calculatedArea?.let { sizeText = "%.2f".format(it) }
    }
    LaunchedEffect(updatedBoundary) {
        updatedBoundary?.let { boundary = it }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            scope.launch {
                captureCurrentLocation(context)?.let { coords ->
                    latitude = coords.first
                    longitude = coords.second
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(
                if (initialParcel == null) "Nueva parcela" else "Editar parcela",
                "Guardar lote, cultivo, variedad y fecha de siembra."
            )
        }
        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la parcela") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Ubicacion") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = sizeText,
                    onValueChange = { sizeText = it },
                    label = { Text("Tamaño (ha)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = { onOpenMap(boundary) },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Mapa")
                }
            }
        }
        item {
            OptionPicker(
                title = "Cultivo",
                selectedLabel = cropType.displayName,
                options = CropType.entries.map { it.displayName },
                onSelect = { selected ->
                    cropType = CropType.entries.first { it.displayName == selected }
                }
            )
        }
        item {
            OutlinedTextField(
                value = variety,
                onValueChange = { variety = it },
                label = { Text("Variedad") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            OutlinedTextField(
                value = sowingDate,
                onValueChange = { sowingDate = it },
                label = { Text("Fecha de siembra (AAAA-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (latitude != null && longitude != null) {
                        "Coordenadas: ${"%.5f".format(latitude)}, ${"%.5f".format(longitude)}"
                    } else {
                        "Coordenadas: no capturadas"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Usar mi ubicación GPS actual")
                }
            }
        }
        if (boundary.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.SquareFoot, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            "Polígono trazado: ${boundary.size} puntos.\nÁrea autocalculada: ${"%.2f".format(sizeText.toDoubleOrNull() ?: 0.0)} ha",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        item {
            Button(
                onClick = {
                    val size = sizeText.toDoubleOrNull() ?: 0.0
                    onSave(name, locationName, size, cropType, variety, sowingDate, latitude, longitude, boundary)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && locationName.isNotBlank() && variety.isNotBlank() && sizeText.toDoubleOrNull() != null
            ) {
                Text(if (initialParcel == null) "Guardar parcela" else "Actualizar parcela")
            }
        }
        onDelete?.let { deleteAction ->
            item {
                Button(
                    onClick = deleteAction,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar parcela")
                }
            }
        }
    }
}

@Composable
fun NewActivityScreen(
    parcels: List<Parcel>,
    initialActivity: ActivityRecord? = null,
    preselectedParcelId: String? = null,
    onSave: (String, ActivityType, String, Double, String, String, String?) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var parcelId by remember { mutableStateOf(initialActivity?.parcelId ?: preselectedParcelId ?: parcels.firstOrNull()?.id.orEmpty()) }
    var activityType by remember { mutableStateOf(initialActivity?.activityType ?: ActivityType.Sowing) }
    var date by remember { mutableStateOf(initialActivity?.date ?: "2026-04-17") }
    var costText by remember { mutableStateOf(initialActivity?.cost?.toString().orEmpty()) }
    var quantity by remember { mutableStateOf(initialActivity?.quantity.orEmpty()) }
    var notes by remember { mutableStateOf(initialActivity?.notes.orEmpty()) }
    var photoUri by remember { mutableStateOf(initialActivity?.photoUri) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) photoUri = uri.toString()
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri = cameraUri?.toString()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(
                if (initialActivity == null) "Nueva actividad" else "Editar actividad",
                "Registrar siembra, riego, abonado, fumigacion, deshierbe o cosecha."
            )
        }
        if (parcels.isEmpty()) {
            item {
                Text("Primero crea una parcela para poder registrar actividades.", color = MaterialTheme.colorScheme.error)
            }
        } else {
            item {
                OptionPicker(
                    title = "Parcela",
                    selectedLabel = parcels.firstOrNull { it.id == parcelId }?.name.orEmpty(),
                    options = parcels.map { it.name },
                    onSelect = { selected ->
                        parcelId = parcels.first { it.name == selected }.id
                    }
                )
            }
            item {
                OptionPicker(
                    title = "Tipo de actividad",
                    selectedLabel = activityType.label,
                    options = ActivityType.entries.map { it.label },
                    onSelect = { selected ->
                        activityType = ActivityType.entries.first { it.label == selected }
                    }
                )
            }
            item {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Fecha (AAAA-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it },
                    label = { Text("Costo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Cantidad aplicada o usada") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Foto de apoyo", style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Elegir de galeria")
                    }
                    Button(
                        onClick = {
                            val tempUri = createTempImageUri(context)
                            cameraUri = tempUri
                            cameraLauncher.launch(tempUri)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tomar foto")
                    }
                    photoUri?.let { imageUri ->
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Foto de actividad",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        onSave(parcelId, activityType, date, costText.toDoubleOrNull() ?: 0.0, quantity, notes, photoUri)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = parcelId.isNotBlank() && quantity.isNotBlank() && notes.isNotBlank()
                ) {
                    Text(if (initialActivity == null) "Guardar actividad" else "Actualizar actividad")
                }
            }
            onDelete?.let { deleteAction ->
                item {
                    Button(
                        onClick = deleteAction,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Eliminar actividad")
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionPicker(
    title: String,
    selectedLabel: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text("Seleccionado: $selectedLabel", style = MaterialTheme.typography.bodyMedium)
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    Button(
                        onClick = { onSelect(option) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(option)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle("Configuracion", "Preferencias, notificaciones, modo offline y opcion de sincronizacion.")
    }
}

@Composable
fun TaskFormScreen(
    parcels: List<Parcel>,
    initialTask: CropTask? = null,
    onSave: (String, String, String, TaskType, String, Boolean, Boolean) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var parcelId by remember { mutableStateOf(initialTask?.parcelId ?: parcels.firstOrNull()?.id.orEmpty()) }
    var title by remember { mutableStateOf(initialTask?.title.orEmpty()) }
    var dueDate by remember { mutableStateOf(initialTask?.dueDate ?: "2026-04-17") }
    var taskType by remember { mutableStateOf(initialTask?.taskType ?: TaskType.Monitoring) }
    var priority by remember { mutableStateOf(initialTask?.priority ?: "Media") }
    var reminderEnabled by remember { mutableStateOf(initialTask?.reminderEnabled ?: true) }
    var completed by remember { mutableStateOf(initialTask?.completed ?: false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(
                if (initialTask == null) "Nueva tarea" else "Editar tarea",
                "Programar labores, prioridad y recordatorio local."
            )
        }
        if (parcels.isEmpty()) {
            item {
                Text("Primero crea una parcela para programar tareas.", color = MaterialTheme.colorScheme.error)
            }
        } else {
            item {
                OptionPicker(
                    title = "Parcela",
                    selectedLabel = parcels.firstOrNull { it.id == parcelId }?.name.orEmpty(),
                    options = parcels.map { it.name },
                    onSelect = { selected -> parcelId = parcels.first { it.name == selected }.id }
                )
            }
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titulo de la tarea") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Fecha (AAAA-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OptionPicker(
                    title = "Tipo de tarea",
                    selectedLabel = taskType.label,
                    options = TaskType.entries.map { it.label },
                    onSelect = { selected -> taskType = TaskType.entries.first { it.label == selected } }
                )
            }
            item {
                OptionPicker(
                    title = "Prioridad",
                    selectedLabel = priority,
                    options = listOf("Alta", "Media", "Baja"),
                    onSelect = { priority = it }
                )
            }
            item {
                OptionPicker(
                    title = "Recordatorio local",
                    selectedLabel = if (reminderEnabled) "Activo" else "Inactivo",
                    options = listOf("Activo", "Inactivo"),
                    onSelect = { reminderEnabled = it == "Activo" }
                )
            }
            if (initialTask != null) {
                item {
                    OptionPicker(
                        title = "Estado",
                        selectedLabel = if (completed) "Realizada" else "Pendiente",
                        options = listOf("Pendiente", "Realizada"),
                        onSelect = { completed = it == "Realizada" }
                    )
                }
            }
            item {
                Button(
                    onClick = { onSave(parcelId, title, dueDate, taskType, priority, reminderEnabled, completed) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = parcelId.isNotBlank() && title.isNotBlank()
                ) {
                    Text(if (initialTask == null) "Guardar tarea" else "Actualizar tarea")
                }
            }
            onDelete?.let { deleteAction ->
                item {
                    Button(onClick = deleteAction, modifier = Modifier.fillMaxWidth()) {
                        Text("Eliminar tarea")
                    }
                }
            }
        }
    }
}

@Composable
fun ObservationFormScreen(
    parcels: List<Parcel>,
    initialObservation: CropObservation? = null,
    preselectedParcelId: String? = null,
    onSave: (String, String, String, String, List<String>, String, String?) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var parcelId by remember { mutableStateOf(initialObservation?.parcelId ?: preselectedParcelId ?: parcels.firstOrNull()?.id.orEmpty()) }
    var date by remember { mutableStateOf(initialObservation?.date ?: "2026-04-17") }
    var cropStage by remember { mutableStateOf(initialObservation?.cropStage ?: "Monitoreo general") }
    var generalStatus by remember { mutableStateOf(initialObservation?.generalStatus ?: "Regular") }
    var selectedSymptoms by remember { mutableStateOf(initialObservation?.symptoms ?: emptyList()) }
    var recommendation by remember {
        mutableStateOf(
            initialObservation?.recommendation
                ?: ObservationSupport.recommendationFor(selectedSymptoms, generalStatus)
        )
    }
    var photoUri by remember { mutableStateOf(initialObservation?.photoUri) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) photoUri = uri.toString()
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) photoUri = cameraUri?.toString()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(
                if (initialObservation == null) "Nuevo monitoreo" else "Editar monitoreo",
                "Registrar estado general, sintomas y evidencia visual por visita."
            )
        }
        if (parcels.isEmpty()) {
            item { Text("Primero crea una parcela para registrar monitoreo.", color = MaterialTheme.colorScheme.error) }
        } else {
            item {
                OptionPicker(
                    title = "Parcela",
                    selectedLabel = parcels.firstOrNull { it.id == parcelId }?.name.orEmpty(),
                    options = parcels.map { it.name },
                    onSelect = { selected -> parcelId = parcels.first { it.name == selected }.id }
                )
            }
            item {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Fecha (AAAA-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = cropStage,
                    onValueChange = { cropStage = it },
                    label = { Text("Etapa del cultivo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OptionPicker(
                    title = "Estado general",
                    selectedLabel = generalStatus,
                    options = listOf("Bueno", "Regular", "Alerta"),
                    onSelect = {
                        generalStatus = it
                        recommendation = ObservationSupport.recommendationFor(selectedSymptoms, generalStatus)
                    }
                )
            }
            item {
                MultiOptionPicker(
                    title = "Sintomas observados",
                    selected = selectedSymptoms,
                    options = ObservationSupport.symptomsCatalog,
                    onToggle = { symptom ->
                        selectedSymptoms = if (symptom in selectedSymptoms) {
                            selectedSymptoms - symptom
                        } else {
                            selectedSymptoms + symptom
                        }
                        recommendation = ObservationSupport.recommendationFor(selectedSymptoms, generalStatus)
                    }
                )
            }
            item {
                OutlinedTextField(
                    value = recommendation,
                    onValueChange = { recommendation = it },
                    label = { Text("Recomendacion") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Foto de monitoreo", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                        Text("Elegir de galeria")
                    }
                    Button(
                        onClick = {
                            val tempUri = createTempImageUri(context)
                            cameraUri = tempUri
                            cameraLauncher.launch(tempUri)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tomar foto")
                    }
                    photoUri?.let { imageUri ->
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Foto de monitoreo",
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = { onSave(parcelId, date, cropStage, generalStatus, selectedSymptoms, recommendation, photoUri) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = parcelId.isNotBlank() && cropStage.isNotBlank()
                ) {
                    Text(if (initialObservation == null) "Guardar monitoreo" else "Actualizar monitoreo")
                }
            }
            onDelete?.let { deleteAction ->
                item {
                    Button(onClick = deleteAction, modifier = Modifier.fillMaxWidth()) {
                        Text("Eliminar monitoreo")
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiOptionPicker(
    title: String,
    selected: List<String>,
    options: List<String>,
    onToggle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            if (selected.isEmpty()) "Seleccionado: ninguno" else "Seleccionado: ${selected.joinToString()}",
            style = MaterialTheme.typography.bodyMedium
        )
        Card {
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    Button(onClick = { onToggle(option) }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (option in selected) "Quitar: $option" else "Agregar: $option")
                    }
                }
            }
        }
    }
}
