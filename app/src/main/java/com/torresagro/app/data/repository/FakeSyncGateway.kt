package com.torresagro.app.data.repository

class FakeSyncGateway : SyncGateway {
    override suspend fun pushPendingChanges() = Unit
    override suspend fun pullLatestData() = Unit
}
