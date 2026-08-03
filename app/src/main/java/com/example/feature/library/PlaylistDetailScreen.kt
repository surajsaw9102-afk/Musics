package com.example.feature.library

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.catalog.MusicCatalog
import com.example.core.components.AuraButton
import com.example.core.components.AuraEmptyState
import com.example.core.database.entities.SongEntity
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    onBackClick: () -> Unit,
    onPlaySong: (SongEntity) -> Unit,
    onPlayPlaylist: (List<SongEntity>) -> Unit,
    libraryState: com.example.core.state.LibraryState,
    modifier: Modifier = Modifier,
    testTag: String = "playlist_detail_screen"
) {
    val context = LocalContext.current
    val libraryData by libraryState.libraryDataState.collectAsState()
    val playlist = libraryData.playlists.find { it.id == playlistId }

    var showEditDialog by remember { mutableStateOf(false) }
    var showAddSongsDialog by remember { mutableStateOf(false) }

    if (playlist == null) {
        AuraEmptyState(
            title = "Playlist Not Found",
            description = "This playlist may have been deleted or removed.",
            actionButtonText = "Back to Library",
            onActionClick = onBackClick,
            modifier = modifier.padding(24.dp)
        )
        return
    }

    val songList = remember(playlist.songIds) {
        playlist.songIds.mapNotNull { MusicCatalog.getSongById(it) }
    }

    val totalDurationMs = songList.sumOf { it.durationMs }
    val totalMinutes = totalDurationMs / 60000
    val totalSeconds = (totalDurationMs % 60000) / 1000
    val formattedDuration = "${totalMinutes}m ${totalSeconds}s"

    Column(
        modifier = modifier
            .testTag(testTag)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Action Bar
        TopAppBar(
            title = {
                Text(
                    text = playlist.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { libraryState.togglePinPlaylist(playlist.id) }) {
                    Icon(
                        imageVector = if (playlist.pinned) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Pin Playlist",
                        tint = if (playlist.pinned) AuraColors.NeonCyan else Color.White
                    )
                }

                IconButton(onClick = { showEditDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Playlist",
                        tint = Color.White
                    )
                }

                IconButton(onClick = {
                    val dup = libraryState.duplicatePlaylist(playlist.id)
                    if (dup != null) {
                        Toast.makeText(context, "Duplicated into '${dup.name}'", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate Playlist",
                        tint = Color.White
                    )
                }

                IconButton(onClick = {
                    libraryState.deletePlaylist(playlist.id)
                    Toast.makeText(context, "Playlist deleted", Toast.LENGTH_SHORT).show()
                    onBackClick()
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Playlist",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AuraRadius.Large))
                        .auraGlass(shape = RoundedCornerShape(AuraRadius.Large))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    AuraColors.ElectricPurple.copy(alpha = 0.35f),
                                    AuraColors.DarkSurfaceVariant
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Cover Art Image
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(AuraRadius.Medium))
                                .auraGlass(shape = RoundedCornerShape(AuraRadius.Medium))
                        ) {
                            AsyncImage(
                                model = playlist.coverUrl,
                                contentDescription = playlist.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = playlist.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        if (playlist.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = playlist.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Metadata line
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = playlist.ownerName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AuraColors.NeonCyan
                            )
                            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${songList.size} tracks",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = formattedDuration,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Play & Shuffle Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AuraButton(
                                text = "Play",
                                icon = Icons.Default.PlayArrow,
                                onClick = {
                                    if (songList.isNotEmpty()) {
                                        onPlayPlaylist(songList)
                                    }
                                },
                                enabled = songList.isNotEmpty(),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedButton(
                                onClick = {
                                    if (songList.isNotEmpty()) {
                                        onPlayPlaylist(songList.shuffled())
                                    }
                                },
                                enabled = songList.isNotEmpty(),
                                shape = RoundedCornerShape(AuraRadius.Medium),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = AuraColors.NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Shuffle", color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Add Tracks Action
                        Button(
                            onClick = { showAddSongsDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AuraColors.ElectricPurple.copy(alpha = 0.3f),
                                contentColor = AuraColors.NeonCyan
                            ),
                            shape = RoundedCornerShape(AuraRadius.Medium),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Tracks")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Tracks to Playlist", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tracklist Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tracklist (${songList.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Empty Tracklist State
            if (songList.isEmpty()) {
                item {
                    AuraEmptyState(
                        title = "Playlist is Empty",
                        description = "Tap 'Add Tracks to Playlist' above to populate your playlist.",
                        actionButtonText = "Add Tracks Now",
                        onActionClick = { showAddSongsDialog = true },
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                // Tracks List
                itemsIndexed(songList, key = { index, song -> "${song.id}_$index" }) { index, song ->
                    val isLiked = libraryData.likedSongs.any { it.id == song.id }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AuraRadius.Small))
                            .auraGlass(shape = RoundedCornerShape(AuraRadius.Small))
                            .clickable {
                                libraryState.recordSongPlay(song.id)
                                onPlaySong(song)
                            }
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Track Number
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(24.dp)
                            )

                            // Thumbnail Cover
                            AsyncImage(
                                model = song.coverUrl,
                                contentDescription = song.title,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(AuraRadius.Small)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${song.artistName} • ${song.audioQuality}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Move Up / Move Down Reordering Actions
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (index > 0) {
                                    IconButton(
                                        onClick = { libraryState.reorderSongsInPlaylist(playlist.id, index, index - 1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = "Move Up",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                if (index < songList.size - 1) {
                                    IconButton(
                                        onClick = { libraryState.reorderSongsInPlaylist(playlist.id, index, index + 1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "Move Down",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Like button
                                IconButton(
                                    onClick = { libraryState.toggleLikeSong(song.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Like Song",
                                        tint = if (isLiked) AuraColors.NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Remove Song button
                                IconButton(
                                    onClick = { libraryState.removeSongFromPlaylist(playlist.id, song.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove Track",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Dialogs
    if (showEditDialog) {
        CreateEditPlaylistDialog(
            initialName = playlist.name,
            initialDescription = playlist.description,
            initialCoverUrl = playlist.coverUrl,
            isEditing = true,
            onDismiss = { showEditDialog = false },
            onSave = { name, desc, cover ->
                libraryState.updatePlaylistDetails(playlist.id, name, desc, cover)
                showEditDialog = false
            }
        )
    }

    if (showAddSongsDialog) {
        AddSongsToPlaylistDialog(
            existingSongIds = playlist.songIds,
            onDismiss = { showAddSongsDialog = false },
            onAddSongs = { selectedSongIds ->
                libraryState.addSongsToPlaylist(playlist.id, selectedSongIds)
                showAddSongsDialog = false
            }
        )
    }
}
