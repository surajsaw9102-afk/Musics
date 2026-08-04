package com.example.feature.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.core.repository.DefaultArtistRepository
import com.example.core.state.FollowStateManager
import com.example.core.state.LikeStateManager
import com.example.core.state.PlayerState
import com.example.feature.music.TrackInfoPanel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistId: String,
    onBackClick: () -> Unit,
    onPlaySong: (SongEntity) -> Unit,
    onAlbumSelect: (AlbumEntity) -> Unit = {},
    onArtistSelect: (ArtistEntity) -> Unit = {},
    playerState: PlayerState? = null,
    libraryState: com.example.core.state.LibraryState? = null,
    modifier: Modifier = Modifier,
    testTag: String = "artist_detail_screen"
) {
    val repository = remember { DefaultArtistRepository() }

    var artist by remember { mutableStateOf<ArtistEntity?>(null) }
    var topSongs by remember { mutableStateOf<List<SongEntity>>(emptyList()) }
    var albums by remember { mutableStateOf<List<AlbumEntity>>(emptyList()) }
    var singles by remember { mutableStateOf<List<AlbumEntity>>(emptyList()) }
    var relatedArtists by remember { mutableStateOf<List<ArtistEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var isBioExpanded by remember { mutableStateOf(false) }
    var selectedSongForInfoPanel by remember { mutableStateOf<SongEntity?>(null) }

    // Follow & Like state
    val followedArtistIds by FollowStateManager.followedArtistIds.collectAsState(initial = emptySet())
    val likedTrackIds by LikeStateManager.likedTrackIds.collectAsState(initial = emptySet())

    val isFollowed = artistId in followedArtistIds

    LaunchedEffect(artistId) {
        isLoading = true
        repository.getArtistDetails(artistId).collectLatest { result ->
            if (result is com.example.core.api.NetworkResult.Success) {
                artist = result.data
            }
        }
        repository.getArtistTopSongs(artistId).collectLatest { result ->
            if (result is com.example.core.api.NetworkResult.Success) {
                topSongs = result.data
            }
        }
        repository.getArtistAlbums(artistId).collectLatest { result ->
            if (result is com.example.core.api.NetworkResult.Success) {
                albums = result.data
            }
        }
        repository.getArtistSingles(artistId).collectLatest { result ->
            if (result is com.example.core.api.NetworkResult.Success) {
                singles = result.data
            }
        }
        repository.getRelatedArtists(artistId).collectLatest { result ->
            if (result is com.example.core.api.NetworkResult.Success) {
                relatedArtists = result.data
            }
        }
        isLoading = false
    }

    val currentArtist = artist

    Scaffold(
        modifier = modifier
            .testTag(testTag)
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (isLoading && currentArtist == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AuraColors.NeonCyan)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading artist details...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (currentArtist == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PersonOff, contentDescription = null, tint = AuraColors.NeonCyan, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Artist not found", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackClick) { Text("Go Back") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Parallax/Hero Header
                item {
                    ArtworkHeader(
                        type = HeaderType.ARTIST,
                        title = currentArtist.name,
                        subtitle = "Verified Artist",
                        imageUrl = currentArtist.avatarUrl,
                        isVerified = currentArtist.isVerified,
                        monthlyListenersText = MetadataRenderer.formatMonthlyListeners(currentArtist.monthlyListeners),
                        isPrimaryActionActive = isFollowed,
                        primaryActionLabel = if (isFollowed) "Following" else "Follow",
                        onPrimaryActionClick = { FollowStateManager.toggleFollow(currentArtist.id) },
                        onBackClick = onBackClick,
                        onPlayAllClick = {
                            if (topSongs.isNotEmpty()) {
                                onPlaySong(topSongs.first())
                            }
                        },
                        onShuffleClick = {
                            if (topSongs.isNotEmpty()) {
                                onPlaySong(topSongs.shuffled().first())
                            }
                        },
                        onShareClick = { /* Share action */ }
                    )
                }

                // Genre Tags Row
                item {
                    val genres = topSongs.map { it.genre }.distinct()
                    if (genres.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(genres) { genre ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(AuraRadius.Pill))
                                        .background(AuraColors.DarkSurfaceVariant)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("#$genre", style = MaterialTheme.typography.labelMedium, color = AuraColors.NeonCyan)
                                }
                            }
                        }
                    }
                }

                // Biography Card (Expandable with fallback)
                item {
                    val bioText = if (currentArtist.bio.isNotBlank()) currentArtist.bio else MetadataRenderer.getFallbackBio(currentArtist.name)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(AuraRadius.Medium))
                            .auraGlass(shape = RoundedCornerShape(AuraRadius.Medium))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        AuraColors.DarkSurfaceVariant,
                                        AuraColors.DarkSurface
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.animateContentSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "About the Artist",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                TextButton(onClick = { isBioExpanded = !isBioExpanded }) {
                                    Text(
                                        text = if (isBioExpanded) "Show Less" else "Read More",
                                        color = AuraColors.NeonCyan,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = bioText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = if (isBioExpanded) Int.MAX_VALUE else 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Top Popular Tracks
                item {
                    Text(
                        text = "Popular Tracks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                if (topSongs.isEmpty()) {
                    item {
                        Text(
                            text = "No songs available for this artist yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    itemsIndexed(topSongs, key = { _, song -> song.id }) { index, song ->
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

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(AuraRadius.Small))
                                ) {
                                    AsyncImage(
                                        model = song.coverUrl,
                                        contentDescription = song.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

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
                                        text = "${song.albumTitle} • ${MetadataRenderer.formatDuration(song.durationMs)}",
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

                // Albums & EPs
                item {
                    if (albums.isNotEmpty()) {
                        RelatedAlbumsSection(
                            title = "Discography (Albums & EPs)",
                            albums = albums,
                            onAlbumSelect = onAlbumSelect
                        )
                    }
                }

                // Singles
                item {
                    if (singles.isNotEmpty()) {
                        RelatedAlbumsSection(
                            title = "Singles",
                            albums = singles,
                            onAlbumSelect = onAlbumSelect
                        )
                    }
                }

                // Related / Similar Artists
                item {
                    if (relatedArtists.isNotEmpty()) {
                        RelatedArtistsSection(
                            title = "Fans Also Like (Similar Artists)",
                            artists = relatedArtists,
                            onArtistSelect = onArtistSelect
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
                    onArtistSelect(ArtistEntity(id = id, name = song.artistName, avatarUrl = song.coverUrl))
                },
                onNavigateToAlbum = { id ->
                    selectedSongForInfoPanel = null
                    onAlbumSelect(AlbumEntity(id = id, title = song.albumTitle, artistId = song.artistId, artistName = song.artistName, coverUrl = song.coverUrl))
                }
            )
        }
    }
}
