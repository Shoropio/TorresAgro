package com.torresagro.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.torresagro.app.data.firebase.FirebaseBootstrap
import com.torresagro.app.data.firebase.FirebaseSyncGateway
import com.torresagro.app.data.local.AgroDatabase
import com.torresagro.app.data.local.util.SyncGatewayHolder
import com.torresagro.app.data.local.util.TaskReminderScheduler
import com.torresagro.app.data.local.util.TaskReminderWorker
import com.torresagro.app.data.repository.RoomAgroRepository
import com.torresagro.app.ui.TorresAgroApp
import com.torresagro.app.ui.theme.TorresAgroTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private var notificationRoute by mutableStateOf<String?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val database by lazy {
        AgroDatabase.getDatabase(applicationContext)
    }

    private val repository by lazy {
        val authManager = com.torresagro.app.data.firebase.FirebaseAuthManager()
        val syncGateway = FirebaseSyncGateway(applicationContext, database, authManager)
        SyncGatewayHolder.set(syncGateway)
        RoomAgroRepository(
            dao = database.agroDao(),
            syncGateway = syncGateway,
            reminderScheduler = TaskReminderScheduler(applicationContext),
            authManager = authManager,
            scope = lifecycleScope
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationRoute = intent.getStringExtra(TaskReminderWorker.EXTRA_NOTIFICATION_ROUTE)

        requestNotificationPermissionIfNeeded()
        FirebaseBootstrap.initializeIfPossible(applicationContext)

        setContent {
            TorresAgroTheme {
                TorresAgroApp(
                    repository = repository,
                    notificationRoute = notificationRoute,
                    onNotificationRouteConsumed = { notificationRoute = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationRoute = intent.getStringExtra(TaskReminderWorker.EXTRA_NOTIFICATION_ROUTE)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
