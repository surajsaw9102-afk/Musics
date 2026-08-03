package com.example.core.cache

import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity
import com.example.feature.cache.CacheModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CachedItem<T>(
    val id: String,
    val data: T,
    val cachedAt: Long = System.currentTimeMillis(),
    val estimatedSizeMb: Double,
    val accessCount: Int = 1,
    val lastAccessedAt: Long = System.currentTimeMillis()
)

data class CacheStats(
    val totalCacheSizeMb: Double = 42.5,
    val maxCacheLimitMb: Double = 500.0,
    val cachedSongCount: Int = 3,
    val cachedAlbumCount: Int = 2,
    val cachedArtistCount: Int = 2,
    val totalCacheHits: Int = 128,
    val isAutoCleanupEnabled: Boolean = true
)

object SmartCacheManager : CacheModule {

    private val cachedSongsMap = mutableMapOf<String, CachedItem<SongEntity>>()
    private val cachedAlbumsMap = mutableMapOf<String, CachedItem<AlbumEntity>>()
    private val cachedArtistsMap = mutableMapOf<String, CachedItem<ArtistEntity>>()
    private val songPlayCountsMap = mutableMapOf<String, Int>()

    private val _stats = MutableStateFlow(CacheStats())
    val stats: StateFlow<CacheStats> = _stats.asStateFlow()

    init {
        // Pre-populate intelligent smart cache with starter items
        val s1 = MusicCatalog.getSongById("song_101")
        val s2 = MusicCatalog.getSongById("song_103")
        val alb1 = MusicCatalog.getAlbumById("alb_1")
        val art1 = MusicCatalog.getArtistById("art_1")

        if (s1 != null) cacheSong(s1, "initial")
        if (s2 != null) cacheSong(s2, "initial")
        if (alb1 != null) cacheAlbum(alb1)
        if (art1 != null) cacheArtist(art1)
    }

    // CacheModule interface compliance
    override suspend fun cacheAudioSegment(key: String, bytes: ByteArray): Boolean {
        // Simulated byte segment caching
        updateStats()
        return true
    }

    override suspend fun clearAllCache(): Boolean {
        cachedSongsMap.clear()
        cachedAlbumsMap.clear()
        cachedArtistsMap.clear()
        songPlayCountsMap.clear()
        updateStats()
        return true
    }

    fun recordSongPlay(song: SongEntity) {
        val current = songPlayCountsMap[song.id] ?: 0
        val newCount = current + 1
        songPlayCountsMap[song.id] = newCount

        // Auto cache frequently or recently played song
        cacheSong(song, "frequently_played")
    }

    fun cacheSong(song: SongEntity, source: String = "manual") {
        val estimatedMb = if (song.isHdAudio) 14.2 else 8.5
        val existing = cachedSongsMap[song.id]
        if (existing != null) {
            cachedSongsMap[song.id] = existing.copy(
                accessCount = existing.accessCount + 1,
                lastAccessedAt = System.currentTimeMillis()
            )
        } else {
            cachedSongsMap[song.id] = CachedItem(
                id = song.id,
                data = song,
                estimatedSizeMb = estimatedMb
            )
        }
        enforceCacheLimit()
        updateStats()
    }

    fun cacheAlbum(album: AlbumEntity) {
        cachedAlbumsMap[album.id] = CachedItem(
            id = album.id,
            data = album,
            estimatedSizeMb = 1.2
        )
        updateStats()
    }

    fun cacheArtist(artist: ArtistEntity) {
        cachedArtistsMap[artist.id] = CachedItem(
            id = artist.id,
            data = artist,
            estimatedSizeMb = 0.8
        )
        updateStats()
    }

    fun isSongCached(songId: String): Boolean {
        val item = cachedSongsMap[songId] ?: return false
        cachedSongsMap[songId] = item.copy(
            accessCount = item.accessCount + 1,
            lastAccessedAt = System.currentTimeMillis()
        )
        updateStats()
        return true
    }

    fun getCachedSongs(): List<SongEntity> {
        return cachedSongsMap.values.map { it.data }
    }

    fun setMaxCacheLimitMb(limitMb: Double) {
        _stats.value = _stats.value.copy(maxCacheLimitMb = limitMb)
        enforceCacheLimit()
    }

    private fun enforceCacheLimit() {
        var totalMb = calculateTotalCacheMb()
        val maxLimit = _stats.value.maxCacheLimitMb

        if (totalMb > maxLimit) {
            // Sort by least recently accessed
            val sortedList = cachedSongsMap.values.sortedBy { it.lastAccessedAt }
            for (item in sortedList) {
                if (totalMb <= maxLimit) break
                cachedSongsMap.remove(item.id)
                totalMb -= item.estimatedSizeMb
            }
        }
    }

    private fun calculateTotalCacheMb(): Double {
        val songSize = cachedSongsMap.values.sumOf { it.estimatedSizeMb }
        val albumSize = cachedAlbumsMap.values.sumOf { it.estimatedSizeMb }
        val artistSize = cachedArtistsMap.values.sumOf { it.estimatedSizeMb }
        return songSize + albumSize + artistSize
    }

    private fun updateStats() {
        val totalMb = calculateTotalCacheMb()
        _stats.value = _stats.value.copy(
            totalCacheSizeMb = (totalMb * 10.0).let { Math.round(it) / 10.0 },
            cachedSongCount = cachedSongsMap.size,
            cachedAlbumCount = cachedAlbumsMap.size,
            cachedArtistCount = cachedArtistsMap.size
        )
    }
}
