package com.example.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.components.AuraButton
import com.example.core.components.AuraIconButton
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.auraGlass
import com.example.core.state.PlayerData
import com.example.core.state.PlayerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    playerData: PlayerData,
    playerState: PlayerState,
    onDismiss: () -> Unit,
    onOpenFullQueue: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AuraColors.DarkBackground,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Play Queue",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${playerData.queue.size} songs in queue",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            onDismiss()
                            onOpenFullQueue()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInFull,
                            contentDescription = "Full Queue Page",
                            tint = AuraColors.NeonCyan
                        )
                    }

                    if (playerData.queue.isNotEmpty()) {
                        var showSavePlaylistDialog by remember { mutableStateOf(false) }
                        var playlistNameInput by remember { mutableStateOf("") }

                        TextButton(onClick = { showSavePlaylistDialog = true }) {
                            Text(
                                text = "Save as Playlist",
                                color = AuraColors.NeonCyan,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        TextButton(onClick = { playerState.clearQueue() }) {
                            Text(
                                text = "Clear Queue",
                                color = AuraColors.MagentaPulse,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        if (showSavePlaylistDialog) {
                            AlertDialog(
                                onDismissRequest = { showSavePlaylistDialog = false },
                                title = { Text("Save Queue as Playlist", fontWeight = FontWeight.Bold) },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("Enter a name for your new playlist containing ${playerData.queue.size} tracks:")
                                        OutlinedTextField(
                                            value = playlistNameInput,
                                            onValueChange = { playlistNameInput = it },
                                            placeholder = { Text("My Queue Playlist") },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AuraColors.NeonCyan,
                                                unfocusedBorderColor = AuraColors.DarkGlassBorder
                                            )
                                        )
                                    }
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            if (playlistNameInput.isNotBlank()) {
                                                playerState.saveQueueAsPlaylist(playlistNameInput)
                                                showSavePlaylistDialog = false
                                            }
                                        }
                                    ) {
                                        Text("Save", color = AuraColors.NeonCyan)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showSavePlaylistDialog = false }) {
                                        Text("Cancel")
                                    }
                                },
                                containerColor = AuraColors.DarkSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Currently Playing Section
            playerData.currentSong?.let { song ->
                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelSmall,
                    color = AuraColors.NeonCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .auraGlass(shape = RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = song.coverUrl,
                        contentDescription = song.title,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artistName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Playing",
                        tint = AuraColors.NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Up Next List
            Text(
                text = "UP NEXT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (playerData.queue.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Queue is empty. Tap any song to play!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(
                        items = playerData.queue,
                        key = { index, song -> "${song.id}_$index" }
                    ) { index, song ->
                        val isCurrent = index == playerData.currentIndex

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isCurrent) AuraColors.ElectricPurple.copy(alpha = 0.2f)
                                    else Color.Transparent
                                )
                                .clickable { playerState.playQueueIndex(index) }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(28.dp)
                            )

                            AsyncImage(
                                model = song.coverUrl,
                                contentDescription = song.title,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) AuraColors.NeonCyan else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artistName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            AuraIconButton(
                                icon = Icons.Default.Close,
                                contentDescription = "Remove",
                                onClick = { playerState.removeFromQueue(index) },
                                size = 32.dp,
                                isGlass = false,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
