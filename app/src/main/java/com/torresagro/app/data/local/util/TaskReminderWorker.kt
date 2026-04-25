package com.torresagro.app.data.local.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.torresagro.app.MainActivity
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
        val taskRoute = "edit_task/$taskId"

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Recordatorio de labor")
            .setContentText("$title - $parcelName")
            .setContentIntent(openAppPendingIntent(taskRoute, taskId.hashCode()))
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Tarea: $title\nParcela: $parcelName\nFecha programada: $dueDate"
                )
            )
            .addAction(
                R.drawable.ic_launcher,
                "Ver tarea",
                openAppPendingIntent(taskRoute, taskId.hashCode() + 1)
            )
            .addAction(
                R.drawable.ic_launcher,
                "Calendario",
                openAppPendingIntent("tasks", taskId.hashCode() + 2)
            )
            .addAction(
                R.drawable.ic_launcher,
                "Registrar actividad",
                openAppPendingIntent("new_activity", taskId.hashCode() + 3)
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

    private fun openAppPendingIntent(route: String, requestCode: Int): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            action = "$ACTION_OPEN_ROUTE:$route"
            putExtra(EXTRA_NOTIFICATION_ROUTE, route)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            applicationContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_ID = "task_reminders"
        const val EXTRA_NOTIFICATION_ROUTE = "com.torresagro.app.NOTIFICATION_ROUTE"
        private const val ACTION_OPEN_ROUTE = "com.torresagro.app.action.OPEN_ROUTE"
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
