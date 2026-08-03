package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "library_items")
data class LibraryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val itemId: String,
    val itemType: String, // "SONG", "ALBUM", "ARTIST", "PLAYLIST"
    val addedAt: Long = System.currentTimeMillis()
)
