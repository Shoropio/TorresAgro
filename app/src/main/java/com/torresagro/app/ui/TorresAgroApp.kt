package com.torresagro.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.navigation.*
import androidx.navigation.compose.*
import android.content.Intent
import androidx.core.content.FileProvider
import com.torresagro.app.data.report.ReportService
import com.torresagro.app.data.repository.AgroRepository
import com.torresagro.app.ui.navigation.AppDestination
import com.torresagro.app.ui.screen.*
import com.torresagro.app.ui.theme.AccentSky
import com.torresagro.app.ui.viewmodel.AppViewModel
import com.torresagro.app.ui.viewmodel.AppViewModelFactory

@Composable
fun TorresAgroApp(repository: AgroRepository) {
    val navController = rememberNavController()
    val factory = AppViewModelFactory(repository)
    val viewModel: AppViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val authManager = remember { com.torresagro.app.data.firebase.FirebaseAuthManager() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val reportService = remember { ReportService(context) }
    val topLevel = listOf(
        AppDestination.Home,
        AppDestination.Parcels,
        AppDestination.Tasks,
        AppDestination.Inventory,
        AppDestination.Settings
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in topLevel.map { it.route }) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    topLevel.forEach { destination ->
                        val icon = when (destination) {
                            AppDestination.Home -> Icons.Outlined.Home
                            AppDestination.Parcels -> Icons.Outlined.Map
                            AppDestination.Tasks -> Icons.Outlined.Today
                            AppDestination.Inventory -> Icons.Outlined.Inventory2
                            AppDestination.Settings -> Icons.Outlined.Settings
                            else -> Icons.Outlined.Home
                        }
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = icon, contentDescription = stringResource(destination.label)) },
                            label = { Text(stringResource(destination.label)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = AccentSky.copy(alpha = 0.14f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Splash.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(animationSpec = tween(400)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(400)) },
            popEnterTransition = { fadeIn(animationSpec = tween(400)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(400)) },
            popExitTransition = { fadeOut(animationSpec = tween(400)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(400)) }
        ) {
            composable(AppDestination.Splash.route) {
                LaunchedEffect(Unit) {
                    val userId = authManager.currentUid()
                    if (userId != null) {
                        navController.navigate(AppDestination.Home.route) {
                            popUpTo(AppDestination.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(AppDestination.Login.route) {
                            popUpTo(AppDestination.Splash.route) { inclusive = true }
                        }
                    }
                }
                SplashScreen()
            }
            composable(AppDestination.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(AppDestination.Home.route) {
                            popUpTo(AppDestination.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(AppDestination.Home.route) {
                HomeScreen(
                    state = state,
                    onCompleteTask = viewModel::completeTask,
                    onAddParcel = { navController.navigate(AppDestination.NewParcel.route) },
                    onAddActivity = { navController.navigate(AppDestination.NewActivity.route) },
                    onAlertsClick = { navController.navigate(AppDestination.AlertsCenter.route) },
                    onOpenAgriMap = { navController.navigate(AppDestination.AgriMap.route) },
                    onRefreshWeather = viewModel::refreshWeather,
                    onRefreshCurrentLocationWeather = viewModel::refreshWeatherForCoordinates
                )
            }
            composable(AppDestination.Parcels.route) {
                ParcelsScreen(
                    state = state,
                    onOpenParcel = { navController.navigate("${AppDestination.ParcelDetail.route}/$it") },
                    onAddParcel = { navController.navigate(AppDestination.NewParcel.route) }
                )
            }
            composable(AppDestination.NewParcel.route) {
                val boundaryResult = it.savedStateHandle.get<List<Pair<Double, Double>>>("points")
                val areaResult = it.savedStateHandle.get<Double>("area")
                
                NewParcelScreen(
                    updatedBoundary = boundaryResult,
                    calculatedArea = areaResult,
                    onSave = { name, location, size, cropType, variety, sowingDate, latitude, longitude, pts ->
                        viewModel.addParcel(name, location, size, cropType, variety, sowingDate, latitude, longitude, pts)
                        navController.popBackStack()
                    },
                    onOpenMap = { pts ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("points", pts)
                        navController.navigate(AppDestination.MapParcel.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("${AppDestination.EditParcel.route}/{parcelId}") { backStackEntry ->
                val parcelId = backStackEntry.arguments?.getString("parcelId").orEmpty()
                val parcel = state.parcels.firstOrNull { it.id == parcelId }
                val boundaryResult = backStackEntry.savedStateHandle.get<List<Pair<Double, Double>>>("points")
                val areaResult = backStackEntry.savedStateHandle.get<Double>("area")

                NewParcelScreen(
                    initialParcel = parcel,
                    updatedBoundary = boundaryResult,
                    calculatedArea = areaResult,
                    onSave = { name, location, size, cropType, variety, sowingDate, latitude, longitude, pts ->
                        viewModel.updateParcel(parcelId, name, location, size, cropType, variety, sowingDate, latitude, longitude, pts)
                        navController.popBackStack()
                    },
                    onOpenMap = { pts ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("points", pts)
                        navController.navigate(AppDestination.MapParcel.route)
                    },
                    onBack = { navController.popBackStack() },
                    onDelete = {
                        viewModel.deleteParcel(parcelId)
                        navController.navigate(AppDestination.Parcels.route) {
                            popUpTo(AppDestination.Parcels.route) { inclusive = false }
                        }
                    }
                )
            }
            composable(AppDestination.MapParcel.route) {
                val initialPoints = navController.previousBackStackEntry?.savedStateHandle?.get<List<Pair<Double, Double>>>("points") ?: emptyList()
                com.torresagro.app.ui.screen.ParcelMapScreen(
                    initialPoints = initialPoints,
                    onConfirm = { pts, area ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("points", pts)
                        navController.previousBackStackEntry?.savedStateHandle?.set("area", area)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppDestination.AgriMap.route) {
                AgriMapScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onOpenParcel = { parcelId -> 
                        navController.navigate("${AppDestination.ParcelDetail.route}/$parcelId")
                    }
                )
            }
            composable("${AppDestination.ParcelDetail.route}/{parcelId}") { backStackEntry ->
                val parcelId = backStackEntry.arguments?.getString("parcelId").orEmpty()
                ParcelDetailScreen(
                    parcel = state.parcels.firstOrNull { it.id == parcelId },
                    weather = state.parcelWeatherById[parcelId],
                    activities = state.activities.filter { it.parcelId == parcelId },
                    observations = state.observations.filter { it.parcelId == parcelId },
                    agriData = state.parcelAgriData[parcelId],
                    onAddActivity = { navController.navigate("${AppDestination.NewActivity.route}/$parcelId") },
                    onEditParcel = { navController.navigate("${AppDestination.EditParcel.route}/$parcelId") },
                    onEditActivity = { activityId ->
                        navController.navigate("${AppDestination.EditActivity.route}/$activityId")
                    },
                    onAddObservation = { navController.navigate("${AppDestination.NewObservation.route}/$parcelId") },
                    onEditObservation = { observationId ->
                        navController.navigate("${AppDestination.EditObservation.route}/$observationId")
                    },
                    onDeleteParcel = { id ->
                        viewModel.deleteParcel(id)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() },
                    onRefreshWeather = viewModel::refreshWeather,
                    onRefreshSatellite = { viewModel.refreshSatelliteData(parcelId) },
                    onGenerateReport = { parcel, agriData ->
                        val file = reportService.generateParcelReport(parcel, agriData)
                        if (file != null) {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir Reporte"))
                        }
                    }
                )
            }
            composable(AppDestination.Tasks.route) {
                TasksScreen(
                    state = state,
                    onCompleteTask = viewModel::completeTask,
                    onAddTask = { navController.navigate(AppDestination.NewTask.route) },
                    onEditTask = { taskId -> navController.navigate("${AppDestination.EditTask.route}/$taskId") }
                )
            }
            composable(AppDestination.Inventory.route) {
                InventoryScreen(
                    state = state,
                    onAddItem = { navController.navigate(AppDestination.NewInventoryItem.route) },
                    onEditItem = { id -> navController.navigate("${AppDestination.EditInventoryItem.route}/$id") }
                )
            }
            composable(AppDestination.NewInventoryItem.route) {
                InventoryFormScreen(
                    onSave = { name, cat, stock, unit, min ->
                        viewModel.addInventoryItem(name, cat, stock, unit, min)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("${AppDestination.EditInventoryItem.route}/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id").orEmpty()
                val item = state.inventory.firstOrNull { it.id == id }
                InventoryFormScreen(
                    initialItem = item,
                    onSave = { name, cat, stock, unit, min ->
                        viewModel.updateInventoryItem(id, name, cat, stock, unit, min)
                        navController.popBackStack()
                    },
                    onDelete = {
                        viewModel.deleteInventoryItem(id)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppDestination.Settings.route) {
                SettingsScreen(
                    onSignOut = {
                        authManager.signOut()
                        navController.navigate(AppDestination.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            // Add other forms as needed...
            composable(AppDestination.NewTask.route) {
                TaskFormScreen(
                    parcels = state.parcels,
                    onSave = { parcelId, title, dueDate, taskType, priority, reminderEnabled, _ ->
                        viewModel.addTask(parcelId, title, dueDate, taskType, priority, reminderEnabled)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("${AppDestination.EditTask.route}/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId").orEmpty()
                val task = state.tasks.firstOrNull { it.id == taskId } ?: return@composable
                TaskFormScreen(
                    parcels = state.parcels,
                    initialTask = task,
                    onSave = { parcelId, title, dueDate, taskType, priority, reminder, completed ->
                        viewModel.updateTask(taskId, parcelId, title, dueDate, taskType, priority, reminder, completed)
                        navController.popBackStack()
                    },
                    onDelete = {
                        viewModel.deleteTask(taskId)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("${AppDestination.NewActivity.route}/{parcelId}") { backStackEntry ->
                val parcelId = backStackEntry.arguments?.getString("parcelId").orEmpty()
                NewActivityScreen(
                    parcels = state.parcels,
                    preselectedParcelId = parcelId,
                    onSave = { selectedParcelId, activityType, date, cost, quantity, notes, photoUri ->
                        viewModel.addActivity(selectedParcelId, activityType, date, cost, quantity, notes, photoUri)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("${AppDestination.EditActivity.route}/{activityId}") { backStackEntry ->
                val activityId = backStackEntry.arguments?.getString("activityId").orEmpty()
                val activity = state.activities.firstOrNull { it.id == activityId } ?: return@composable
                NewActivityScreen(
                    parcels = state.parcels,
                    initialActivity = activity,
                    onSave = { parcelId, type, date, cost, quantity, notes, photo ->
                        viewModel.updateActivity(activityId, parcelId, type, date, cost, quantity, notes, photo)
                        navController.popBackStack()
                    },
                    onDelete = {
                        viewModel.deleteActivity(activityId)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("${AppDestination.NewObservation.route}/{parcelId}") { backStackEntry ->
                val parcelId = backStackEntry.arguments?.getString("parcelId").orEmpty()
                ObservationFormScreen(
                    parcels = state.parcels,
                    preselectedParcelId = parcelId,
                    onSave = { selParcelId, date, stage, status, symptoms, recommendation, photo ->
                        viewModel.addObservation(selParcelId, date, stage, status, symptoms, recommendation, photo)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("${AppDestination.EditObservation.route}/{observationId}") { backStackEntry ->
                val observationId = backStackEntry.arguments?.getString("observationId").orEmpty()
                val obs = state.observations.firstOrNull { it.id == observationId } ?: return@composable
                ObservationFormScreen(
                    parcels = state.parcels,
                    initialObservation = obs,
                    onSave = { parcelId, date, stage, status, symptoms, rec, photo ->
                        viewModel.updateObservation(observationId, parcelId, date, stage, status, symptoms, rec, photo)
                        navController.popBackStack()
                    },
                    onDelete = {
                        viewModel.deleteObservation(observationId)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppDestination.AlertsCenter.route) {
                AlertsCenterScreen(
                    uiState = state,
                    onParcelClick = { parcelId ->
                        navController.navigate("${AppDestination.ParcelDetail.route}/$parcelId")
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
