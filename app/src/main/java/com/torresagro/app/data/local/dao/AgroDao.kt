package com.torresagro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.torresagro.app.data.local.entity.ActivityRecordEntity
import com.torresagro.app.data.local.entity.CropObservationEntity
import com.torresagro.app.data.local.entity.CropTaskEntity
import com.torresagro.app.data.local.entity.HarvestRecordEntity
import com.torresagro.app.data.local.entity.InventoryItemEntity
import com.torresagro.app.data.local.entity.ParcelEntity
import com.torresagro.app.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgroDao {
    @Query("SELECT * FROM parcels")
    fun observeParcels(): Flow<List<ParcelEntity>>

    @Query("SELECT * FROM crop_tasks")
    fun observeTasks(): Flow<List<CropTaskEntity>>

    @Query("SELECT * FROM activity_records")
    fun observeActivities(): Flow<List<ActivityRecordEntity>>

    @Query("SELECT * FROM crop_observations")
    fun observeObservations(): Flow<List<CropObservationEntity>>

    @Query("SELECT * FROM inventory_items")
    fun observeInventory(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM harvest_records")
    fun observeHarvests(): Flow<List<HarvestRecordEntity>>

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

    @Query("UPDATE crop_tasks SET completed = 1 WHERE id = :taskId")
    suspend fun markTaskCompleted(taskId: String)

    @Query("SELECT COUNT(*) FROM parcels")
    suspend fun parcelCount(): Int

    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    suspend fun getSyncQueueItems(): List<SyncQueueEntity>

    @Query("DELETE FROM sync_queue WHERE id = :queueId")
    suspend fun deleteSyncQueueItem(queueId: String)

    @Query("SELECT * FROM parcels WHERE id = :parcelId LIMIT 1")
    suspend fun findParcelById(parcelId: String): ParcelEntity?

    @Query("SELECT * FROM crop_tasks WHERE id = :taskId LIMIT 1")
    suspend fun findTaskById(taskId: String): CropTaskEntity?

    @Query("SELECT * FROM activity_records WHERE id = :activityId LIMIT 1")
    suspend fun findActivityById(activityId: String): ActivityRecordEntity?

    @Query("SELECT * FROM crop_observations WHERE id = :observationId LIMIT 1")
    suspend fun findObservationById(observationId: String): CropObservationEntity?

    @Query("DELETE FROM parcels WHERE id = :parcelId")
    suspend fun deleteParcel(parcelId: String)

    @Query("DELETE FROM activity_records WHERE id = :activityId")
    suspend fun deleteActivity(activityId: String)

    @Query("DELETE FROM crop_observations WHERE id = :observationId")
    suspend fun deleteObservation(observationId: String)

    @Query("DELETE FROM crop_tasks WHERE parcelId = :parcelId")
    suspend fun deleteTasksByParcel(parcelId: String)

    @Query("DELETE FROM crop_tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: String)

    @Query("DELETE FROM crop_observations WHERE parcelId = :parcelId")
    suspend fun deleteObservationsByParcel(parcelId: String)

    @Query("DELETE FROM activity_records WHERE parcelId = :parcelId")
    suspend fun deleteActivitiesByParcel(parcelId: String)

    @Query("DELETE FROM parcels")
    suspend fun clearParcels()

    @Query("DELETE FROM crop_tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM activity_records")
    suspend fun clearActivities()

    @Query("DELETE FROM crop_observations")
    suspend fun clearObservations()
}
