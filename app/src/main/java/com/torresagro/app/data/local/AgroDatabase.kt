package com.torresagro.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 8,
    exportSchema = true
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
                    .addMigrations(
                        MIGRATION_1_8,
                        MIGRATION_2_8,
                        MIGRATION_3_8,
                        MIGRATION_4_8,
                        MIGRATION_5_8,
                        MIGRATION_6_8,
                        MIGRATION_7_8
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_8 = userScopedMigration(1)
        private val MIGRATION_2_8 = userScopedMigration(2)
        private val MIGRATION_3_8 = userScopedMigration(3)
        private val MIGRATION_4_8 = userScopedMigration(4)
        private val MIGRATION_5_8 = userScopedMigration(5)
        private val MIGRATION_6_8 = userScopedMigration(6)
        private val MIGRATION_7_8 = userScopedMigration(7)

        private fun userScopedMigration(startVersion: Int): Migration =
            object : Migration(startVersion, 8) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    listOf(
                        "parcels",
                        "crop_tasks",
                        "activity_records",
                        "crop_observations",
                        "inventory_items",
                        "harvest_records",
                        "sync_queue",
                        "weather_cache",
                        "agri_data_cache"
                    ).forEach { table ->
                        db.addTextColumnIfMissing(table, "userId", "anonymous")
                    }
                }
            }

        private fun SupportSQLiteDatabase.addTextColumnIfMissing(
            table: String,
            column: String,
            defaultValue: String
        ) {
            val existingColumns = query("PRAGMA table_info(`$table`)").use { cursor ->
                buildSet {
                    val nameIndex = cursor.getColumnIndex("name")
                    while (cursor.moveToNext()) {
                        add(cursor.getString(nameIndex))
                    }
                }
            }
            if (column !in existingColumns) {
                execSQL("ALTER TABLE `$table` ADD COLUMN `$column` TEXT NOT NULL DEFAULT '$defaultValue'")
            }
        }
    }
}
