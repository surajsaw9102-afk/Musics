package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_entries")
data class CacheEntity(
    @PrimaryKey val id: String,
    val cacheKey: String,
    val cachedFilePath: String,
    val cacheSizeBytes: Long,
    val lastAccessedAt: Long = System.currentTimeMillis()
)
