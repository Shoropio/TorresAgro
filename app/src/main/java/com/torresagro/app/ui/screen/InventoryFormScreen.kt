package com.torresagro.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.torresagro.app.domain.model.InventoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryFormScreen(
    initialItem: InventoryItem? = null,
    onSave: (String, String, Double, String, Double) -> Unit,
    onDelete: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var category by remember { mutableStateOf(initialItem?.category ?: "") }
    var stock by remember { mutableStateOf(initialItem?.stock?.toString() ?: "") }
    var unit by remember { mutableStateOf(initialItem?.unit ?: "Kg") }
    var minStock by remember { mutableStateOf(initialItem?.minimumStock?.toString() ?: "5.0") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialItem == null) "Nuevo Artículo" else "Editar Artículo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(
                        onClick = {
                            val stockVal = stock.toDoubleOrNull() ?: 0.0
                            val minVal = minStock.toDoubleOrNull() ?: 0.0
                            onSave(name, category, stockVal, unit, minVal)
                        },
                        enabled = name.isNotBlank() && category.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
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
                label = { Text("Nombre del Producto") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoría (Fertilizante, Semilla, etc.)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock Actual") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unidad") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            OutlinedTextField(
                value = minStock,
                onValueChange = { minStock = it },
                label = { Text("Stock Mínimo (Alerta)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}
