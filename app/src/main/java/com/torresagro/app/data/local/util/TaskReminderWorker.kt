package com.torresagro.app.data.local.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.torresagro.app.R

class TaskReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        ensureChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val taskId = inputData.getString(KEY_TASK_ID).orEmpty()
        val title = inputData.getString(KEY_TITLE).orEmpty()
        val parcelName = inputData.getString(KEY_PARCEL_NAME).orEmpty()
        val dueDate = inputData.getString(KEY_DUE_DATE).orEmpty()

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Recordatorio de labor")
            .setContentText("$title - $parcelName")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Tarea: $title\nParcela: $parcelName\nFecha programada: $dueDate"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(taskId.hashCode(), notification)
        return Result.success()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Recordatorios agricolas",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alertas locales para labores programadas"
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "task_reminders"
        const val KEY_TASK_ID = "task_id"
        const val KEY_TITLE = "title"
        const val KEY_PARCEL_NAME = "parcel_name"
        const val KEY_DUE_DATE = "due_date"

        fun buildInputData(taskId: String, title: String, parcelName: String, dueDate: String): Data {
            return Data.Builder()
                .putString(KEY_TASK_ID, taskId)
                .putString(KEY_TITLE, title)
                .putString(KEY_PARCEL_NAME, parcelName)
                .putString(KEY_DUE_DATE, dueDate)
                .build()
        }
    }
}
