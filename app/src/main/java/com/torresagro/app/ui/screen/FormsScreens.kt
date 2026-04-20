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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.foundation.layout.*
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.torresagro.app.R
import com.torresagro.app.domain.model.*
import com.torresagro.app.ui.component.SectionTitle
import com.torresagro.app.ui.util.createTempImageUri
import com.torresagro.app.ui.util.captureCurrentLocation
import java.time.LocalDate
import java.util.Locale

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionTitle(stringResource(R.string.app_name), stringResource(R.string.splash_desc))
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
        SectionTitle(stringResource(R.string.quick_access_title), stringResource(R.string.quick_access_desc))
        Button(onClick = onContinue) { Text(stringResource(R.string.enter_panel)) }
    }
}

@Composable
fun NewParcelScreen(
    initialParcel: Parcel? = null,
    calculatedArea: Double? = null,
    updatedBoundary: List<Pair<Double, Double>>? = null,
    onSave: (String, String, Double, CropType, String, String, Double?, Double?, List<Pair<Double, Double>>) -> Unit,
    onOpenMap: (List<Pair<Double, Double>>) -> Unit,
    onBack: () -> Unit,
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
    val parsedSize = remember(sizeText) { sizeText.normalizedDecimalOrNull() }
    val nameError = remember(name) { validateRequired(name, context.getString(R.string.error_parcel_name)) }
    val locationError = remember(locationName) { validateRequired(locationName, context.getString(R.string.error_parcel_location)) }
    val sizeError = remember(sizeText, parsedSize) {
        when {
            sizeText.isBlank() -> context.getString(R.string.error_parcel_size)
            parsedSize == null -> context.getString(R.string.error_invalid_size)
            parsedSize <= 0.0 -> context.getString(R.string.error_zero_size)
            else -> null
        }
    }
    val varietyError = remember(variety) { validateRequired(variety, context.getString(R.string.error_parcel_variety)) }
    val sowingDateError = remember(sowingDate) {
        if (parseDateOrNull(sowingDate) == null) context.getString(R.string.error_invalid_date) else null
    }
    val parcelFormValid = listOf(nameError, locationError, sizeError, varietyError, sowingDateError).all { it == null }

    LaunchedEffect(calculatedArea) {
        calculatedArea?.let { sizeText = String.format(Locale.US, "%.2f", it) }
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Default.Map, contentDescription = stringResource(R.string.back), modifier = androidx.compose.ui.Modifier.rotate(180f)) } // Usando Map como fallback de icono
                SectionTitle(
                    if (initialParcel == null) stringResource(R.string.new_parcel_title) else stringResource(R.string.edit_parcel_title),
                    stringResource(R.string.parcel_form_desc)
                )
            }
        }
        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.field_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } }
            )
        }
        item {
            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text(stringResource(R.string.field_location)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = locationError != null,
                supportingText = locationError?.let { { Text(it) } }
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
                    label = { Text(stringResource(R.string.field_size_ha)) },
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
                    Text(stringResource(R.string.map_btn))
                }
            }
        }
        if (sizeError != null) {
            item {
                Text(
                    text = sizeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        item {
            OptionPicker(
                title = stringResource(R.string.crop_label),
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
                label = { Text(stringResource(R.string.field_variety)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = varietyError != null,
                supportingText = varietyError?.let { { Text(it) } }
            )
        }
        item {
            OutlinedTextField(
                value = sowingDate,
                onValueChange = { sowingDate = it },
                label = { Text(stringResource(R.string.field_sowing_date)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = sowingDateError != null,
                supportingText = sowingDateError?.let { { Text(it) } }
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (latitude != null && longitude != null) {
                        stringResource(R.string.coordinates_label, "${"%.5f".format(latitude)}, ${"%.5f".format(longitude)}")
                    } else {
                        stringResource(R.string.coordinates_not_captured)
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
                    Text(stringResource(R.string.use_gps_location))
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
                            stringResource(R.string.polygon_points_label, boundary.size) + "\n" +
                            stringResource(R.string.autocalculated_area_label, sizeText.toDoubleOrNull() ?: 0.0),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        item {
                Button(
                    onClick = {
                        val normalizedSize = parsedSize ?: 0.0
                        onSave(name, locationName, normalizedSize, cropType, variety, sowingDate, latitude, longitude, boundary)
                    },
                modifier = Modifier.fillMaxWidth(),
                enabled = parcelFormValid
            ) {
                Text(if (initialParcel == null) stringResource(R.string.save_parcel) else stringResource(R.string.update_parcel))
            }
        }
        item {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.cancel_btn))
            }
        }
        onDelete?.let { deleteAction ->
            item {
                Button(
                    onClick = deleteAction,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete_parcel_btn))
                }
            }
        }
    }
}

private fun String.normalizedDecimalOrNull(): Double? {
    return trim()
        .replace(",", ".")
        .toDoubleOrNull()
}

private fun validateRequired(value: String, message: String): String? =
    if (value.isBlank()) message else null

private fun parseDateOrNull(value: String): LocalDate? =
    runCatching { LocalDate.parse(value.trim()) }.getOrNull()

@Composable
fun NewActivityScreen(
    parcels: List<Parcel>,
    initialActivity: ActivityRecord? = null,
    preselectedParcelId: String? = null,
    onSave: (String, ActivityType, String, Double, String, String, String?) -> Unit,
    onBack: () -> Unit,
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
    val parsedCost = remember(costText) { costText.normalizedDecimalOrNull() }
    val activityDateError = remember(date) { if (parseDateOrNull(date) == null) context.getString(R.string.error_invalid_date) else null }
    val costError = remember(costText, parsedCost) {
        when {
            costText.isBlank() -> context.getString(R.string.error_cost_empty)
            parsedCost == null -> context.getString(R.string.error_cost_invalid)
            parsedCost < 0.0 -> context.getString(R.string.error_cost_negative)
            else -> null
        }
    }
    val quantityError = remember(quantity) { validateRequired(quantity, context.getString(R.string.error_quantity_required)) }
    val notesError = remember(notes) { validateRequired(notes, context.getString(R.string.error_notes_required)) }
    val activityFormValid = parcelId.isNotBlank() && listOf(activityDateError, costError, quantityError, notesError).all { it == null }

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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Default.Map, contentDescription = stringResource(R.string.back), modifier = Modifier.rotate(180f)) }
                SectionTitle(
                    if (initialActivity == null) stringResource(R.string.new_activity_title) else stringResource(R.string.edit_activity_title),
                    stringResource(R.string.activity_form_desc)
                )
            }
        }
        if (parcels.isEmpty()) {
            item {
                Text(stringResource(R.string.error_no_parcels_activity), color = MaterialTheme.colorScheme.error)
            }
        } else {
            item {
                OptionPicker(
                    title = stringResource(R.string.parcel_label),
                    selectedLabel = parcels.firstOrNull { it.id == parcelId }?.name.orEmpty(),
                    options = parcels.map { it.name },
                    onSelect = { selected ->
                        parcelId = parcels.first { it.name == selected }.id
                    }
                )
            }
            item {
                OptionPicker(
                    title = stringResource(R.string.activity_type_label),
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
                    label = { Text(stringResource(R.string.date_format_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = activityDateError != null,
                    supportingText = activityDateError?.let { { Text(it) } }
                )
            }
            item {
                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it },
                    label = { Text(stringResource(R.string.cost_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = costError != null,
                    supportingText = costError?.let { { Text(it) } }
                )
            }
            item {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text(stringResource(R.string.quantity_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = quantityError != null,
                    supportingText = quantityError?.let { { Text(it) } }
                )
            }
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.observations_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = notesError != null,
                    supportingText = notesError?.let { { Text(it) } }
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.photo_support_label), style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.choose_gallery_btn))
                    }
                    Button(
                        onClick = {
                            val tempUri = createTempImageUri(context)
                            cameraUri = tempUri
                            cameraLauncher.launch(tempUri)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.take_photo_btn))
                    }
                    photoUri?.let { imageUri ->
                        AsyncImage(
                            model = imageUri,
                            contentDescription = stringResource(R.string.activity_photo_desc),
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
                        onSave(parcelId, activityType, date, parsedCost ?: 0.0, quantity, notes, photoUri)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = activityFormValid
                ) {
                    Text(if (initialActivity == null) stringResource(R.string.save_activity_btn) else stringResource(R.string.update_activity_btn))
                }
            }
            item {
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.cancel_btn))
                }
            }
            onDelete?.let { deleteAction ->
                item {
                    Button(
                        onClick = deleteAction,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.delete_activity_btn))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionPicker(
    title: String,
    selectedLabel: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}


@Composable
fun TaskFormScreen(
    parcels: List<Parcel>,
    initialTask: CropTask? = null,
    onSave: (String, String, String, TaskType, String, Boolean, Boolean) -> Unit,
    onBack: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var parcelId by remember { mutableStateOf(initialTask?.parcelId ?: parcels.firstOrNull()?.id.orEmpty()) }
    var title by remember { mutableStateOf(initialTask?.title.orEmpty()) }
    var dueDate by remember { mutableStateOf(initialTask?.dueDate ?: "2026-04-17") }
    var taskType by remember { mutableStateOf(initialTask?.taskType ?: TaskType.Monitoring) }
    var priority by remember { mutableStateOf(initialTask?.priority ?: context.getString(R.string.priority_medium)) }
    var reminderEnabled by remember { mutableStateOf(initialTask?.reminderEnabled ?: true) }
    var completed by remember { mutableStateOf(initialTask?.completed ?: false) }
    val titleError = remember(title) { validateRequired(title, context.getString(R.string.error_task_title_required)) }
    val dueDateError = remember(dueDate) { if (parseDateOrNull(dueDate) == null) context.getString(R.string.error_invalid_date) else null }
    val taskFormValid = parcelId.isNotBlank() && listOf(titleError, dueDateError).all { it == null }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Default.Map, contentDescription = stringResource(R.string.back), modifier = Modifier.rotate(180f)) }
                SectionTitle(
                    if (initialTask == null) stringResource(R.string.new_task_title) else stringResource(R.string.edit_task_title),
                    stringResource(R.string.task_form_desc)
                )
            }
        }
        if (parcels.isEmpty()) {
            item {
                Text(stringResource(R.string.error_no_parcels_task), color = MaterialTheme.colorScheme.error)
            }
        } else {
            item {
                OptionPicker(
                    title = stringResource(R.string.parcel_label),
                    selectedLabel = parcels.firstOrNull { it.id == parcelId }?.name.orEmpty(),
                    options = parcels.map { it.name },
                    onSelect = { selected -> parcelId = parcels.first { it.name == selected }.id }
                )
            }
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.task_title_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = titleError != null,
                    supportingText = titleError?.let { { Text(it) } }
                )
            }
            item {
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text(stringResource(R.string.date_format_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = dueDateError != null,
                    supportingText = dueDateError?.let { { Text(it) } }
                )
            }
            item {
                OptionPicker(
                    title = stringResource(R.string.task_type_label),
                    selectedLabel = taskType.label,
                    options = TaskType.entries.map { it.label },
                    onSelect = { selected -> taskType = TaskType.entries.first { it.label == selected } }
                )
            }
            item {
                OptionPicker(
                    title = stringResource(R.string.priority_label),
                    selectedLabel = priority,
                    options = listOf(stringResource(R.string.priority_high), stringResource(R.string.priority_medium), stringResource(R.string.priority_low)),
                    onSelect = { priority = it }
                )
            }
            item {
                OptionPicker(
                    title = stringResource(R.string.local_reminder_label),
                    selectedLabel = if (reminderEnabled) stringResource(R.string.active) else stringResource(R.string.inactive),
                    options = listOf(stringResource(R.string.active), stringResource(R.string.inactive)),
                    onSelect = { reminderEnabled = it == context.getString(R.string.active) }
                )
            }
            if (initialTask != null) {
                item {
                    OptionPicker(
                        title = stringResource(R.string.status_label),
                        selectedLabel = if (completed) stringResource(R.string.task_done_label) else stringResource(R.string.task_pending_label),
                        options = listOf(stringResource(R.string.task_pending_label), stringResource(R.string.task_done_label)),
                        onSelect = { completed = it == context.getString(R.string.task_done_label) }
                    )
                }
            }
            item {
                Button(
                    onClick = { onSave(parcelId, title, dueDate, taskType, priority, reminderEnabled, completed) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = taskFormValid
                ) {
                    Text(if (initialTask == null) stringResource(R.string.save_task_btn) else stringResource(R.string.update_task_btn))
                }
            }
            item {
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.cancel_btn))
                }
            }
            onDelete?.let { deleteAction ->
                item {
                    Button(onClick = deleteAction, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.delete_task_btn))
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
    onBack: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var parcelId by remember {
        mutableStateOf(
            initialObservation?.parcelId ?: preselectedParcelId
            ?: parcels.firstOrNull()?.id.orEmpty()
        )
    }
    var date by remember { mutableStateOf(initialObservation?.date ?: "2026-04-17") }
    var cropStage by remember {
        mutableStateOf(
            initialObservation?.cropStage ?: "Monitoreo general"
        )
    }
    var generalStatus by remember { mutableStateOf(initialObservation?.generalStatus ?: context.getString(R.string.status_regular)) }
    var selectedSymptoms by remember { mutableStateOf(initialObservation?.symptoms ?: emptyList()) }
    var recommendation by remember {
        mutableStateOf(
            initialObservation?.recommendation
                ?: ObservationSupport.recommendationFor(selectedSymptoms, generalStatus)
        )
    }
    var photoUri by remember { mutableStateOf(initialObservation?.photoUri) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val observationDateError =
        remember(date) { if (parseDateOrNull(date) == null) context.getString(R.string.error_invalid_date) else null }
    val cropStageError =
        remember(cropStage) { validateRequired(cropStage, context.getString(R.string.error_crop_stage_required)) }
    val recommendationError = remember(recommendation) {
        validateRequired(
            recommendation,
            context.getString(R.string.error_recommendation_required)
        )
    }
    val observationFormValid = parcelId.isNotBlank() && listOf(
        observationDateError,
        cropStageError,
        recommendationError
    ).all { it == null }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = stringResource(R.string.back),
                        modifier = Modifier.rotate(180f)
                    )
                }
                SectionTitle(
                    if (initialObservation == null) stringResource(R.string.new_observation_title) else stringResource(R.string.edit_observation_title),
                    stringResource(R.string.observation_form_desc)
                )
            }
        }
        if (parcels.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.error_no_parcels_observation),
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            item {
                OptionPicker(
                    title = stringResource(R.string.parcel_label),
                    selectedLabel = parcels.firstOrNull { it.id == parcelId }?.name.orEmpty(),
                    options = parcels.map { it.name },
                    onSelect = { selected -> parcelId = parcels.first { it.name == selected }.id }
                )
            }
            item {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text(stringResource(R.string.date_format_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = observationDateError != null,
                    supportingText = observationDateError?.let { { Text(it) } }
                )
            }
            item {
                OutlinedTextField(
                    value = cropStage,
                    onValueChange = { cropStage = it },
                    label = { Text(stringResource(R.string.crop_stage_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = cropStageError != null,
                    supportingText = cropStageError?.let { { Text(it) } }
                )
            }
            item {
                OptionPicker(
                    title = stringResource(R.string.general_status_label),
                    selectedLabel = generalStatus,
                    options = listOf(stringResource(R.string.status_good), stringResource(R.string.status_regular), stringResource(R.string.status_alert)),
                    onSelect = {
                        generalStatus = it
                        recommendation =
                            ObservationSupport.recommendationFor(selectedSymptoms, generalStatus)
                    }
                )
            }
            item {
                MultiOptionPicker(
                    title = stringResource(R.string.observed_symptoms_label),
                    selected = selectedSymptoms,
                    options = ObservationSupport.symptomsCatalog,
                    onToggle = { symptom ->
                        selectedSymptoms = if (symptom in selectedSymptoms) {
                            selectedSymptoms - symptom
                        } else {
                            selectedSymptoms + symptom
                        }
                        recommendation =
                            ObservationSupport.recommendationFor(selectedSymptoms, generalStatus)
                    }
                )
            }
            item {
                OutlinedTextField(
                    value = recommendation,
                    onValueChange = { recommendation = it },
                    label = { Text(stringResource(R.string.recommendation_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = recommendationError != null,
                    supportingText = recommendationError?.let { { Text(it) } }
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.monitoring_photo_label), style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.choose_gallery_btn))
                    }
                    Button(
                        onClick = {
                            val tempUri = createTempImageUri(context)
                            cameraUri = tempUri
                            cameraLauncher.launch(tempUri)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.take_photo_btn))
                    }
                    photoUri?.let { imageUri ->
                        AsyncImage(
                            model = imageUri,
                            contentDescription = stringResource(R.string.observation_form_desc),
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        onSave(
                            parcelId,
                            date,
                            cropStage,
                            generalStatus,
                            selectedSymptoms,
                            recommendation,
                            photoUri
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = observationFormValid
                ) {
                    Text(if (initialObservation == null) stringResource(R.string.save_observation_btn) else stringResource(R.string.update_observation_btn))
                }
            }
            item {
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.cancel_btn))
                }
            }
            onDelete?.let { deleteAction ->
                item {
                    Button(onClick = deleteAction, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.delete_observation_btn))
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
            if (selected.isEmpty()) stringResource(R.string.selected_none) else stringResource(R.string.selected_label, selected.joinToString()),
            style = MaterialTheme.typography.bodyMedium
        )
        Card {
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    Button(onClick = { onToggle(option) }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (option in selected) stringResource(R.string.remove_label, option) else stringResource(R.string.add_label, option))
                    }
                }
            }
        }
    }
}

