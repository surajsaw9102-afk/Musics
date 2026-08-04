package com.example.feature.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.components.*
import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass
import com.example.core.repository.DefaultAlbumRepository
import com.example.core.state.LikeStateManager
import com.example.core.state.PlayerState
import com.example.feature.music.TrackInfoPanel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumId: String,
    onBackClick: () -> Unit,
    onPlaySong: (SongEntity) -> Unit,
    onArtistSelect: (String) -> Unit = {},
    onAlbumSelect: (AlbumEntity) -> Unit = {},
    playerState: PlayerState? = null,
    libraryState: com.example.core.state.LibraryState? = null,
    modifier: Modifier = Modifier,
    testTag: String = "album_detail_screen"
) {
    val repository = remember { DefaultAlbumRepository() }

    var album by remember { mutableStateOf<AlbumEntity?>(null) }
    var albumTracks by remember { mutableStateOf<List<SongEntity>>(emptyList()) }
    var relatedAlbums by remember { mutableStateOf<List<AlbumEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedSongForInfoPanel by remember { mutableStateOf<SongEntity?>(null) }

    val savedAlbumIds by LikeStateManager.savedAlbumIds.collectAsState(initial = emptySet())
    val likedTrackIds by LikeStateManager.likedTrackIds.collectAsState(initial = emptySet())

    val isSaved = albumId in savedAlbumIds

    LaunchedEffect(albumId) {
        isLoading = true
        repository.getAlbumDetails(albumId).collectLatest { result ->
            if (result is com.example.core.api.NetworkResult.Success) {
                album = result.data
            }
        }
        repository.getAlbumTracks(albumId).collectLatest { result ->
            if (result is com.example.core.api.NetworkResult.Success) {
                albumTracks = result.data
            }
        }
        val currentArtistId = album?.artistId ?: ""
        repository.getRelatedAlbums(albumId, currentArtistId).collectLatest { result ->
            if (result is com.example.core.api.NetworkResult.Success) {
                relatedAlbums = result.data
            }
        }
        isLoading = false
    }

    val currentAlbum = album

    Scaffold(
        modifier = modifier
            .testTag(testTag)
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (isLoading && currentAlbum == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AuraColors.NeonCyan)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading album details...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (currentAlbum == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Album, contentDescription = null, tint = AuraColors.NeonCyan, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Album not found", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackClick) { Text("Go Back") }
                }
            }
        } else {
            val totalDurationMs = albumTracks.sumOf { it.durationMs }
            val totalMins = totalDurationMs / 60000

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Album Artwork Hero Header
                item {
                    ArtworkHeader(
                        type = HeaderType.ALBUM,
                        title = currentAlbum.title,
                        subtitle = "Album by ${currentAlbum.artistName}",
                        imageUrl = currentAlbum.coverUrl,
                        monthlyListenersText = "${currentAlbum.releaseYear} • ${albumTracks.size} Tracks (${totalMins} mins)",
                        isPrimaryActionActive = isSaved,
                        primaryActionLabel = if (isSaved) "Saved" else "Save Album",
                        onPrimaryActionClick = { LikeStateManager.toggleAlbumLike(currentAlbum.id) },
                        onBackClick = onBackClick,
                        onPlayAllClick = {
                            if (albumTracks.isNotEmpty()) {
                                onPlaySong(albumTracks.first())
                            }
                        },
                        onShuffleClick = {
                            if (albumTracks.isNotEmpty()) {
                                onPlaySong(albumTracks.shuffled().first())
                            }
                        },
                        onShareClick = { /* Share album */ }
                    )
                }

                // Audio Quality & Info Chips Bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val sampleQuality = albumTracks.firstOrNull()?.audioQuality ?: "Lossless FLAC 24-bit"
                        AudioQualityBadge(qualityText = sampleQuality)
                        LyricsAvailableBadge(isAvailable = true)

                        Spacer(modifier = Modifier.weight(1f))

                        TextButton(onClick = { onArtistSelect(currentAlbum.artistId) }) {
                            Text(
                                text = "By ${currentAlbum.artistName}",
                                color = AuraColors.NeonCyan,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Track List Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tracks (${albumTracks.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Track List Items
                if (albumTracks.isEmpty()) {
                    item {
                        Text(
                            text = "No tracks in this album.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    itemsIndexed(albumTracks, key = { _, song -> song.id }) { index, song ->
                        val isLiked = song.id in likedTrackIds

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(AuraRadius.Small))
                                .auraGlass(shape = RoundedCornerShape(AuraRadius.Small))
                                .clickable { onPlaySong(song) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraColors.NeonCyan,
                                    modifier = Modifier.width(20.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = song.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (song.isExplicit) ExplicitBadge()
                                    }
                                    Text(
                                        text = "${song.artistName} • ${MetadataRenderer.formatDuration(song.durationMs)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = { LikeStateManager.toggleTrackLike(song.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (isLiked) AuraColors.MagentaPulse else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = { selectedSongForInfoPanel = song },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.MoreVert,
                                        contentDescription = "More Options",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Related / More Albums Section
                item {
                    if (relatedAlbums.isNotEmpty()) {
                        RelatedAlbumsSection(
                            title = "More Albums You May Like",
                            albums = relatedAlbums,
                            onAlbumSelect = onAlbumSelect
                        )
                    }
                }
            }
        }

        // Track Info Expansion Bottom Sheet Panel
        selectedSongForInfoPanel?.let { song ->
            TrackInfoPanel(
                song = song,
                onDismiss = { selectedSongForInfoPanel = null },
                onPlay = {
                    onPlaySong(song)
                    selectedSongForInfoPanel = null
                },
                onAddToQueue = {
                    playerState?.addToQueue(song)
                    selectedSongForInfoPanel = null
                },
                onAddToPlaylist = {
                    selectedSongForInfoPanel = null
                },
                onToggleLike = {
                    LikeStateManager.toggleTrackLike(song.id)
                },
                isLiked = song.id in likedTrackIds,
                onNavigateToArtist = { id ->
                    selectedSongForInfoPanel = null
                    onArtistSelect(id)
                },
                onNavigateToAlbum = { id ->
                    selectedSongForInfoPanel = null
                    onAlbumSelect(AlbumEntity(id = id, title = song.albumTitle, artistId = song.artistId, artistName = song.artistName, coverUrl = song.coverUrl))
                }
            )
        }
    }
}
