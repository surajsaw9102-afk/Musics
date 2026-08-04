package com.example.core.repository

import com.example.core.api.NetworkResult
import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.SongEntity
import com.example.core.provider.MusicProviderRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface AlbumRepository {
    fun getAlbumDetails(albumId: String): Flow<NetworkResult<AlbumEntity>>
    fun getAlbumTracks(albumId: String): Flow<NetworkResult<List<SongEntity>>>
    fun getRelatedAlbums(albumId: String, artistId: String): Flow<NetworkResult<List<AlbumEntity>>>
}

class DefaultAlbumRepository : AlbumRepository {
    override fun getAlbumDetails(albumId: String): Flow<NetworkResult<AlbumEntity>> = flow {
        val providerResult = MusicProviderRegistry.getActive().getAlbumDetails(albumId)
        if (providerResult is NetworkResult.Success<AlbumEntity>) {
            emit(providerResult)
        } else {
            val fallback = MusicCatalog.getAlbumById(albumId)
            if (fallback != null) {
                emit(NetworkResult.Success(fallback))
            } else {
                emit(NetworkResult.Error(code = 404, message = "Album not found: $albumId"))
            }
        }
    }

    override fun getAlbumTracks(albumId: String): Flow<NetworkResult<List<SongEntity>>> = flow {
        val tracks = MusicCatalog.getSongsByAlbum(albumId)
        emit(NetworkResult.Success(tracks))
    }

    override fun getRelatedAlbums(albumId: String, artistId: String): Flow<NetworkResult<List<AlbumEntity>>> = flow {
        val related = MusicCatalog.FEATURED_ALBUMS.filter { it.id != albumId }
        emit(NetworkResult.Success(related))
    }
}
