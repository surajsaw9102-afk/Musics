package com.example.core.repository

import com.example.core.api.NetworkResult
import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity
import com.example.core.provider.MusicProviderRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface ArtistRepository {
    fun getArtistDetails(artistId: String): Flow<NetworkResult<ArtistEntity>>
    fun getArtistTopSongs(artistId: String): Flow<NetworkResult<List<SongEntity>>>
    fun getArtistAlbums(artistId: String): Flow<NetworkResult<List<AlbumEntity>>>
    fun getArtistSingles(artistId: String): Flow<NetworkResult<List<AlbumEntity>>>
    fun getRelatedArtists(artistId: String): Flow<NetworkResult<List<ArtistEntity>>>
}

class DefaultArtistRepository : ArtistRepository {
    override fun getArtistDetails(artistId: String): Flow<NetworkResult<ArtistEntity>> = flow {
        val providerResult = MusicProviderRegistry.getActive().getArtistDetails(artistId)
        if (providerResult is NetworkResult.Success<ArtistEntity>) {
            emit(providerResult)
        } else {
            // Catalog fallback
            val fallback = MusicCatalog.getArtistById(artistId)
            if (fallback != null) {
                emit(NetworkResult.Success(fallback))
            } else {
                emit(NetworkResult.Error(code = 404, message = "Artist not found: $artistId"))
            }
        }
    }

    override fun getArtistTopSongs(artistId: String): Flow<NetworkResult<List<SongEntity>>> = flow {
        val songs = MusicCatalog.getSongsByArtist(artistId)
        emit(NetworkResult.Success(songs))
    }

    override fun getArtistAlbums(artistId: String): Flow<NetworkResult<List<AlbumEntity>>> = flow {
        val albums = MusicCatalog.FEATURED_ALBUMS.filter { it.artistId == artistId }
        emit(NetworkResult.Success(albums))
    }

    override fun getArtistSingles(artistId: String): Flow<NetworkResult<List<AlbumEntity>>> = flow {
        val singles = MusicCatalog.FEATURED_ALBUMS.filter { it.artistId == artistId && it.totalTracks <= 3 }
        emit(NetworkResult.Success(singles))
    }

    override fun getRelatedArtists(artistId: String): Flow<NetworkResult<List<ArtistEntity>>> = flow {
        val related = MusicCatalog.FEATURED_ARTISTS.filter { it.id != artistId }
        emit(NetworkResult.Success(related))
    }
}
