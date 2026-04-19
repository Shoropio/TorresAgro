package com.torresagro.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.torresagro.app.R
import com.torresagro.app.domain.model.AppUiState
import com.torresagro.app.domain.model.InventoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    state: AppUiState,
    onAddItem: () -> Unit,
    onEditItem: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.inventory_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddItem) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Item")
            }
        }
    ) { padding ->
        if (state.inventory.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Text("El inventario está vacío", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.inventory) { item ->
                    InventoryCard(item = item, onClick = { onEditItem(item.id) })
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryCard(item: InventoryItem, onClick: () -> Unit) {
    val isLowStock = item.stock <= item.minimumStock
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (isLowStock) {
                    Icon(Icons.Default.Warning, contentDescription = "Bajo Stock", tint = MaterialTheme.colorScheme.error)
                }
            }
            
            Text(
                text = item.category,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${item.stock}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = " ${item.unit}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Mín: ${item.minimumStock}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
