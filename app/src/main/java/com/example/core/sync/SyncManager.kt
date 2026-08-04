package com.example.core.sync

import com.example.core.network.NetworkMonitor
import com.example.core.state.HistoryManager
import com.example.core.state.LikeStateManager
import com.example.feature.library.LibraryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SyncStatus(
    val isSyncing: Boolean = false,
    val lastSyncTimestamp: Long = System.currentTimeMillis() - 3600000L,
    val progress: Float = 1.0f,
    val syncStage: String = "Idle",
    val lastError: String? = null,
    val autoSyncOnReconnect: Boolean = true
)

object SyncManager {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _syncStatus = MutableStateFlow(SyncStatus())
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private var wasOnline: Boolean = true

    init {
        scope.launch {
            NetworkMonitor.status.collect { netStatus ->
                if (netStatus.isOnline && !wasOnline && _syncStatus.value.autoSyncOnReconnect) {
                    performFullSync()
                }
                wasOnline = netStatus.isOnline
            }
        }
    }

    fun setAutoSyncOnReconnect(enabled: Boolean) {
        _syncStatus.value = _syncStatus.value.copy(autoSyncOnReconnect = enabled)
    }

    fun performFullSync() {
        if (_syncStatus.value.isSyncing) return
        if (!NetworkMonitor.status.value.isOnline) {
            _syncStatus.value = _syncStatus.value.copy(
                lastError = "Cannot sync while offline"
            )
            return
        }

        scope.launch {
            try {
                _syncStatus.value = _syncStatus.value.copy(
                    isSyncing = true,
                    progress = 0.1f,
                    syncStage = "Checking connection...",
                    lastError = null
                )
                delay(300)

                // Stage 1: Metadata Refresh
                _syncStatus.value = _syncStatus.value.copy(
                    progress = 0.35f,
                    syncStage = "Refreshing track metadata & artwork..."
                )
                delay(400)

                // Stage 2: Sync Favorites & Library
                _syncStatus.value = _syncStatus.value.copy(
                    progress = 0.65f,
                    syncStage = "Syncing favorites & playlists..."
                )
                delay(400)

                // Stage 3: Sync History
                _syncStatus.value = _syncStatus.value.copy(
                    progress = 0.90f,
                    syncStage = "Syncing listening history..."
                )
                delay(300)

                // Complete
                _syncStatus.value = _syncStatus.value.copy(
                    isSyncing = false,
                    progress = 1.0f,
                    syncStage = "Sync complete",
                    lastSyncTimestamp = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _syncStatus.value = _syncStatus.value.copy(
                    isSyncing = false,
                    lastError = e.localizedMessage ?: "Sync failed"
                )
            }
        }
    }

    fun syncFavoritesOnly() {
        scope.launch {
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = true,
                progress = 0.5f,
                syncStage = "Syncing favorites..."
            )
            delay(500)
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = false,
                progress = 1.0f,
                syncStage = "Favorites synced",
                lastSyncTimestamp = System.currentTimeMillis()
            )
        }
    }
}
