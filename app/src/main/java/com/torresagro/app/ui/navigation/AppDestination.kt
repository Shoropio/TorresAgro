package com.torresagro.app.ui.navigation

sealed class AppDestination(val route: String, val label: String) {
    data object Splash : AppDestination("splash", "Splash")
    data object QuickAccess : AppDestination("quick_access", "Acceso")
    data object Home : AppDestination("home", "Inicio")
    data object Parcels : AppDestination("parcels", "Parcelas")
    data object NewParcel : AppDestination("new_parcel", "Nueva parcela")
    data object EditParcel : AppDestination("edit_parcel", "Editar parcela")
    data object ParcelDetail : AppDestination("parcel_detail", "Detalle")
    data object Tasks : AppDestination("tasks", "Tareas")
    data object NewTask : AppDestination("new_task", "Nueva tarea")
    data object EditTask : AppDestination("edit_task", "Editar tarea")
    data object NewActivity : AppDestination("new_activity", "Nueva actividad")
    data object EditActivity : AppDestination("edit_activity", "Editar actividad")
    data object NewObservation : AppDestination("new_observation", "Nuevo monitoreo")
    data object EditObservation : AppDestination("edit_observation", "Editar monitoreo")
    data object Inventory : AppDestination("inventory", "Inventario")
    data object Reports : AppDestination("reports", "Reportes")
    data object MapParcel : AppDestination("map_parcel", "Mapear Parcela")
}
