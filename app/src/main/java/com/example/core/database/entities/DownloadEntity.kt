package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val songId: String,
    val localFilePath: String,
    val fileSizeMb: Double,
    val quality: String = "LOSSLESS_FLAC",
    val downloadedAt: Long = System.currentTimeMillis()
)
