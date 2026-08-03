package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entities.FavoriteEntity
import com.example.core.database.entities.LibraryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    @Query("SELECT * FROM library_items WHERE userId = :userId ORDER BY addedAt DESC")
    fun getUserLibrary(userId: String): Flow<List<LibraryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToLibrary(item: LibraryEntity)

    @Query("DELETE FROM library_items WHERE userId = :userId AND itemId = :itemId")
    suspend fun removeFromLibrary(userId: String, itemId: String)

    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getUserFavorites(userId: String): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE userId = :userId AND songId = :songId")
    suspend fun removeFavorite(userId: String, songId: String)
}
