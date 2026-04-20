package com.torresagro.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.torresagro.app.data.local.dao.AgroDao
import com.torresagro.app.data.local.entity.ActivityRecordEntity
import com.torresagro.app.data.local.entity.CropObservationEntity
import com.torresagro.app.data.local.entity.CropTaskEntity
import com.torresagro.app.data.local.entity.HarvestRecordEntity
import com.torresagro.app.data.local.entity.InventoryItemEntity
import com.torresagro.app.data.local.entity.ParcelEntity
import com.torresagro.app.data.local.entity.AgriDataEntity
import com.torresagro.app.data.local.entity.SyncQueueEntity
import com.torresagro.app.data.local.entity.WeatherCacheEntity

@Database(
    entities = [
        ParcelEntity::class,
        CropTaskEntity::class,
        ActivityRecordEntity::class,
        CropObservationEntity::class,
        InventoryItemEntity::class,
        HarvestRecordEntity::class,
        SyncQueueEntity::class,
        WeatherCacheEntity::class,
        AgriDataEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AgroDatabase : RoomDatabase() {
    abstract fun agroDao(): AgroDao

    companion object {
        @Volatile
        private var INSTANCE: AgroDatabase? = null

        fun getDatabase(context: Context): AgroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgroDatabase::class.java,
                    "agro_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
