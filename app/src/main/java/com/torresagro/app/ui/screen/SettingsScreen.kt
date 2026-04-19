package com.torresagro.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.torresagro.app.R
import com.torresagro.app.ui.component.SectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onSignOut: () -> Unit = {}) {
    var notificationFreq by remember { mutableStateOf(0) } // 0: Daily, 1: Weekly, 2: Critical
    val freqOptions = listOf(
        stringResource(R.string.notif_daily),
        stringResource(R.string.notif_weekly),
        stringResource(R.string.notif_critical)
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings_title)) })
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
                Text(
                    text = stringResource(R.string.notification_frequency),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Card {
                    Column(modifier = Modifier.padding(8.dp)) {
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
                ListItem(
                    headlineContent = { Text(stringResource(R.string.dark_mode)) },
                    supportingContent = { Text(stringResource(R.string.dark_mode_desc)) },
                    leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                    trailingContent = { Switch(checked = true, onCheckedChange = {}) } // Controlled by system usually
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(stringResource(R.string.language)) },
                    supportingContent = { Text("Español") },
                    leadingContent = { Icon(Icons.Default.Language, contentDescription = null) }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(stringResource(R.string.about_app)) },
                    supportingContent = { Text(stringResource(R.string.app_version)) },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar sesion")
                }
            }
        }
    }
}
