package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val albumId: String,
    val albumTitle: String,
    val coverUrl: String,
    val durationMs: Long,
    val audioUrl: String,
    val isHdAudio: Boolean = true,
    val isExplicit: Boolean = false,
    val genre: String = "Electronic",
    val releaseYear: Int = 2026,
    val audioQuality: String = "Lossless FLAC 24-bit / 96kHz",
    val bitrate: String = "1411 kbps",
    val codec: String = "FLAC",
    val isAvailable: Boolean = true
)
