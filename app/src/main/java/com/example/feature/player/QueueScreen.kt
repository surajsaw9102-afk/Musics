package com.example.feature.player

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.components.SongOverflowSheet
import com.example.core.database.entities.SongEntity
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass
import com.example.core.state.PlayerData
import com.example.core.state.PlayerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    playerData: PlayerData,
    playerState: PlayerState,
    onBackClick: () -> Unit = {},
    onNavigateToBrowse: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedIndices = remember { mutableStateListOf<Int>() }

    var showClearQueueDialog by remember { mutableStateOf(false) }
    var showSavePlaylistDialog by remember { mutableStateOf(false) }
    var playlistNameInput by remember { mutableStateOf("") }

    var selectedOverflowSong by remember { mutableStateOf<SongEntity?>(null) }

    // Filter queue items if search active
    val filteredQueue = remember(playerData.queue, searchQuery) {
        if (searchQuery.isBlank()) {
            playerData.queue.mapIndexed { idx, song -> idx to song }
        } else {
            playerData.queue.mapIndexed { idx, song -> idx to song }
                .filter { (_, song) ->
                    song.title.contains(searchQuery, ignoreCase = true) ||
                    song.artistName.contains(searchQuery, ignoreCase = true)
                }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AuraColors.DarkBackground,
                        AuraColors.DarkSurface,
                        Color.Black
                    )
                )
            )
            .statusBarsPadding()
            .testTag("full_queue_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // --- Top App Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("queue_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Playing Queue",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${playerData.queue.size} songs in queue",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Search button
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search Queue",
                            tint = if (isSearchActive) AuraColors.NeonCyan else Color.White
                        )
                    }

                    // Multi-select toggle
                    IconButton(onClick = {
                        isMultiSelectMode = !isMultiSelectMode
                        if (!isMultiSelectMode) selectedIndices.clear()
                    }) {
                        Icon(
                            imageVector = if (isMultiSelectMode) Icons.Default.ChecklistRtl else Icons.Outlined.Checklist,
                            contentDescription = "Multi Select",
                            tint = if (isMultiSelectMode) AuraColors.NeonCyan else Color.White
                        )
                    }

                    // Overflow actions menu
                    var showTopMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showTopMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showTopMenu,
                        onDismissRequest = { showTopMenu = false },
                        modifier = Modifier.background(AuraColors.DarkSurfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Save Queue as Playlist", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = AuraColors.NeonCyan) },
                            onClick = {
                                showTopMenu = false
                                showSavePlaylistDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Queue", color = AuraColors.MagentaPulse) },
                            leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = AuraColors.MagentaPulse) },
                            onClick = {
                                showTopMenu = false
                                showClearQueueDialog = true
                            }
                        )
                    }
                }
            }

            // --- Search Field Bar (if active) ---
            AnimatedVisibility(visible = isSearchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter queue by title or artist...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AuraColors.NeonCyan) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color.White)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuraColors.NeonCyan,
                        unfocusedBorderColor = AuraColors.DarkGlassBorder,
                        focusedContainerColor = AuraColors.DarkSurfaceVariant,
                        unfocusedContainerColor = AuraColors.DarkSurfaceVariant
                    ),
                    singleLine = true
                )
            }

            // --- Queue Main Content ---
            if (playerData.queue.isEmpty()) {
                // Empty Queue State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(AuraColors.ElectricPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = null,
                                tint = AuraColors.NeonCyan,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Text(
                            text = "Your Queue is Empty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Explore songs, albums, and playlists to add tracks to your queue.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = onNavigateToBrowse,
                            colors = ButtonDefaults.buttonColors(containerColor = AuraColors.NeonCyan),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.Explore, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Discover Music", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // --- Currently Playing Hero Section ---
                    playerData.currentSong?.let { song ->
                        item(key = "now_playing_section") {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "NOW PLAYING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AuraColors.NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(AuraRadius.Large)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = AuraColors.DarkSurfaceVariant
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = AuraColors.NeonCyan.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(AuraRadius.Small))
                                        ) {
                                            AsyncImage(
                                                model = song.coverUrl,
                                                contentDescription = song.title,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = song.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = song.artistName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AuraColors.NeonCyan,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Play/Pause button
                                        IconButton(onClick = playerState::togglePlayPause) {
                                            Icon(
                                                imageVector = if (playerData.isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                                                contentDescription = "Play Pause",
                                                tint = AuraColors.NeonCyan,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }

                                        // Overflow menu
                                        IconButton(onClick = { selectedOverflowSong = song }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Options",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "UP NEXT (${filteredQueue.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        TextButton(onClick = { showSavePlaylistDialog = true }) {
                                            Text("Save as Playlist", color = AuraColors.NeonCyan, style = MaterialTheme.typography.labelMedium)
                                        }
                                        TextButton(onClick = { showClearQueueDialog = true }) {
                                            Text("Clear Queue", color = AuraColors.MagentaPulse, style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- Up Next Queue Track Rows ---
                    itemsIndexed(
                        items = filteredQueue,
                        key = { _, pair -> "${pair.second.id}_${pair.first}" }
                    ) { _, (originalIndex, song) ->
                        val isCurrent = originalIndex == playerData.currentIndex
                        val isSelected = selectedIndices.contains(originalIndex)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (isMultiSelectMode) {
                                        if (isSelected) selectedIndices.remove(originalIndex) else selectedIndices.add(originalIndex)
                                    } else {
                                        playerState.playQueueIndex(originalIndex)
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isSelected -> AuraColors.ElectricPurple.copy(alpha = 0.35f)
                                    isCurrent -> AuraColors.NeonCyan.copy(alpha = 0.15f)
                                    else -> AuraColors.DarkSurfaceVariant.copy(alpha = 0.6f)
                                }
                            ),
                            border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, AuraColors.NeonCyan) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isMultiSelectMode) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedIndices.add(originalIndex) else selectedIndices.remove(originalIndex)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = AuraColors.NeonCyan,
                                            checkmarkColor = Color.Black
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                } else {
                                    Text(
                                        text = "${originalIndex + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isCurrent) AuraColors.NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.width(28.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = song.coverUrl,
                                        contentDescription = song.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCurrent) AuraColors.NeonCyan else Color.White,
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

                                if (!isMultiSelectMode) {
                                    // Move Up / Move Down buttons
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        if (originalIndex > 0) {
                                            IconButton(
                                                onClick = { playerState.reorderQueue(originalIndex, originalIndex - 1) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowUpward,
                                                    contentDescription = "Move Up",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        if (originalIndex < playerData.queue.size - 1) {
                                            IconButton(
                                                onClick = { playerState.reorderQueue(originalIndex, originalIndex + 1) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDownward,
                                                    contentDescription = "Move Down",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { selectedOverflowSong = song },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Song options",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Bulk Action Floating Bar (if multi select mode) ---
        if (isMultiSelectMode && selectedIndices.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = AuraColors.DarkSurfaceVariant,
                tonalElevation = 12.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${selectedIndices.size} selected",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                val sortedDesc = selectedIndices.sortedDescending()
                                sortedDesc.forEach { idx -> playerState.removeFromQueue(idx) }
                                selectedIndices.clear()
                                isMultiSelectMode = false
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = AuraColors.MagentaPulse, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove", color = AuraColors.MagentaPulse)
                        }

                        Button(
                            onClick = {
                                val selectedSongs = selectedIndices.mapNotNull { idx -> playerData.queue.getOrNull(idx) }
                                if (selectedSongs.isNotEmpty()) {
                                    playerState.saveQueueAsPlaylist("Queue Selection (${selectedSongs.size} tracks)")
                                }
                                selectedIndices.clear()
                                isMultiSelectMode = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AuraColors.NeonCyan)
                        ) {
                            Text("Save Playlist", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- Song Action Bottom Sheet ---
    if (selectedOverflowSong != null) {
        val song = selectedOverflowSong!!
        val songIndex = playerData.queue.indexOfFirst { it.id == song.id }

        SongOverflowSheet(
            song = song,
            onDismiss = { selectedOverflowSong = null },
            onPlayNow = { playerState.playSong(song) },
            onPlayNext = { playerState.addNext(song) },
            onPlayLater = { playerState.addToQueue(song) },
            onAddToPlaylist = { playerState.saveQueueAsPlaylist("Playlist - ${song.title}") },
            onToggleLike = { playerState.toggleLike() },
            isLiked = playerData.isLiked,
            onRemoveFromQueue = if (songIndex >= 0) {
                { playerState.removeFromQueue(songIndex) }
            } else null
        )
    }

    // --- Dialogs ---
    if (showClearQueueDialog) {
        AlertDialog(
            onDismissRequest = { showClearQueueDialog = false },
            title = { Text("Clear Queue?", fontWeight = FontWeight.Bold) },
            text = { Text("This will remove all upcoming tracks from your current queue.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        playerState.clearQueue()
                        showClearQueueDialog = false
                    }
                ) {
                    Text("Clear Queue", color = AuraColors.MagentaPulse)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearQueueDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = AuraColors.DarkSurfaceVariant
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
                        placeholder = { Text("My Queue Mix") },
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
                            playlistNameInput = ""
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
