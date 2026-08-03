package com.example.core.storage

import com.example.core.cache.SmartCacheManager
import com.example.core.downloads.DownloadManager
import com.example.core.downloads.DownloadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StorageStats(
    val musicStorageMb: Double = 38.4,
    val cacheStorageMb: Double = 42.5,
    val freeDeviceSpaceGb: Double = 48.2,
    val totalDeviceCapacityGb: Double = 128.0
) {
    val totalAppUsedMb: Double
        get() = musicStorageMb + cacheStorageMb

    val usedPercentageOfDevice: Float
        get() = ((totalAppUsedMb / 1024.0) / totalDeviceCapacityGb).toFloat().coerceIn(0.01f, 1.0f)
}

object StorageManager {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val _stats = MutableStateFlow(StorageStats())
    val stats: StateFlow<StorageStats> = _stats.asStateFlow()

    fun updateStorageStats() {
        val downloadedItems = DownloadManager.state.value.itemsMap.values.filter { it.state == DownloadState.DOWNLOADED }
        val musicMb = downloadedItems.sumOf { item ->
            if (item.totalBytes > 0) item.totalBytes / (1024.0 * 1024.0) else 32.0
        }

        val cacheMb = SmartCacheManager.stats.value.totalCacheSizeMb

        _stats.value = _stats.value.copy(
            musicStorageMb = (musicMb * 10.0).let { Math.round(it) / 10.0 },
            cacheStorageMb = cacheMb
        )
    }

    fun clearCache() {
        scope.launch {
            SmartCacheManager.clearAllCache()
            updateStorageStats()
        }
    }

    fun deleteAllDownloads() {
        DownloadManager.removeAllDownloads()
        updateStorageStats()
    }
}
