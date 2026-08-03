package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val username: String,
    val email: String,
    val photoUrl: String? = null,
    val language: String = "English",
    val country: String = "United States",
    val themeMode: String = "DARK",
    val isFreeMember: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
