package com.torresagro.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.torresagro.app.R
import com.torresagro.app.domain.model.AppUiState
import com.torresagro.app.domain.model.InventoryItem
import com.torresagro.app.ui.component.EmptyStateCard
import com.torresagro.app.ui.component.SectionTitle
import com.torresagro.app.ui.component.SurfaceStatChip
import com.torresagro.app.ui.theme.AccentGold
import com.torresagro.app.ui.theme.AccentSky

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    state: AppUiState,
    onAddItem: () -> Unit,
    onEditItem: (String) -> Unit
) {
    val lowStockItems = remember(state.inventory) { state.inventory.count { it.stock <= it.minimumStock } }
    val totalUnits = remember(state.inventory) { state.inventory.sumOf { it.stock } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.inventory_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddItem,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Anadir item")
            }
        }
    ) { padding ->
        if (state.inventory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    title = "Inventario listo para arrancar",
                    description = "Agrega insumos, fertilizantes y herramientas para controlar stock y faltantes antes de que peguen en campo.",
                    icon = Icons.Default.Inventory,
                    actionLabel = "Agregar item",
                    onAction = onAddItem
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    SectionTitle(
                        title = stringResource(R.string.inventory_and_supplies),
                        subtitle = stringResource(R.string.inventory_desc)
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SurfaceStatChip(
                            label = "Items activos",
                            value = state.inventory.size.toString(),
                            icon = Icons.Default.Inventory,
                            modifier = Modifier.weight(1f),
                            accent = AccentSky
                        )
                        SurfaceStatChip(
                            label = "Bajo stock",
                            value = lowStockItems.toString(),
                            icon = Icons.Default.Warning,
                            modifier = Modifier.weight(1f),
                            accent = AccentGold
                        )
                    }
                }
                item {
                    SurfaceStatChip(
                        label = "Unidades registradas",
                        value = totalUnits.toString(),
                        icon = Icons.Default.Tune,
                        accent = MaterialTheme.colorScheme.primary
                    )
                }
                items(state.inventory, key = { it.id }) { item ->
                    InventoryCard(item = item, onClick = { onEditItem(item.id) })
                }
            }
        }
    }
}

@Composable
fun InventoryCard(item: InventoryItem, onClick: () -> Unit) {
    val isLowStock = item.stock <= item.minimumStock

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (isLowStock) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Bajo stock",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                text = item.category,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${item.stock}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = " ${item.unit}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Min: ${item.minimumStock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
