package com.example.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.core.database.dao.LibraryDao
import com.example.core.database.dao.SongDao
import com.example.core.database.dao.UserDao
import com.example.core.database.entities.*

@Database(
    entities = [
        UserEntity::class,
        ArtistEntity::class,
        AlbumEntity::class,
        SongEntity::class,
        PlaylistEntity::class,
        LibraryEntity::class,
        FavoriteEntity::class,
        HistoryEntity::class,
        DownloadEntity::class,
        CacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun songDao(): SongDao
    abstract fun libraryDao(): LibraryDao
}
