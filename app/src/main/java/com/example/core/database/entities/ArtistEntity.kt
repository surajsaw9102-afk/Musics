package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val avatarUrl: String,
    val bio: String = "",
    val monthlyListeners: Long = 0,
    val isVerified: Boolean = true
)
