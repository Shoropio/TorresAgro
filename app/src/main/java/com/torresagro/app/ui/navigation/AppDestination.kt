package com.torresagro.app.ui.navigation

import androidx.annotation.StringRes
import com.torresagro.app.R

sealed class AppDestination(val route: String, @StringRes val label: Int) {
    data object Splash : AppDestination("splash", R.string.notif_daily) 
    data object Login : AppDestination("login", R.string.sign_in_google)
    data object Home : AppDestination("home", R.string.nav_home)
    data object Parcels : AppDestination("parcels", R.string.nav_parcels)
    data object NewParcel : AppDestination("new_parcel", R.string.add_parcel_btn)
    data object EditParcel : AppDestination("edit_parcel", R.string.save_btn)
    data object ParcelDetail : AppDestination("parcel_detail", R.string.parcels_title)
    
    data object Tasks : AppDestination("tasks", R.string.nav_tasks)
    data object NewTask : AppDestination("new_task", R.string.nav_tasks)
    data object EditTask : AppDestination("edit_task", R.string.nav_tasks)
    
    data object NewActivity : AppDestination("new_activity", R.string.activity_action)
    data object EditActivity : AppDestination("edit_activity", R.string.activity_action)
    
    data object NewObservation : AppDestination("new_observation", R.string.crop_monitoring)
    data object EditObservation : AppDestination("edit_observation", R.string.crop_monitoring)
    
    data object Inventory : AppDestination("inventory", R.string.nav_inventory)
    data object NewInventoryItem : AppDestination("new_inventory", R.string.nav_inventory)
    data object EditInventoryItem : AppDestination("edit_inventory", R.string.nav_inventory)
    
    data object Settings : AppDestination("settings", R.string.nav_settings)
    data object MapParcel : AppDestination("map_parcel", R.string.map_title)
}
