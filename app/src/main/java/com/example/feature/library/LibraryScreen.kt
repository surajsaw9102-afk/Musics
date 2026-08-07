package com.example.feature.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.core.catalog.MusicCatalog
import com.example.core.components.*
import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass
import com.example.core.state.LibraryState

@Composable
fun LibraryScreen(
    onSongSelect: (SongEntity) -> Unit,
    libraryState: LibraryState = viewModel(),
    modifier: Modifier = Modifier,
    testTag: String = "library_screen"
) {
    val dataState by libraryState.libraryDataState.collectAsState()

    var activeDetailScreen by remember { mutableStateOf<String?>(null) } // "PL_id", "ALB_id", "ART_id"
    var showCreateDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedOverflowSong by remember { mutableStateOf<SongEntity?>(null) }

    // Navigation sub-routing check
    val detailKey = activeDetailScreen
    if (detailKey != null) {
        when {
            detailKey.startsWith("PL_") -> {
                val playlistId = detailKey.removePrefix("PL_")
                PlaylistDetailScreen(
                    playlistId = playlistId,
                    onBackClick = { activeDetailScreen = null },
                    onPlaySong = onSongSelect,
                    onPlayPlaylist = { songs ->
                        if (songs.isNotEmpty()) onSongSelect(songs.first())
                    },
                    libraryState = libraryState,
                    modifier = modifier
                )
                return
            }
            detailKey.startsWith("ALB_") -> {
                val albumId = detailKey.removePrefix("ALB_")
                AlbumDetailScreen(
                    albumId = albumId,
                    onBackClick = { activeDetailScreen = null },
                    onPlaySong = onSongSelect,
                    libraryState = libraryState,
                    modifier = modifier
                )
                return
            }
            detailKey.startsWith("ART_") -> {
                val artistId = detailKey.removePrefix("ART_")
                ArtistDetailScreen(
                    artistId = artistId,
                    onBackClick = { activeDetailScreen = null },
                    onPlaySong = onSongSelect,
                    libraryState = libraryState,
                    modifier = modifier
                )
                return
            }
        }
    }

    Column(
        modifier = modifier
            .testTag(testTag)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Section Header & Create Playlist Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AuraSectionHeader(
                    title = "Your Personal Library",
                    subtitle = "100% Free • Playlists, Liked Songs & Saved Collection"
                )
            }

            AuraButton(
                text = "New",
                icon = Icons.Default.Add,
                onClick = { showCreateDialog = true },
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search bar in Library
        AuraTextField(
            value = dataState.searchQuery,
            onValueChange = { libraryState.setSearchQuery(it) },
            placeholder = "Search playlists, songs, artists, albums...",
            leadingIcon = Icons.Default.Search,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips Row + Sort Dropdown Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(FilterType.entries.toTypedArray()) { filter ->
                    AuraChip(
                        label = filter.label,
                        isSelected = filter == dataState.activeFilter,
                        onClick = { libraryState.setFilter(filter) }
                    )
                }
            }

            Box {
                IconButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AuraColors.ElectricPurple.copy(alpha = 0.25f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort Options",
                        tint = AuraColors.NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(AuraColors.DarkSurfaceVariant)
                ) {
                    SortOption.entries.forEach { sort ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = sort.label,
                                    color = if (sort == dataState.activeSort) AuraColors.NeonCyan else Color.White,
                                    fontWeight = if (sort == dataState.activeSort) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                libraryState.setSort(sort)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Library Body View based on active filter
        val query = dataState.searchQuery.trim().lowercase()

        // Filter and Sort Data
        val playlists = dataState.playlists.filter {
            query.isBlank() || it.name.lowercase().contains(query) || it.description.lowercase().contains(query)
        }.applySorting(dataState.activeSort) { it.name }

        val likedSongs = dataState.likedSongs.filter {
            query.isBlank() || it.title.lowercase().contains(query) || it.artistName.lowercase().contains(query)
        }.applySorting(dataState.activeSort) { it.title }

        val savedAlbums = dataState.savedAlbums.filter {
            query.isBlank() || it.title.lowercase().contains(query) || it.artistName.lowercase().contains(query)
        }.applySorting(dataState.activeSort) { it.title }

        val followedArtists = dataState.followedArtists.filter {
            query.isBlank() || it.name.lowercase().contains(query)
        }.applySorting(dataState.activeSort) { it.name }

        val historySongs = dataState.historySongs.filter {
            query.isBlank() || it.title.lowercase().contains(query) || it.artistName.lowercase().contains(query)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PINNED SECTION (Show when Filter is ALL or PINNED)
            val isPinnedFilter = dataState.activeFilter == FilterType.PINNED
            val isAllFilter = dataState.activeFilter == FilterType.ALL
            val pinnedPlaylists = playlists.filter { dataState.pinnedPlaylistIds.contains(it.id) }
            val pinnedSongs = likedSongs.filter { dataState.pinnedSongIds.contains(it.id) }
            val pinnedAlbums = savedAlbums.filter { dataState.pinnedAlbumIds.contains(it.id) }
            val pinnedArtists = followedArtists.filter { dataState.pinnedArtistIds.contains(it.id) }
            val totalPinned = pinnedPlaylists.size + pinnedSongs.size + pinnedAlbums.size + pinnedArtists.size

            if (isPinnedFilter) {
                if (totalPinned == 0) {
                    item {
                        AuraEmptyState(
                            title = "No Pinned Items Yet",
                            description = "Tap the star icon on any playlist, song, album, or artist to pin it for quick access.",
                            actionButtonText = "Browse Playlists",
                            onActionClick = { libraryState.setFilter(FilterType.PLAYLISTS) },
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                } else {
                    item {
                        Text("Pinned Collection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AuraColors.NeonCyan)
                    }
                    items(pinnedPlaylists, key = { "pin_pl_${it.id}" }) { pl ->
                        AuraListTile(
                            title = pl.name,
                            subtitle = "${pl.songIds.size} tracks • Pinned Playlist",
                            coverUrl = pl.coverUrl,
                            onClick = { activeDetailScreen = "PL_${pl.id}" }
                        )
                    }
                    items(pinnedSongs, key = { "pin_sg_${it.id}" }) { sg ->
                        AuraListTile(
                            title = sg.title,
                            subtitle = "${sg.artistName} • Pinned Song",
                            coverUrl = sg.coverUrl,
                            isHdAudio = sg.isHdAudio,
                            onClick = { onSongSelect(sg) },
                            onMenuClick = { selectedOverflowSong = sg }
                        )
                    }
                    items(pinnedAlbums, key = { "pin_alb_${it.id}" }) { alb ->
                        AuraListTile(
                            title = alb.title,
                            subtitle = "${alb.artistName} • Pinned Album",
                            coverUrl = alb.coverUrl,
                            onClick = { activeDetailScreen = "ALB_${alb.id}" }
                        )
                    }
                }
            }

            // PLAYLISTS SECTION (Show when ALL or PLAYLISTS)
            if (isAllFilter || dataState.activeFilter == FilterType.PLAYLISTS) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Playlists (${playlists.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (isAllFilter) {
                            Text(
                                text = "See All",
                                style = MaterialTheme.typography.labelSmall,
                                color = AuraColors.NeonCyan,
                                modifier = Modifier.clickable { libraryState.setFilter(FilterType.PLAYLISTS) }
                            )
                        }
                    }
                }

                if (playlists.isEmpty()) {
                    item {
                        AuraEmptyState(
                            title = "No Playlists Found",
                            description = "Create a custom playlist to organize your favorite audio tracks.",
                            actionButtonText = "Create Playlist",
                            onActionClick = { showCreateDialog = true },
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                } else {
                    items(playlists, key = { "pl_${it.id}" }) { pl ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AuraRadius.Medium))
                                .auraGlass(shape = RoundedCornerShape(AuraRadius.Medium))
                                .clickable { activeDetailScreen = "PL_${pl.id}" }
                                .padding(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                AsyncImage(
                                    model = pl.coverUrl,
                                    contentDescription = pl.name,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(AuraRadius.Small)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = pl.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (pl.pinned) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = "Pinned",
                                                tint = AuraColors.NeonCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${pl.songIds.size} tracks • ${pl.description}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(onClick = { libraryState.togglePinPlaylist(pl.id) }) {
                                    Icon(
                                        imageVector = if (pl.pinned) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "Pin",
                                        tint = if (pl.pinned) AuraColors.NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // LIKED SONGS SECTION (Show when ALL or SONGS)
            if (isAllFilter || dataState.activeFilter == FilterType.SONGS) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Liked Songs (${likedSongs.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                if (likedSongs.isEmpty()) {
                    item {
                        AuraEmptyState(
                            title = "No Liked Songs Yet",
                            description = "Tap the heart icon on any song while browsing or playing to save it here.",
                            actionButtonText = "Discover Songs",
                            onActionClick = { libraryState.setFilter(FilterType.ALL) },
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                } else {
                    items(likedSongs, key = { "song_${it.id}" }) { song ->
                        AuraListTile(
                            title = song.title,
                            subtitle = "${song.artistName} • ${song.audioQuality}",
                            coverUrl = song.coverUrl,
                            isHdAudio = song.isHdAudio,
                            onClick = {
                                libraryState.recordSongPlay(song.id)
                                onSongSelect(song)
                            },
                            onMenuClick = {
                                selectedOverflowSong = song
                            }
                        )
                    }
                }
            }

            // SAVED ALBUMS SECTION (Show when ALL or ALBUMS)
            if (isAllFilter || dataState.activeFilter == FilterType.ALBUMS) {
                item {
                    Text(
                        text = "Saved Albums (${savedAlbums.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (savedAlbums.isEmpty()) {
                    item {
                        AuraEmptyState(
                            title = "No Saved Albums",
                            description = "Save full studio records to your library for instant access.",
                            actionButtonText = "Browse Catalog",
                            onActionClick = { libraryState.setFilter(FilterType.ALL) },
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                } else {
                    items(savedAlbums, key = { "alb_${it.id}" }) { alb ->
                        AuraListTile(
                            title = alb.title,
                            subtitle = "${alb.artistName} • ${alb.releaseYear}",
                            coverUrl = alb.coverUrl,
                            onClick = { activeDetailScreen = "ALB_${alb.id}" }
                        )
                    }
                }
            }

            // FOLLOWED ARTISTS SECTION (Show when ALL or ARTISTS)
            if (isAllFilter || dataState.activeFilter == FilterType.ARTISTS) {
                item {
                    Text(
                        text = "Followed Artists (${followedArtists.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (followedArtists.isEmpty()) {
                    item {
                        AuraEmptyState(
                            title = "No Followed Artists",
                            description = "Follow your favorite creators to stay updated on new releases.",
                            actionButtonText = "Explore Artists",
                            onActionClick = { libraryState.setFilter(FilterType.ALL) },
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                } else {
                    items(followedArtists, key = { "art_${it.id}" }) { artist ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AuraRadius.Medium))
                                .auraGlass(shape = RoundedCornerShape(AuraRadius.Medium))
                                .clickable { activeDetailScreen = "ART_${artist.id}" }
                                .padding(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                AsyncImage(
                                    model = artist.avatarUrl,
                                    contentDescription = artist.name,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = artist.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${artist.monthlyListeners / 1000000f}M Monthly Listeners • Verified",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                TextButton(
                                    onClick = { libraryState.toggleFollowArtist(artist.id) }
                                ) {
                                    Text("Following", color = AuraColors.NeonCyan, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // HISTORY SECTION (Show when HISTORY filter)
            if (dataState.activeFilter == FilterType.HISTORY) {
                item {
                    Text(
                        text = "Playback History (${historySongs.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (historySongs.isEmpty()) {
                    item {
                        AuraEmptyState(
                            title = "No Playback History",
                            description = "Start playing music to build your personal streaming history.",
                            actionButtonText = "Play Music",
                            onActionClick = { libraryState.setFilter(FilterType.ALL) },
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                } else {
                    items(historySongs, key = { "hist_${it.id}" }) { song ->
                        AuraListTile(
                            title = song.title,
                            subtitle = "${song.artistName} • ${song.albumTitle}",
                            coverUrl = song.coverUrl,
                            isHdAudio = song.isHdAudio,
                            onClick = {
                                libraryState.recordSongPlay(song.id)
                                onSongSelect(song)
                            },
                            onMenuClick = {
                                selectedOverflowSong = song
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (selectedOverflowSong != null) {
        val song = selectedOverflowSong!!
        SongOverflowSheet(
            song = song,
            onDismiss = { selectedOverflowSong = null },
            onPlayNow = {
                onSongSelect(song)
                selectedOverflowSong = null
            }
        )
    }

    // Create Playlist Dialog
    if (showCreateDialog) {
        CreateEditPlaylistDialog(
            isEditing = false,
            onDismiss = { showCreateDialog = false },
            onSave = { name, desc, cover ->
                val created = libraryState.createPlaylist(name, desc, cover)
                showCreateDialog = false
                activeDetailScreen = "PL_${created.id}"
            }
        )
    }
}

private fun <T> List<T>.applySorting(
    sortOption: SortOption,
    nameExtractor: (T) -> String
): List<T> {
    return when (sortOption) {
        SortOption.ALPHABETICAL -> this.sortedBy { nameExtractor(it).lowercase() }
        else -> this
    }
}
