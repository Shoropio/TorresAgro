package com.torresagro.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.torresagro.app.data.firebase.FirebaseBootstrap
import com.torresagro.app.data.firebase.FirebaseSyncGateway
import com.torresagro.app.data.local.AgroDatabase
import com.torresagro.app.data.local.SeedData
import com.torresagro.app.data.local.util.TaskReminderScheduler
import com.torresagro.app.data.repository.RoomAgroRepository
import com.torresagro.app.ui.TorresAgroApp
import com.torresagro.app.ui.theme.TorresAgroTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AgroDatabase::class.java,
            "torres-agro.db"
        ).fallbackToDestructiveMigration().build()
    }

    private val repository by lazy {
        val syncGateway = FirebaseSyncGateway(applicationContext, database)
        RoomAgroRepository(
            dao = database.agroDao(),
            syncGateway = syncGateway,
            reminderScheduler = TaskReminderScheduler(applicationContext),
            scope = lifecycleScope
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // OSMDroid Initialization
        org.osmdroid.config.Configuration.getInstance().userAgentValue = packageName

        requestNotificationPermissionIfNeeded()
        FirebaseBootstrap.initializeIfPossible(applicationContext)
        
        lifecycleScope.launch {
            try {
                SeedData.populateIfEmpty(database)
            } catch (e: Exception) {
                android.util.Log.e("TorresAgro", "Error en SeedData: ${e.message}")
            }
        }
        
        lifecycleScope.launch {
            runCatching {
                repository.pushPendingChangesForStartup()
                repository.pullLatestDataForStartup()
            }.onFailure {
                android.util.Log.e("TorresAgro", "Error inicial de Firebase: ${it.message}")
            }
        }

        setContent {
            TorresAgroTheme {
                TorresAgroApp(repository = repository)
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
