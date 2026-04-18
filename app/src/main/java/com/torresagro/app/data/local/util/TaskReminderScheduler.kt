package com.torresagro.app.data.local.util

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.torresagro.app.domain.model.CropTask
import com.torresagro.app.domain.model.Parcel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class TaskReminderScheduler(
    private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun syncTasks(tasks: List<CropTask>, parcels: List<Parcel>) {
        val parcelNames = parcels.associateBy({ it.id }, { it.name })
        tasks.forEach { task ->
            if (task.reminderEnabled && !task.completed) {
                schedule(task, parcelNames[task.parcelId].orEmpty())
            } else {
                cancel(task.id)
            }
        }
    }

    fun schedule(task: CropTask, parcelName: String) {
        val delayMillis = computeDelayMillis(task.dueDate)
        val work = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                TaskReminderWorker.buildInputData(
                    taskId = task.id,
                    title = task.title,
                    parcelName = parcelName,
                    dueDate = task.dueDate
                )
            )
            .addTag(tagFor(task.id))
            .build()

        workManager.enqueueUniqueWork(uniqueName(task.id), ExistingWorkPolicy.REPLACE, work)
    }

    fun cancel(taskId: String) {
        workManager.cancelUniqueWork(uniqueName(taskId))
    }

    private fun computeDelayMillis(dueDate: String): Long {
        val target = runCatching {
            LocalDateTime.of(LocalDate.parse(dueDate), LocalTime.of(7, 0))
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrElse { System.currentTimeMillis() + 15_000L }

        return (target - System.currentTimeMillis()).coerceAtLeast(15_000L)
    }

    private fun uniqueName(taskId: String) = "task_reminder_$taskId"
    private fun tagFor(taskId: String) = "task_tag_$taskId"
}
