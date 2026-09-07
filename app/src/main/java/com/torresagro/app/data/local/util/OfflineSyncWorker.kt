package com.torresagro.app.data.local.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.torresagro.app.data.repository.SyncGateway

class OfflineSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val syncGateway = SyncGatewayHolder.get() ?: return Result.failure()
        return runCatching {
            syncGateway.pushPendingChanges()
            syncGateway.pullLatestData()
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }
}

object SyncGatewayHolder {
    @Volatile
    private var instance: SyncGateway? = null

    fun set(gateway: SyncGateway) {
        instance = gateway
    }

    fun get(): SyncGateway? = instance
}
