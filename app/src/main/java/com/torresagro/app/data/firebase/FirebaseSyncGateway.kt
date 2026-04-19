package com.torresagro.app.data.firebase

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.torresagro.app.data.local.AgroDatabase
import com.torresagro.app.data.local.entity.ActivityRecordEntity
import com.torresagro.app.data.local.entity.CropObservationEntity
import com.torresagro.app.data.local.entity.CropTaskEntity
import com.torresagro.app.data.local.entity.ParcelEntity
import com.torresagro.app.data.local.entity.SyncQueueEntity
import com.torresagro.app.data.repository.SyncGateway
import kotlinx.coroutines.tasks.await

class FirebaseSyncGateway(
    private val context: Context,
    private val database: AgroDatabase
) : SyncGateway {
    private val dao = database.agroDao()

    override suspend fun pushPendingChanges() {
        if (!FirebaseBootstrap.initializeIfPossible(context)) return
        val uid = FirebaseAuthManager().ensureSignedIn() ?: return
        val firestore = FirebaseFirestore.getInstance()
        val queue = dao.getSyncQueueItems()

        queue.forEach { item ->
            when (item.entityType) {
                "parcel" -> syncParcel(uid, firestore, item)
                "task" -> syncTask(uid, firestore, item)
                "activity" -> syncActivity(uid, firestore, item)
                "observation" -> syncObservation(uid, firestore, item)
            }
            dao.deleteSyncQueueItem(item.id)
        }
    }

    override suspend fun pullLatestData() {
        if (!FirebaseBootstrap.initializeIfPossible(context)) return
        val uid = FirebaseAuthManager().ensureSignedIn() ?: return
        val firestore = FirebaseFirestore.getInstance()

        val parcels = firestore.collection(userPath(uid, "parcels")).get().await().documents.mapNotNull { doc ->
            doc.toObject(ParcelRemote::class.java)?.toEntity(doc.id)
        }
        val tasks = firestore.collection(userPath(uid, "tasks")).get().await().documents.mapNotNull { doc ->
            doc.toObject(TaskRemote::class.java)?.toEntity(doc.id)
        }
        val activities = firestore.collection(userPath(uid, "activities")).get().await().documents.mapNotNull { doc ->
            doc.toObject(ActivityRemote::class.java)?.toEntity(doc.id)
        }
        val observations = firestore.collection(userPath(uid, "observations")).get().await().documents.mapNotNull { doc ->
            doc.toObject(ObservationRemote::class.java)?.toEntity(doc.id)
        }

        database.withTransaction {
            dao.clearParcels()
            dao.clearTasks()
            dao.clearActivities()
            dao.clearObservations()
            dao.upsertParcels(parcels)
            dao.upsertTasks(tasks)
            dao.upsertActivities(activities)
            dao.upsertObservations(observations)
        }
    }

    private suspend fun syncParcel(uid: String, firestore: FirebaseFirestore, item: SyncQueueEntity) {
        val ref = firestore.collection(userPath(uid, "parcels")).document(item.entityId)
        if (item.operation == "DELETE") {
            ref.delete().await()
            return
        }
        val parcel = dao.findParcelById(item.entityId) ?: return
        ref.set(ParcelRemote.from(parcel)).await()
    }

    private suspend fun syncTask(uid: String, firestore: FirebaseFirestore, item: SyncQueueEntity) {
        val ref = firestore.collection(userPath(uid, "tasks")).document(item.entityId)
        if (item.operation == "DELETE") {
            ref.delete().await()
            return
        }
        val task = dao.findTaskById(item.entityId) ?: return
        ref.set(TaskRemote.from(task)).await()
    }

    private suspend fun syncActivity(
        uid: String,
        firestore: FirebaseFirestore,
        item: SyncQueueEntity
    ) {
        val ref = firestore.collection(userPath(uid, "activities")).document(item.entityId)
        if (item.operation == "DELETE") {
            ref.delete().await()
            return
        }
        val activity = dao.findActivityById(item.entityId) ?: return
        val uploadedPhoto = uploadIfNeeded(activity.photoUri)
        val payload = ActivityRemote.from(activity.copy(photoUri = uploadedPhoto))
        ref.set(payload).await()
        if (uploadedPhoto != activity.photoUri) {
            dao.upsertActivities(listOf(activity.copy(photoUri = uploadedPhoto)))
        }
    }

    private suspend fun syncObservation(
        uid: String,
        firestore: FirebaseFirestore,
        item: SyncQueueEntity
    ) {
        val ref = firestore.collection(userPath(uid, "observations")).document(item.entityId)
        if (item.operation == "DELETE") {
            ref.delete().await()
            return
        }
        val observation = dao.findObservationById(item.entityId) ?: return
        val uploadedPhoto = uploadIfNeeded(observation.photoUri)
        val payload = ObservationRemote.from(observation.copy(photoUri = uploadedPhoto))
        ref.set(payload).await()
        if (uploadedPhoto != observation.photoUri) {
            dao.upsertObservations(listOf(observation.copy(photoUri = uploadedPhoto)))
        }
    }

    private suspend fun uploadIfNeeded(photoUri: String?): String? {
        // La version gratuita tiene limitaciones de Storage. 
        // Por ahora mantenemos la foto localmente y evitamos el error de subida.
        return photoUri
    }

    private fun userPath(uid: String, collection: String) = "users/$uid/$collection"
}

data class ParcelRemote(
    val name: String = "",
    val locationName: String = "",
    val sizeHectares: Double = 0.0,
    val cropType: String = "",
    val variety: String = "",
    val sowingDate: String = "",
    val expectedHarvestDate: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    fun toEntity(id: String) = ParcelEntity(
        id = id,
        name = name,
        locationName = locationName,
        sizeHectares = sizeHectares,
        cropType = cropType,
        variety = variety,
        sowingDate = sowingDate,
        expectedHarvestDate = expectedHarvestDate,
        latitude = latitude,
        longitude = longitude,
        offlinePendingSync = false
    )

    companion object {
        fun from(entity: ParcelEntity) = ParcelRemote(
            name = entity.name,
            locationName = entity.locationName,
            sizeHectares = entity.sizeHectares,
            cropType = entity.cropType,
            variety = entity.variety,
            sowingDate = entity.sowingDate,
            expectedHarvestDate = entity.expectedHarvestDate,
            latitude = entity.latitude,
            longitude = entity.longitude
        )
    }
}

data class TaskRemote(
    val parcelId: String = "",
    val title: String = "",
    val dueDate: String = "",
    val taskType: String = "",
    val completed: Boolean = false,
    val priority: String = "",
    val reminderEnabled: Boolean = false
) {
    fun toEntity(id: String) = CropTaskEntity(id, parcelId, title, dueDate, taskType, completed, priority, reminderEnabled)

    companion object {
        fun from(entity: CropTaskEntity) = TaskRemote(
            parcelId = entity.parcelId,
            title = entity.title,
            dueDate = entity.dueDate,
            taskType = entity.taskType,
            completed = entity.completed,
            priority = entity.priority,
            reminderEnabled = entity.reminderEnabled
        )
    }
}

data class ActivityRemote(
    val parcelId: String = "",
    val activityType: String = "",
    val date: String = "",
    val cost: Double = 0.0,
    val quantity: String = "",
    val notes: String = "",
    val photoUri: String? = null
) {
    fun toEntity(id: String) = ActivityRecordEntity(id, parcelId, activityType, date, cost, quantity, notes, photoUri)

    companion object {
        fun from(entity: ActivityRecordEntity) = ActivityRemote(
            parcelId = entity.parcelId,
            activityType = entity.activityType,
            date = entity.date,
            cost = entity.cost,
            quantity = entity.quantity,
            notes = entity.notes,
            photoUri = entity.photoUri
        )
    }
}

data class ObservationRemote(
    val parcelId: String = "",
    val date: String = "",
    val cropStage: String = "",
    val generalStatus: String = "",
    val symptoms: String = "",
    val recommendation: String = "",
    val photoUri: String? = null
) {
    fun toEntity(id: String) = CropObservationEntity(id, parcelId, date, cropStage, generalStatus, symptoms, recommendation, photoUri)

    companion object {
        fun from(entity: CropObservationEntity) = ObservationRemote(
            parcelId = entity.parcelId,
            date = entity.date,
            cropStage = entity.cropStage,
            generalStatus = entity.generalStatus,
            symptoms = entity.symptoms,
            recommendation = entity.recommendation,
            photoUri = entity.photoUri
        )
    }
}
