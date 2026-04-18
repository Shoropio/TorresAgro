package com.torresagro.app.data.local.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.torresagro.app.data.repository.SyncGateway

class OfflineSyncWorker(
    appContext: Context,
    params: WorkerParameters,
    private val syncGateway: SyncGateway
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return runCatching {
            syncGateway.pushPendingChanges()
            syncGateway.pullLatestData()
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }
}
