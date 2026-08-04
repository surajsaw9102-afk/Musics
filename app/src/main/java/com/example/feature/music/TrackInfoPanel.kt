package com.example.feature.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import com.example.core.components.AudioQualityBadge
import com.example.core.components.ExplicitBadge
import com.example.core.components.LyricsAvailableBadge
import com.example.core.components.MetadataRenderer
import com.example.core.database.entities.SongEntity
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass
import com.example.core.repository.DefaultMetadataRepository
import com.example.core.repository.ExtendedTrackMetadata

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackInfoPanel(
    song: SongEntity,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleLike: () -> Unit,
    isLiked: Boolean,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "track_info_panel"
) {
    val metadataRepo = remember { DefaultMetadataRepository() }
    var extendedMetadata by remember { mutableStateOf<ExtendedTrackMetadata?>(null) }

    LaunchedEffect(song.id) {
        metadataRepo.getTrackMetadata(song.id).collect { result ->
            if (result is com.example.core.api.NetworkResult.Success) {
                extendedMetadata = result.data
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AuraColors.DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(topStart = AuraRadius.ExtraLarge, topEnd = AuraRadius.ExtraLarge),
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card with Cover Art, Title, Artist, Album
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AuraRadius.Large))
                    .auraGlass(shape = RoundedCornerShape(AuraRadius.Large))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                AuraColors.DarkSurfaceVariant,
                                AuraColors.ElectricPurple.copy(alpha = 0.25f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(AuraRadius.Medium))
                    ) {
                        AsyncImage(
                            model = song.coverUrl,
                            contentDescription = song.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (song.isExplicit) {
                                ExplicitBadge()
                            }
                        }

                        Text(
                            text = song.artistName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AuraColors.NeonCyan,
                            modifier = Modifier.clickable {
                                onDismiss()
                                onNavigateToArtist(song.artistId)
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${song.albumTitle} • ${song.releaseYear}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable {
                                onDismiss()
                                onNavigateToAlbum(song.albumId)
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = onToggleLike) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like Song",
                            tint = if (isLiked) AuraColors.MagentaPulse else Color.White
                        )
                    }
                }
            }

            // Badges Row (Audio Quality, Lyrics, Duration)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AudioQualityBadge(qualityText = song.audioQuality)
                LyricsAvailableBadge(isAvailable = extendedMetadata?.lyricsAvailable ?: true)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = MetadataRenderer.formatDuration(song.durationMs),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(color = AuraColors.DarkGlassBorder)

            // Audio Specs Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AuraRadius.Medium))
                    .background(AuraColors.DarkSurfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "TECHNICAL METADATA",
                    style = MaterialTheme.typography.labelSmall,
                    color = AuraColors.NeonCyan,
                    fontWeight = FontWeight.Bold
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Format & Codec", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${song.codec} (${song.bitrate})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Genre / Style", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(song.genre, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Track Position", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Track ${extendedMetadata?.trackNumber ?: 1} of ${extendedMetadata?.totalAlbumTracks ?: 10}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Copyright", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(extendedMetadata?.copyright ?: "© 2026 Aura Music", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            // Quick Actions List
            var showShareSheet by remember { mutableStateOf(false) }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ListItem(
                    headlineContent = { Text("Share Song", fontWeight = FontWeight.SemiBold, color = Color.White) },
                    leadingContent = { Icon(Icons.Default.Share, contentDescription = null, tint = AuraColors.NeonCyan) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        showShareSheet = true
                    }
                )

                ListItem(
                    headlineContent = { Text("Play Now", fontWeight = FontWeight.SemiBold, color = Color.White) },
                    leadingContent = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AuraColors.NeonCyan) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        onDismiss()
                        onPlay()
                    }
                )

                ListItem(
                    headlineContent = { Text("Add to Playing Queue", fontWeight = FontWeight.SemiBold, color = Color.White) },
                    leadingContent = { Icon(Icons.Default.Queue, contentDescription = null, tint = AuraColors.NeonCyan) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        onDismiss()
                        onAddToQueue()
                    }
                )

                ListItem(
                    headlineContent = { Text("Add to Playlist", fontWeight = FontWeight.SemiBold, color = Color.White) },
                    leadingContent = { Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = AuraColors.NeonCyan) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        onDismiss()
                        onAddToPlaylist()
                    }
                )

                ListItem(
                    headlineContent = { Text("View Artist Page", fontWeight = FontWeight.SemiBold, color = Color.White) },
                    leadingContent = { Icon(Icons.Default.Person, contentDescription = null, tint = AuraColors.NeonCyan) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        onDismiss()
                        onNavigateToArtist(song.artistId)
                    }
                )

                ListItem(
                    headlineContent = { Text("View Album Page", fontWeight = FontWeight.SemiBold, color = Color.White) },
                    leadingContent = { Icon(Icons.Default.Album, contentDescription = null, tint = AuraColors.NeonCyan) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        onDismiss()
                        onNavigateToAlbum(song.albumId)
                    }
                )
            }

            if (showShareSheet) {
                com.example.feature.social.AuraShareSheet(
                    shareContent = com.example.feature.social.ShareContent(
                        contentType = com.example.feature.social.ShareContentType.SONG,
                        id = song.id,
                        title = song.title,
                        subtitle = song.artistName,
                        imageUrl = song.coverUrl,
                        shareUrl = com.example.feature.social.ShareManager.createShareUrl(com.example.feature.social.ShareContentType.SONG, song.id),
                        deepLink = com.example.feature.social.ShareManager.createDeepLink(com.example.feature.social.ShareContentType.SONG, song.id),
                        description = "Stream '${song.title}' 100% Free on Aura Music",
                        song = song
                    ),
                    onDismiss = { showShareSheet = false },
                    onPlayClick = {
                        showShareSheet = false
                        onDismiss()
                        onPlay()
                    }
                )
            }
        }
    }
}
