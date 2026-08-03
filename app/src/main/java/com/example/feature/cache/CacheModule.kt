package com.example.feature.cache

interface CacheModule {
    suspend fun cacheAudioSegment(key: String, bytes: ByteArray): Boolean
    suspend fun clearAllCache(): Boolean
}
