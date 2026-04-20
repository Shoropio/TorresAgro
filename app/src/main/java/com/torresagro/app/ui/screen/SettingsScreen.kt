package com.torresagro.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.torresagro.app.R
import com.torresagro.app.ui.component.SectionTitle
import com.torresagro.app.ui.component.SurfaceStatChip
import com.torresagro.app.ui.theme.AccentGold
import com.torresagro.app.ui.theme.AccentSky

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onSignOut: () -> Unit = {}) {
    var notificationFreq by remember { mutableStateOf(0) }
    val freqOptions = listOf(
        stringResource(R.string.notif_daily),
        stringResource(R.string.notif_weekly),
        stringResource(R.string.notif_critical)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                    title = stringResource(R.string.settings_title),
                    subtitle = "Ajusta alertas, apariencia y sesion desde un panel mas claro y ordenado."
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SurfaceStatChip(
                        label = "Alertas",
                        value = freqOptions[notificationFreq],
                        icon = Icons.Default.Info,
                        modifier = Modifier.weight(1f),
                        accent = AccentSky
                    )
                    SurfaceStatChip(
                        label = "Tema",
                        value = stringResource(R.string.dark_mode),
                        icon = Icons.Default.DarkMode,
                        modifier = Modifier.weight(1f),
                        accent = AccentGold
                    )
                }
            }
            item {
                Text(
                    text = stringResource(R.string.notification_frequency),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Card(
                    border = CardDefaults.outlinedCardBorder(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        freqOptions.forEachIndexed { index, option ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = notificationFreq == index,
                                    onClick = { notificationFreq = index }
                                )
                                Text(text = option)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    border = CardDefaults.outlinedCardBorder(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.dark_mode)) },
                        supportingContent = { Text(stringResource(R.string.dark_mode_desc)) },
                        leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                        trailingContent = { Switch(checked = true, onCheckedChange = {}) }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.language)) },
                        supportingContent = { Text(stringResource(R.string.language_name)) },
                        leadingContent = { Icon(Icons.Default.Language, contentDescription = null) }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.about_app)) },
                        supportingContent = { Text(stringResource(R.string.app_version)) },
                        leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.sign_out))
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.copyright),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
