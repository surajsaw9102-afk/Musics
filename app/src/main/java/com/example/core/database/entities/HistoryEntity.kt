package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_history")
data class HistoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val songId: String,
    val playedAt: Long = System.currentTimeMillis(),
    val playbackDurationMs: Long = 0
)
