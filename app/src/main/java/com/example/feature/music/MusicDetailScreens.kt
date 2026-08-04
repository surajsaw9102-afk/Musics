package com.example.feature.music

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity
import com.example.core.state.LibraryState
import com.example.core.state.PlayerState
import com.example.feature.library.AlbumDetailScreen as LibraryAlbumDetailScreen
import com.example.feature.library.ArtistDetailScreen as LibraryArtistDetailScreen

@Composable
fun ArtistDetailScreen(
    artistId: String,
    onBackClick: () -> Unit,
    onPlaySong: (SongEntity) -> Unit,
    onAlbumSelect: (AlbumEntity) -> Unit = {},
    onArtistSelect: (ArtistEntity) -> Unit = {},
    playerState: PlayerState? = null,
    libraryState: LibraryState? = null,
    modifier: Modifier = Modifier,
    testTag: String = "artist_detail_screen"
) {
    LibraryArtistDetailScreen(
        artistId = artistId,
        onBackClick = onBackClick,
        onPlaySong = onPlaySong,
        onAlbumSelect = onAlbumSelect,
        onArtistSelect = onArtistSelect,
        playerState = playerState,
        libraryState = libraryState,
        modifier = modifier,
        testTag = testTag
    )
}

@Composable
fun AlbumDetailScreen(
    albumId: String,
    onBackClick: () -> Unit,
    onPlaySong: (SongEntity) -> Unit,
    onArtistSelect: (String) -> Unit = {},
    onAlbumSelect: (AlbumEntity) -> Unit = {},
    playerState: PlayerState? = null,
    libraryState: LibraryState? = null,
    modifier: Modifier = Modifier,
    testTag: String = "album_detail_screen"
) {
    LibraryAlbumDetailScreen(
        albumId = albumId,
        onBackClick = onBackClick,
        onPlaySong = onPlaySong,
        onArtistSelect = onArtistSelect,
        onAlbumSelect = onAlbumSelect,
        playerState = playerState,
        libraryState = libraryState,
        modifier = modifier,
        testTag = testTag
    )
}
