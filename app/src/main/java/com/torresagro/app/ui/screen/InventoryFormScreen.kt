package com.torresagro.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.torresagro.app.R
import com.torresagro.app.domain.model.InventoryItem

@Composable
fun InventoryFormScreen(
    initialItem: InventoryItem? = null,
    onSave: (String, String, Double, String, Double) -> Unit,
    onDelete: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var category by remember { mutableStateOf(initialItem?.category ?: "") }
    var stock by remember { mutableStateOf(initialItem?.stock?.toString() ?: "") }
    var unit by remember { mutableStateOf(initialItem?.unit ?: "Kg") }
    var minStock by remember { mutableStateOf(initialItem?.minimumStock?.toString() ?: "5.0") }

    val parsedStock = remember(stock) { stock.normalizedDecimalOrNull() }
    val parsedMinStock = remember(minStock) { minStock.normalizedDecimalOrNull() }
    val nameError = remember(name) { validateRequired(name, context.getString(R.string.error_required_name)) }
    val categoryError = remember(category) { validateRequired(category, context.getString(R.string.error_required_category)) }
    val unitError = remember(unit) { validateRequired(unit, context.getString(R.string.error_required_unit)) }
    val stockError = remember(stock, parsedStock) {
        when {
            stock.isBlank() -> context.getString(R.string.error_required_stock)
            parsedStock == null -> context.getString(R.string.error_invalid_stock)
            parsedStock < 0.0 -> context.getString(R.string.error_negative_stock)
            else -> null
        }
    }
    val minStockError = remember(minStock, parsedMinStock) {
        when {
            minStock.isBlank() -> context.getString(R.string.error_required_min_stock)
            parsedMinStock == null -> context.getString(R.string.error_invalid_min_stock)
            parsedMinStock < 0.0 -> context.getString(R.string.error_negative_min_stock)
            else -> null
        }
    }
    val inventoryFormValid = listOf(nameError, categoryError, unitError, stockError, minStockError).all { it == null }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            InventoryFormHeader(
                title = if (initialItem == null) stringResource(R.string.new_item) else stringResource(R.string.edit_item),
                subtitle = stringResource(R.string.inventory_desc),
                onBack = onBack
            )
        }

        item {
            InventoryFormStatRow(
                listOf(
                    "Estado" to if (initialItem == null) "Nuevo" else "Edicion",
                    "Stock" to (parsedStock?.toString() ?: "--")
                )
            )
        }

        item {
            InventoryFormSection(
                title = "Datos del insumo",
                subtitle = "Identifica el producto y su categoria para control operativo."
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.product_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(stringResource(R.string.category_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = categoryError != null,
                    supportingText = categoryError?.let { { Text(it) } },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        item {
            InventoryFormSection(
                title = "Existencias",
                subtitle = "Registra cantidad actual, unidad y minimo para alertas."
            ) {
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text(stringResource(R.string.stock_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = stockError != null,
                    supportingText = stockError?.let { { Text(it) } },
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text(stringResource(R.string.unit_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = unitError != null,
                    supportingText = unitError?.let { { Text(it) } },
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = minStock,
                    onValueChange = { minStock = it },
                    label = { Text(stringResource(R.string.min_stock_alert)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = minStockError != null,
                    supportingText = minStockError?.let { { Text(it) } },
                    shape = RoundedCornerShape(8.dp)
                )

                if (inventoryFormValid) {
                    Text(
                        text = stringResource(R.string.ready_to_save),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Button(
                onClick = { onSave(name, category, parsedStock ?: 0.0, unit, parsedMinStock ?: 0.0) },
                modifier = Modifier.fillMaxWidth(),
                enabled = inventoryFormValid,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(if (initialItem == null) R.string.save_btn else R.string.update))
            }
        }

        item {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.cancel_btn))
            }
        }

        onDelete?.let { deleteAction ->
            item {
                Button(
                    onClick = deleteAction,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete_btn))
                }
            }
        }
    }
}

@Composable
private fun InventoryFormHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.back),
                modifier = Modifier.rotate(180f)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun InventoryFormSection(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
    }
}

@Composable
private fun InventoryFormStatRow(items: List<Pair<String, String>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { (label, value) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

private fun String.normalizedDecimalOrNull(): Double? =
    trim().replace(",", ".").toDoubleOrNull()

private fun validateRequired(value: String, message: String): String? =
    if (value.isBlank()) message else null
