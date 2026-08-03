package com.example.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.core.catalog.MusicCatalog
import com.example.core.components.AuraButton
import com.example.core.components.AuraTextField
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass

@Composable
fun AddSongsToPlaylistDialog(
    existingSongIds: List<String>,
    onDismiss: () -> Unit,
    onAddSongs: (selectedSongIds: List<String>) -> Unit,
    testTag: String = "add_songs_to_playlist_dialog"
) {
    var searchQuery by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<String>().apply { addAll(existingSongIds) } }

    val allSongs = remember { MusicCatalog.ALL_SONGS }
    val filteredSongs = remember(searchQuery) {
        if (searchQuery.isBlank()) allSongs
        else allSongs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artistName.contains(searchQuery, ignoreCase = true) ||
                    it.genre.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .testTag(testTag)
                .fillMaxWidth()
                .height(520.dp)
                .clip(RoundedCornerShape(AuraRadius.Large))
                .auraGlass(shape = RoundedCornerShape(AuraRadius.Large))
                .background(AuraColors.DarkSurfaceVariant.copy(alpha = 0.95f))
                .padding(20.dp),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Tracks to Playlist",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Search field
                AuraTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search track, artist, or genre...",
                    leadingIcon = Icons.Default.Search,
                    modifier = Modifier.fillMaxWidth()
                )

                // Songs Multi-Select List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSongs, key = { it.id }) { song ->
                        val isChecked = selectedIds.contains(song.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AuraRadius.Small))
                                .clickable {
                                    if (isChecked) {
                                        selectedIds.remove(song.id)
                                    } else {
                                        selectedIds.add(song.id)
                                    }
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked == true) {
                                        if (!selectedIds.contains(song.id)) selectedIds.add(song.id)
                                    } else {
                                        selectedIds.remove(song.id)
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AuraColors.ElectricPurple,
                                    checkmarkColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            AsyncImage(
                                model = song.coverUrl,
                                contentDescription = song.title,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(AuraRadius.Small)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(10.dp))

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
                                    text = "${song.artistName} • ${song.genre}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Done Button
                val newAddedCount = selectedIds.size
                AuraButton(
                    text = "Save Selection ($newAddedCount tracks)",
                    onClick = {
                        onAddSongs(selectedIds.toList())
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
