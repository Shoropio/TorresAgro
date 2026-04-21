package com.torresagro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.torresagro.app.data.local.entity.ActivityRecordEntity
import com.torresagro.app.data.local.entity.CropObservationEntity
import com.torresagro.app.data.local.entity.CropTaskEntity
import com.torresagro.app.data.local.entity.AgriDataEntity
import com.torresagro.app.data.local.entity.HarvestRecordEntity
import com.torresagro.app.data.local.entity.InventoryItemEntity
import com.torresagro.app.data.local.entity.ParcelEntity
import com.torresagro.app.data.local.entity.SyncQueueEntity
import com.torresagro.app.data.local.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgroDao {
    @Query("SELECT * FROM agri_data_cache WHERE userId = :userId")
    fun observeAgriData(userId: String): Flow<List<AgriDataEntity>>
    @Query("SELECT * FROM parcels WHERE userId = :userId")
    fun observeParcels(userId: String): Flow<List<ParcelEntity>>

    @Query("SELECT * FROM crop_tasks WHERE userId = :userId")
    fun observeTasks(userId: String): Flow<List<CropTaskEntity>>

    @Query("SELECT * FROM activity_records WHERE userId = :userId")
    fun observeActivities(userId: String): Flow<List<ActivityRecordEntity>>

    @Query("SELECT * FROM crop_observations WHERE userId = :userId")
    fun observeObservations(userId: String): Flow<List<CropObservationEntity>>

    @Query("SELECT * FROM inventory_items WHERE userId = :userId")
    fun observeInventory(userId: String): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE userId = :userId")
    suspend fun getInventoryItems(userId: String): List<InventoryItemEntity>

    @Query("SELECT * FROM harvest_records WHERE userId = :userId")
    fun observeHarvests(userId: String): Flow<List<HarvestRecordEntity>>

    @Query("SELECT * FROM weather_cache WHERE userId = :userId")
    fun observeWeatherCache(userId: String): Flow<List<WeatherCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTasks(items: List<CropTaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertActivities(items: List<ActivityRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertObservations(items: List<CropObservationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInventory(items: List<InventoryItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHarvests(items: List<HarvestRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueSync(items: List<SyncQueueEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertParcels(items: List<ParcelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeatherCache(items: List<WeatherCacheEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAgriData(items: List<AgriDataEntity>)

    @Query("UPDATE crop_tasks SET completed = 1 WHERE id = :taskId AND userId = :userId")
    suspend fun markTaskCompleted(taskId: String, userId: String)

    @Query("SELECT COUNT(*) FROM parcels WHERE userId = :userId")
    suspend fun parcelCount(userId: String): Int

    @Query("SELECT * FROM sync_queue WHERE userId = :userId ORDER BY createdAt ASC")
    suspend fun getSyncQueueItems(userId: String): List<SyncQueueEntity>

    @Query("DELETE FROM sync_queue WHERE id = :queueId AND userId = :userId")
    suspend fun deleteSyncQueueItem(queueId: String, userId: String)

    @Query("SELECT * FROM parcels WHERE id = :parcelId AND userId = :userId LIMIT 1")
    suspend fun findParcelById(parcelId: String, userId: String): ParcelEntity?

    @Query("SELECT * FROM crop_tasks WHERE id = :taskId AND userId = :userId LIMIT 1")
    suspend fun findTaskById(taskId: String, userId: String): CropTaskEntity?

    @Query("SELECT * FROM activity_records WHERE id = :activityId AND userId = :userId LIMIT 1")
    suspend fun findActivityById(activityId: String, userId: String): ActivityRecordEntity?

    @Query("SELECT * FROM crop_observations WHERE id = :observationId AND userId = :userId LIMIT 1")
    suspend fun findObservationById(observationId: String, userId: String): CropObservationEntity?

    @Query("SELECT * FROM inventory_items WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun findInventoryItemById(id: String, userId: String): InventoryItemEntity?

    @Query("DELETE FROM parcels WHERE id = :parcelId AND userId = :userId")
    suspend fun deleteParcel(parcelId: String, userId: String)

    @Query("DELETE FROM weather_cache WHERE parcelId = :parcelId AND userId = :userId")
    suspend fun deleteWeatherCacheByParcel(parcelId: String, userId: String)

    @Query("DELETE FROM agri_data_cache WHERE parcelId = :parcelId AND userId = :userId")
    suspend fun deleteAgriDataByParcel(parcelId: String, userId: String)

    @Query("DELETE FROM activity_records WHERE id = :activityId AND userId = :userId")
    suspend fun deleteActivity(activityId: String, userId: String)

    @Query("DELETE FROM crop_observations WHERE id = :observationId AND userId = :userId")
    suspend fun deleteObservation(observationId: String, userId: String)

    @Query("DELETE FROM crop_tasks WHERE parcelId = :parcelId AND userId = :userId")
    suspend fun deleteTasksByParcel(parcelId: String, userId: String)

    @Query("DELETE FROM crop_tasks WHERE id = :taskId AND userId = :userId")
    suspend fun deleteTask(taskId: String, userId: String)

    @Query("DELETE FROM crop_observations WHERE parcelId = :parcelId AND userId = :userId")
    suspend fun deleteObservationsByParcel(parcelId: String, userId: String)

    @Query("DELETE FROM activity_records WHERE parcelId = :parcelId AND userId = :userId")
    suspend fun deleteActivitiesByParcel(parcelId: String, userId: String)

    @Query("DELETE FROM parcels WHERE userId = :userId")
    suspend fun clearParcels(userId: String)

    @Query("DELETE FROM crop_tasks WHERE userId = :userId")
    suspend fun clearTasks(userId: String)

    @Query("DELETE FROM activity_records WHERE userId = :userId")
    suspend fun clearActivities(userId: String)

    @Query("DELETE FROM crop_observations WHERE userId = :userId")
    suspend fun clearObservations(userId: String)

    @Query("DELETE FROM inventory_items WHERE userId = :userId")
    suspend fun clearInventory(userId: String)

    @Query("DELETE FROM inventory_items WHERE id = :id AND userId = :userId")
    suspend fun deleteInventoryItem(id: String, userId: String)
}
