package com.torresagro.app.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.torresagro.app.domain.model.AppUiState
import com.torresagro.app.domain.model.SmartSuggestion
import com.torresagro.app.domain.model.SmartSuggestionSource
import com.torresagro.app.ui.component.EmptyStateCard
import com.torresagro.app.ui.component.SectionTitle
import com.torresagro.app.ui.component.SurfaceStatChip
import com.torresagro.app.ui.theme.AccentGold
import com.torresagro.app.ui.theme.AccentSky

@Composable
fun SmartAgroScreen(state: AppUiState) {
    val suggestions = state.smartAnalyses.flatMap { it.suggestions }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionTitle(
                title = "Inteligencia agronomica",
                subtitle = "Reglas por cultivo, aprendizaje historico y biblioteca tecnica para apoyar decisiones de campo."
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SurfaceStatChip(
                    label = "Cultivos",
                    value = state.technicalLibrary.size.toString(),
                    icon = Icons.Default.LibraryBooks,
                    modifier = Modifier.weight(1f),
                    accent = AccentSky
                )
                SurfaceStatChip(
                    label = "Sugerencias",
                    value = suggestions.size.toString(),
                    icon = Icons.Default.Psychology,
                    modifier = Modifier.weight(1f),
                    accent = AccentGold
                )
            }
        }

        if (state.smartAnalyses.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Sin parcelas para analizar",
                    description = "Crea parcelas y registra labores para activar recomendaciones por cultivo, etapa e historial.",
                    icon = Icons.Default.AutoGraph
                )
            }
        } else {
            items(state.smartAnalyses, key = { it.parcelId }) { analysis ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(analysis.parcelName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "${analysis.cropType.displayName} | ${analysis.currentStage?.name ?: "Etapa por confirmar"} | Dia ${analysis.daysAfterSowing}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MiniMetric("Tareas", analysis.openTasks.toString())
                            MiniMetric("Labores", analysis.completedActivities.toString())
                        }
                        analysis.suggestions.take(3).forEach { suggestion ->
                            SuggestionCard(suggestion)
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(
                title = "Biblioteca tecnica",
                subtitle = "Fichas resumidas basadas en referencias FAO y MAG para consulta rapida en campo."
            )
        }

        items(state.technicalLibrary, key = { it.cropType.name }) { sheet ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(sheet.cropType.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(sheet.sourceSummary, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("Manejo: ${sheet.generalManagement.take(2).joinToString(" ")}", style = MaterialTheme.typography.bodyMedium)
                    Text("Factores criticos: ${sheet.criticalFactors.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Etapas: ${sheet.stages.joinToString(" / ") { it.name }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MiniMetric(label: String, value: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SuggestionCard(suggestion: SmartSuggestion) {
    val color = when (suggestion.source) {
        SmartSuggestionSource.TechnicalRule -> AccentSky
        SmartSuggestionSource.HistoricalLearning -> Color(0xFF2E7D32)
        SmartSuggestionSource.SimilarParcelPattern -> AccentGold
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(suggestion.priority, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
                Text("${(suggestion.confidence * 100).toInt()}% confianza", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(suggestion.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(suggestion.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${sourceLabel(suggestion.source)} | ${suggestion.sourceDetail}", style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

private fun sourceLabel(source: SmartSuggestionSource): String = when (source) {
    SmartSuggestionSource.TechnicalRule -> "Regla tecnica"
    SmartSuggestionSource.HistoricalLearning -> "Aprendizaje historico"
    SmartSuggestionSource.SimilarParcelPattern -> "Parcelas similares"
}
