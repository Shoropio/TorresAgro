package com.torresagro.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.torresagro.app.data.local.dao.AgroDao
import com.torresagro.app.data.local.entity.ActivityRecordEntity
import com.torresagro.app.data.local.entity.CropObservationEntity
import com.torresagro.app.data.local.entity.CropTaskEntity
import com.torresagro.app.data.local.entity.HarvestRecordEntity
import com.torresagro.app.data.local.entity.InventoryItemEntity
import com.torresagro.app.data.local.entity.ParcelEntity
import com.torresagro.app.data.local.entity.SyncQueueEntity

@Database(
    entities = [
        ParcelEntity::class,
        CropTaskEntity::class,
        ActivityRecordEntity::class,
        CropObservationEntity::class,
        InventoryItemEntity::class,
        HarvestRecordEntity::class,
        SyncQueueEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AgroDatabase : RoomDatabase() {
    abstract fun agroDao(): AgroDao
}
