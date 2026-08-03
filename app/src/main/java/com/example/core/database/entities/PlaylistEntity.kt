package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val coverUrl: String = "",
    val ownerId: String,
    val trackCount: Int = 0,
    val isPublic: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
