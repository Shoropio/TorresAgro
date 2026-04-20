package com.torresagro.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.torresagro.app.R
import com.torresagro.app.domain.model.InventoryItem

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialItem == null) stringResource(R.string.new_item) else stringResource(R.string.edit_item)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(
                        onClick = {
                            onSave(name, category, parsedStock ?: 0.0, unit, parsedMinStock ?: 0.0)
                        },
                        enabled = inventoryFormValid
                    ) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.save_btn))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.product_name)) },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text(stringResource(R.string.category_label)) },
                modifier = Modifier.fillMaxWidth(),
                isError = categoryError != null,
                supportingText = categoryError?.let { { Text(it) } },
                shape = RoundedCornerShape(12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text(stringResource(R.string.stock_label)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = stockError != null,
                    supportingText = stockError?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text(stringResource(R.string.unit_label)) },
                    modifier = Modifier.weight(0.8f),
                    isError = unitError != null,
                    supportingText = unitError?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = minStock,
                onValueChange = { minStock = it },
                label = { Text(stringResource(R.string.min_stock_alert)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = minStockError != null,
                supportingText = minStockError?.let { { Text(it) } },
                shape = RoundedCornerShape(12.dp)
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
}

private fun String.normalizedDecimalOrNull(): Double? =
    trim().replace(",", ".").toDoubleOrNull()

private fun validateRequired(value: String, message: String): String? =
    if (value.isBlank()) message else null
