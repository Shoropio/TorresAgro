package com.torresagro.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.torresagro.app.data.repository.AgroRepository
import com.torresagro.app.ui.navigation.AppDestination
import com.torresagro.app.ui.screen.HomeScreen
import com.torresagro.app.ui.screen.InventoryScreen
import com.torresagro.app.ui.screen.NewActivityScreen
import com.torresagro.app.ui.screen.NewParcelScreen
import com.torresagro.app.ui.screen.ObservationFormScreen
import com.torresagro.app.ui.screen.ParcelDetailScreen
import com.torresagro.app.ui.screen.ParcelsScreen
import com.torresagro.app.ui.screen.QuickAccessScreen
import com.torresagro.app.ui.screen.ReportsScreen
import com.torresagro.app.ui.screen.SplashScreen
import com.torresagro.app.ui.screen.TaskFormScreen
import com.torresagro.app.ui.screen.TasksScreen
import com.torresagro.app.ui.viewmodel.AppViewModel
import com.torresagro.app.ui.viewmodel.AppViewModelFactory

@Composable
fun TorresAgroApp(repository: AgroRepository) {
    val navController = rememberNavController()
    val factory = AppViewModelFactory(repository)
    val viewModel: AppViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val topLevel = listOf(
        AppDestination.Home,
        AppDestination.Parcels,
        AppDestination.Tasks,
        AppDestination.Inventory,
        AppDestination.Reports
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in topLevel.map { it.route }) {
                NavigationBar {
                    topLevel.forEach { destination ->
                        val icon = when (destination) {
                            AppDestination.Home -> Icons.Outlined.Home
                            AppDestination.Parcels -> Icons.Outlined.Map
                            AppDestination.Tasks -> Icons.Outlined.Today
                            AppDestination.Inventory -> Icons.Outlined.Inventory2
                            AppDestination.Reports -> Icons.Outlined.Assessment
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
                            icon = { Icon(imageVector = icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(AppDestination.Splash.route) {
                LaunchedEffect(Unit) {
                    navController.navigate(AppDestination.QuickAccess.route) {
                        popUpTo(AppDestination.Splash.route) { inclusive = true }
                    }
                }
                SplashScreen()
            }
            composable(AppDestination.QuickAccess.route) {
                QuickAccessScreen(
                    onContinue = {
                        navController.navigate(AppDestination.Home.route) {
                            popUpTo(AppDestination.QuickAccess.route) { inclusive = true }
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
                    onRefreshWeather = viewModel::refreshWeather
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
                    }
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
            composable("${AppDestination.ParcelDetail.route}/{parcelId}") { backStackEntry ->
                val parcelId = backStackEntry.arguments?.getString("parcelId").orEmpty()
                ParcelDetailScreen(
                    parcel = state.parcels.firstOrNull { it.id == parcelId },
                    activities = state.activities.filter { it.parcelId == parcelId },
                    observations = state.observations.filter { it.parcelId == parcelId },
                    onAddActivity = { navController.navigate("${AppDestination.NewActivity.route}/$parcelId") },
                    onEditParcel = { navController.navigate("${AppDestination.EditParcel.route}/$parcelId") },
                    onEditActivity = { activityId ->
                        navController.navigate("${AppDestination.EditActivity.route}/$activityId")
                    },
                    onAddObservation = { navController.navigate("${AppDestination.NewObservation.route}/$parcelId") },
                    onEditObservation = { observationId ->
                        navController.navigate("${AppDestination.EditObservation.route}/$observationId")
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
            composable(AppDestination.NewTask.route) {
                TaskFormScreen(
                    parcels = state.parcels,
                    onSave = { parcelId, title, dueDate, taskType, priority, reminderEnabled, _ ->
                        viewModel.addTask(parcelId, title, dueDate, taskType, priority, reminderEnabled)
                        navController.popBackStack()
                    }
                )
            }
            composable("${AppDestination.EditTask.route}/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId").orEmpty()
                val task = state.tasks.firstOrNull { it.id == taskId }
                TaskFormScreen(
                    parcels = state.parcels,
                    initialTask = task,
                    onSave = { parcelId, title, dueDate, taskType, priority, reminderEnabled, completed ->
                        viewModel.updateTask(taskId, parcelId, title, dueDate, taskType, priority, reminderEnabled, completed)
                        navController.popBackStack()
                    },
                    onDelete = {
                        viewModel.deleteTask(taskId)
                        navController.popBackStack()
                    }
                )
            }
            composable(AppDestination.NewActivity.route) {
                NewActivityScreen(
                    parcels = state.parcels,
                    onSave = { parcelId, activityType, date, cost, quantity, notes, photoUri ->
                        viewModel.addActivity(parcelId, activityType, date, cost, quantity, notes, photoUri)
                        navController.popBackStack()
                    }
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
                    }
                )
            }
            composable("${AppDestination.EditActivity.route}/{activityId}") { backStackEntry ->
                val activityId = backStackEntry.arguments?.getString("activityId").orEmpty()
                val activity = state.activities.firstOrNull { it.id == activityId }
                NewActivityScreen(
                    parcels = state.parcels,
                    initialActivity = activity,
                    onSave = { parcelId, activityType, date, cost, quantity, notes, photoUri ->
                        viewModel.updateActivity(activityId, parcelId, activityType, date, cost, quantity, notes, photoUri)
                        navController.popBackStack()
                    },
                    onDelete = {
                        viewModel.deleteActivity(activityId)
                        navController.popBackStack()
                    }
                )
            }
            composable("${AppDestination.NewObservation.route}/{parcelId}") { backStackEntry ->
                val parcelId = backStackEntry.arguments?.getString("parcelId").orEmpty()
                ObservationFormScreen(
                    parcels = state.parcels,
                    preselectedParcelId = parcelId,
                    onSave = { selectedParcelId, date, cropStage, generalStatus, symptoms, recommendation, photoUri ->
                        viewModel.addObservation(selectedParcelId, date, cropStage, generalStatus, symptoms, recommendation, photoUri)
                        navController.popBackStack()
                    }
                )
            }
            composable("${AppDestination.EditObservation.route}/{observationId}") { backStackEntry ->
                val observationId = backStackEntry.arguments?.getString("observationId").orEmpty()
                val observation = state.observations.firstOrNull { it.id == observationId }
                ObservationFormScreen(
                    parcels = state.parcels,
                    initialObservation = observation,
                    onSave = { parcelId, date, cropStage, generalStatus, symptoms, recommendation, photoUri ->
                        viewModel.updateObservation(observationId, parcelId, date, cropStage, generalStatus, symptoms, recommendation, photoUri)
                        navController.popBackStack()
                    },
                    onDelete = {
                        viewModel.deleteObservation(observationId)
                        navController.popBackStack()
                    }
                )
            }
            composable(AppDestination.Inventory.route) {
                InventoryScreen(state = state)
            }
            composable(AppDestination.Reports.route) {
                ReportsScreen(state = state)
            }
        }
    }
}
