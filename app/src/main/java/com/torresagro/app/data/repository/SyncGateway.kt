package com.torresagro.app.data.repository

interface SyncGateway {
    suspend fun pushPendingChanges()
    suspend fun pullLatestData()
}
