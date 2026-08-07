package com.example.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.database.entities.SongEntity
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOverflowSheet(
    song: SongEntity,
    onDismiss: () -> Unit,
    onPlayNow: () -> Unit = {},
    onPlayNext: () -> Unit = {},
    onPlayLater: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    onToggleLike: () -> Unit = {},
    isLiked: Boolean = false,
    onDownload: () -> Unit = {},
    isDownloaded: Boolean = false,
    onNavigateToArtist: ((String) -> Unit)? = null,
    onNavigateToAlbum: ((String) -> Unit)? = null,
    onShare: () -> Unit = {},
    onRemoveFromQueue: (() -> Unit)? = null,
    onStartRadio: (() -> Unit)? = null,
    onShowTrackInfo: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AuraColors.DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = AuraRadius.ExtraLarge, topEnd = AuraRadius.ExtraLarge),
        modifier = modifier.testTag("song_overflow_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AuraRadius.Large))
                    .auraGlass(shape = RoundedCornerShape(AuraRadius.Large))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                AuraColors.DarkSurfaceVariant,
                                AuraColors.ElectricPurple.copy(alpha = 0.2f)
                            )
                        )
                    )
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
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
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${song.albumTitle} • ${song.genre}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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

            // Quick Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AudioQualityBadge(qualityText = song.audioQuality)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = MetadataRenderer.formatDuration(song.durationMs),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(color = AuraColors.DarkGlassBorder, modifier = Modifier.padding(vertical = 4.dp))

            // Menu Actions List
            SongMenuItem(
                icon = Icons.Default.PlayArrow,
                title = "Play Now",
                subtitle = "Start playback immediately",
                onClick = {
                    onDismiss()
                    onPlayNow()
                }
            )

            SongMenuItem(
                icon = Icons.Default.QueuePlayNext,
                title = "Play Next",
                subtitle = "Insert right after current song",
                onClick = {
                    onDismiss()
                    onPlayNext()
                }
            )

            SongMenuItem(
                icon = Icons.Default.PlaylistAdd,
                title = "Play Later / Add to Queue",
                subtitle = "Append to end of active queue",
                onClick = {
                    onDismiss()
                    onPlayLater()
                }
            )

            SongMenuItem(
                icon = Icons.Default.BookmarkAdd,
                title = "Add to Playlist",
                subtitle = "Save to your custom playlists",
                onClick = {
                    onDismiss()
                    onAddToPlaylist()
                }
            )

            SongMenuItem(
                icon = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                title = if (isLiked) "Liked Song" else "Like Song",
                subtitle = if (isLiked) "Saved in Your Favorites" else "Add to Your Favorite Songs",
                iconTint = if (isLiked) AuraColors.MagentaPulse else AuraColors.NeonCyan,
                onClick = {
                    onToggleLike()
                }
            )

            SongMenuItem(
                icon = if (isDownloaded) Icons.Default.OfflinePin else Icons.Outlined.Download,
                title = if (isDownloaded) "Downloaded Offline" else "Download Track",
                subtitle = "Listen offline anytime",
                iconTint = if (isDownloaded) AuraColors.NeonCyan else Color.White,
                onClick = {
                    onDismiss()
                    onDownload()
                }
            )

            if (onStartRadio != null) {
                SongMenuItem(
                    icon = Icons.Default.Radio,
                    title = "Start Song Radio",
                    subtitle = "Autoplay similar vibes & tracks",
                    onClick = {
                        onDismiss()
                        onStartRadio()
                    }
                )
            }

            if (onNavigateToArtist != null) {
                SongMenuItem(
                    icon = Icons.Default.Person,
                    title = "View Artist Profile",
                    subtitle = song.artistName,
                    onClick = {
                        onDismiss()
                        onNavigateToArtist(song.artistId)
                    }
                )
            }

            if (onNavigateToAlbum != null) {
                SongMenuItem(
                    icon = Icons.Default.Album,
                    title = "View Album Page",
                    subtitle = song.albumTitle,
                    onClick = {
                        onDismiss()
                        onNavigateToAlbum(song.albumId)
                    }
                )
            }

            SongMenuItem(
                icon = Icons.Default.Share,
                title = "Share Track",
                subtitle = "Send link to friends or social apps",
                onClick = {
                    onDismiss()
                    onShare()
                }
            )

            if (onRemoveFromQueue != null) {
                SongMenuItem(
                    icon = Icons.Default.RemoveCircleOutline,
                    title = "Remove from Queue",
                    subtitle = "Remove this track from queue",
                    iconTint = AuraColors.MagentaPulse,
                    onClick = {
                        onDismiss()
                        onRemoveFromQueue()
                    }
                )
            }

            if (onShowTrackInfo != null) {
                SongMenuItem(
                    icon = Icons.Default.Info,
                    title = "Song Details & Technical Specs",
                    subtitle = "View audio bitrate, codec, copyright",
                    onClick = {
                        onDismiss()
                        onShowTrackInfo()
                    }
                )
            }
        }
    }
}

@Composable
private fun SongMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = AuraColors.NeonCyan,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AuraColors.DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
