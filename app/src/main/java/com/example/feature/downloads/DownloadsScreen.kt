package com.example.feature.downloads

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.core.catalog.MusicCatalog
import com.example.core.components.*
import com.example.core.database.entities.SongEntity
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass
import com.example.core.downloads.DownloadItem
import com.example.core.downloads.DownloadState
import com.example.core.offline.OfflineSortOption
import com.example.core.provider.DownloadQuality
import com.example.core.state.DownloadsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    onSongSelect: (SongEntity) -> Unit,
    downloadsState: DownloadsState = viewModel(),
    modifier: Modifier = Modifier,
    testTag: String = "downloads_screen"
) {
    val data by downloadsState.downloads.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    var offlineSearchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(OfflineSortOption.DATE_DOWNLOADED) }
    var showSortMenu by remember { mutableStateOf(false) }

    val tabs = remember {
        listOf("All", "Active Queue", "Songs", "Albums & Playlists", "Artists", "Smart Cache")
    }

    val filteredSongs = remember(data.downloadedSongs, offlineSearchQuery, sortOption) {
        var list = if (offlineSearchQuery.isBlank()) {
            data.downloadedSongs
        } else {
            val q = offlineSearchQuery.trim().lowercase()
            data.downloadedSongs.filter {
                it.title.lowercase().contains(q) ||
                        it.artistName.lowercase().contains(q) ||
                        it.albumTitle.lowercase().contains(q)
            }
        }

        when (sortOption) {
            OfflineSortOption.TITLE -> list.sortedBy { it.title }
            OfflineSortOption.ARTIST -> list.sortedBy { it.artistName }
            OfflineSortOption.DATE_DOWNLOADED -> list
            OfflineSortOption.SIZE -> list.sortedByDescending { it.durationMs }
        }
    }

    Scaffold(
        modifier = modifier
            .testTag(testTag)
            .fillMaxSize(),
        containerColor = Color.Transparent
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        AuraSectionHeader(
                            title = "Downloads & Offline",
                            subtitle = "Zero data consumption • Local High-Fi"
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Sync status button
                        IconButton(
                            onClick = { downloadsState.triggerSync() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AuraColors.DarkSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = if (data.syncStatus.isSyncing) Icons.Default.Sync else Icons.Default.CloudSync,
                                contentDescription = "Sync",
                                tint = if (data.syncStatus.isSyncing) AuraColors.NeonCyan else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Force Offline Switch Badge
                        Surface(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { downloadsState.toggleOfflineMode(!data.isOfflineMode) },
                            color = if (data.isOfflineMode) AuraColors.GoldAmber.copy(alpha = 0.2f) else AuraColors.DarkSurfaceVariant,
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (data.isOfflineMode) AuraColors.GoldAmber else AuraColors.DarkGlassBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (data.isOfflineMode) Icons.Default.SignalCellularConnectedNoInternet0Bar else Icons.Default.Wifi,
                                    contentDescription = "Network Status",
                                    tint = if (data.isOfflineMode) AuraColors.GoldAmber else AuraColors.NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (data.isOfflineMode) "Offline" else "Online",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (data.isOfflineMode) AuraColors.GoldAmber else Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Sync Banner if syncing
            if (data.syncStatus.isSyncing) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AuraRadius.Medium)),
                        color = AuraColors.ElectricPurple.copy(alpha = 0.25f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { data.syncStatus.progress },
                                modifier = Modifier.size(20.dp),
                                color = AuraColors.NeonCyan,
                                strokeWidth = 2.dp,
                            )
                            Column {
                                Text(
                                    text = "Smart Sync Active",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = data.syncStatus.syncStage,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Storage & Smart Cache Dashboard Glass Card
            item {
                AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Device Storage & Cache",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${data.totalMusicStorageMb} MB Music • ${data.cacheStorageMb} MB Cache • ${data.freeDeviceSpaceGb} GB Free",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AuraHdBadge()
                        }

                        // Storage Visualizer Progress Bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(AuraRadius.Small))
                                    .background(AuraColors.DarkSurface)
                            ) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    val musicRatio = (data.totalMusicStorageMb / (128 * 1024.0)).toFloat().coerceAtLeast(0.08f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(musicRatio)
                                            .background(AuraColors.ElectricPurple)
                                    )
                                    val cacheRatio = (data.cacheStorageMb / (128 * 1024.0)).toFloat().coerceAtLeast(0.06f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(cacheRatio)
                                            .background(AuraColors.NeonCyan)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(1f - (musicRatio + cacheRatio))
                                            .background(Color.White.copy(alpha = 0.1f))
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(8.dp).background(AuraColors.ElectricPurple, CircleShape))
                                        Text("Downloads", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(8.dp).background(AuraColors.NeonCyan, CircleShape))
                                        Text("Smart Cache", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                                    }
                                }
                                Text(
                                    text = "${data.freeDeviceSpaceGb} GB Free",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.1f))

                        // Controls Row: Quality, Wi-Fi Only, Clear Cache
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(
                                onClick = { showQualityDialog = true },
                                label = { Text(data.downloadQuality.label, fontSize = 12.sp, color = Color.White) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.HighQuality,
                                        contentDescription = "Quality",
                                        tint = AuraColors.NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(AuraRadius.Medium),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = AuraColors.DarkSurfaceVariant.copy(alpha = 0.6f)
                                )
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Wi-Fi Only",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                                Switch(
                                    checked = data.wifiOnlyDownloads,
                                    onCheckedChange = { downloadsState.setWifiOnly(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = AuraColors.ElectricPurple,
                                        checkedTrackColor = AuraColors.NeonCyan.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.scale(0.8f)
                                )
                            }

                            TextButton(onClick = { showClearConfirmDialog = true }) {
                                Text(
                                    text = "Clean Space",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AuraColors.GoldAmber
                                )
                            }
                        }
                    }
                }
            }

            // Recently Downloaded Horizontal Carousel
            if (data.recentlyDownloadedSongs.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Recently Downloaded",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(data.recentlyDownloadedSongs) { song ->
                                Column(
                                    modifier = Modifier
                                        .width(110.dp)
                                        .clickable {
                                            val catalogSong = MusicCatalog.getSongById(song.id) ?: song
                                            onSongSelect(catalogSong)
                                        }
                                ) {
                                    AsyncImage(
                                        model = song.coverUrl,
                                        contentDescription = song.title,
                                        modifier = Modifier
                                            .size(110.dp)
                                            .clip(RoundedCornerShape(AuraRadius.Medium)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = song.artistName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Offline Search & Sort Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AuraSearchBar(
                        query = offlineSearchQuery,
                        onQueryChange = { offlineSearchQuery = it },
                        placeholder = "Search downloaded songs & artists...",
                        modifier = Modifier.weight(1f)
                    )

                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier
                                .clip(RoundedCornerShape(AuraRadius.Medium))
                                .background(AuraColors.DarkSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = AuraColors.NeonCyan
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(AuraColors.DarkSurfaceVariant)
                        ) {
                            OfflineSortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            color = if (sortOption == option) AuraColors.NeonCyan else Color.White
                                        )
                                    },
                                    onClick = {
                                        sortOption = option
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Tabs / Filters
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(tabs.size) { index ->
                        val isSelected = selectedTab == index
                        val badgeCount = when (index) {
                            1 -> data.activeDownloads.size
                            2 -> filteredSongs.size
                            3 -> data.downloadedAlbums.size + data.downloadedPlaylists.size
                            4 -> data.downloadedArtists.size
                            else -> null
                        }

                        AuraChip(
                            label = if (badgeCount != null && badgeCount > 0) "${tabs[index]} ($badgeCount)" else tabs[index],
                            isSelected = isSelected,
                            onClick = { selectedTab = index }
                        )
                    }
                }
            }

            // --- TAB CONTENT ---

            // Tab 1: Active Downloads / Queue Section
            if (selectedTab == 0 || selectedTab == 1) {
                if (data.activeDownloads.isNotEmpty() || data.pausedDownloads.isNotEmpty() || data.failedDownloads.isNotEmpty()) {
                    item {
                        Text(
                            text = "Downloading Queue (${data.activeDownloads.size + data.pausedDownloads.size + data.failedDownloads.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    items(data.allDownloadItems.filter { it.state != DownloadState.DOWNLOADED }) { item ->
                        ActiveDownloadCard(
                            item = item,
                            onPause = { downloadsState.pauseDownload(item.song.id) },
                            onResume = { downloadsState.resumeDownload(item.song.id) },
                            onCancel = { downloadsState.cancelDownload(item.song.id) },
                            onRetry = { downloadsState.retryDownload(item.song.id) }
                        )
                    }
                } else if (selectedTab == 1) {
                    item {
                        AuraEmptyState(
                            title = "No Active Downloads",
                            description = "Your download queue is empty. Tap download on any song or album to play offline.",
                            icon = Icons.Default.CloudDownload
                        )
                    }
                }
            }

            // Tab 2: Downloaded Songs
            if (selectedTab == 0 || selectedTab == 2) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Downloaded Songs (${filteredSongs.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (filteredSongs.isNotEmpty()) {
                            Text(
                                text = "Play All Offline",
                                style = MaterialTheme.typography.labelMedium,
                                color = AuraColors.NeonCyan,
                                modifier = Modifier.clickable {
                                    val catalogSong = MusicCatalog.getSongById(filteredSongs.first().id) ?: filteredSongs.first()
                                    onSongSelect(catalogSong)
                                }
                            )
                        }
                    }
                }

                if (filteredSongs.isEmpty() && (selectedTab == 2 || selectedTab == 0)) {
                    item {
                        AuraEmptyState(
                            title = "No Downloaded Songs",
                            description = "Save your favorite tracks locally for offline listening anytime.",
                            icon = Icons.Default.MusicNote
                        )
                    }
                } else {
                    items(filteredSongs) { song ->
                        val catalogSong = MusicCatalog.getSongById(song.id) ?: song
                        AuraListTile(
                            title = song.title,
                            subtitle = "${song.artistName} • FLAC Lossless • 38 MB",
                            coverUrl = song.coverUrl,
                            isHdAudio = true,
                            onClick = { onSongSelect(catalogSong) },
                            onMenuClick = { downloadsState.removeDownload(song.id) }
                        )
                    }
                }
            }

            // Tab 3: Downloaded Albums & Playlists
            if (selectedTab == 0 || selectedTab == 3) {
                item {
                    Text(
                        text = "Downloaded Albums & Playlists",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (data.downloadedAlbums.isEmpty() && data.downloadedPlaylists.isEmpty()) {
                    item {
                        AuraEmptyState(
                            title = "No Downloaded Collections",
                            description = "Download full albums or playlists in one click for continuous offline listening.",
                            icon = Icons.AutoMirrored.Filled.QueueMusic
                        )
                    }
                } else {
                    items(data.downloadedAlbums) { album ->
                        DownloadedCollectionCard(
                            title = album.albumTitle,
                            subtitle = "Album by ${album.artistName} • ${album.downloadedSongs.size} Tracks",
                            coverUrl = album.coverUrl,
                            sizeMb = album.totalSizeMb,
                            onPlay = {
                                if (album.downloadedSongs.isNotEmpty()) {
                                    val song = MusicCatalog.getSongById(album.downloadedSongs.first().id) ?: album.downloadedSongs.first()
                                    onSongSelect(song)
                                }
                            }
                        )
                    }

                    items(data.downloadedPlaylists) { playlist ->
                        DownloadedCollectionCard(
                            title = playlist.playlistName,
                            subtitle = "Playlist • ${playlist.downloadedSongs.size} Offline Songs",
                            coverUrl = playlist.coverUrl,
                            sizeMb = playlist.totalSizeMb,
                            onPlay = {
                                if (playlist.downloadedSongs.isNotEmpty()) {
                                    val song = MusicCatalog.getSongById(playlist.downloadedSongs.first().id) ?: playlist.downloadedSongs.first()
                                    onSongSelect(song)
                                }
                            }
                        )
                    }
                }
            }

            // Tab 4: Downloaded Artists
            if (selectedTab == 0 || selectedTab == 4) {
                if (data.downloadedArtists.isNotEmpty()) {
                    item {
                        Text(
                            text = "Downloaded Artists (${data.downloadedArtists.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    items(data.downloadedArtists) { artist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AuraRadius.Medium))
                                .background(AuraColors.DarkSurfaceVariant.copy(alpha = 0.6f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AuraAvatar(
                                photoUrl = artist.avatarUrl,
                                size = 44.dp,
                                showFreeBadge = false
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = artist.artistName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${artist.songCount} offline song(s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Tab 5: Smart Cache Stats
            if (selectedTab == 5) {
                item {
                    AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Smart Cache Statistics",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                AuraFreeBadge()
                            }

                            Text(
                                text = "Smart Cache automatically stores frequently played songs, recent searches, and opened albums locally to eliminate buffering and conserve battery.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                CacheMetricTile(
                                    label = "Cached Songs",
                                    value = "${data.cacheStats.cachedSongCount}"
                                )
                                CacheMetricTile(
                                    label = "Cached Albums",
                                    value = "${data.cacheStats.cachedAlbumCount}"
                                )
                                CacheMetricTile(
                                    label = "Cache Size",
                                    value = "${data.cacheStats.totalCacheSizeMb} MB"
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                CacheMetricTile(
                                    label = "Max Limit",
                                    value = "${data.cacheStats.maxCacheLimitMb.toInt()} MB"
                                )
                                CacheMetricTile(
                                    label = "Cache Hits",
                                    value = "${data.cacheStats.totalCacheHits}"
                                )
                                CacheMetricTile(
                                    label = "Auto Cleanup",
                                    value = "Enabled (LRU)"
                                )
                            }

                            AuraButton(
                                text = "Clear Smart Cache",
                                onClick = { downloadsState.clearCache() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    // Download Quality Selection Dialog
    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text("Select Download Quality", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DownloadQuality.values().forEach { quality ->
                        val isSelected = data.downloadQuality == quality
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AuraRadius.Medium))
                                .clickable {
                                    downloadsState.setDownloadQuality(quality)
                                    showQualityDialog = false
                                }
                                .background(if (isSelected) AuraColors.ElectricPurple.copy(alpha = 0.2f) else Color.Transparent)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = quality.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) AuraColors.NeonCyan else Color.White
                                )
                                Text(
                                    text = quality.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = AuraColors.NeonCyan
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQualityDialog = false }) {
                    Text("Close", color = AuraColors.NeonCyan)
                }
            },
            containerColor = AuraColors.DarkSurfaceVariant
        )
    }

    // Clear Space Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Storage Management", color = Color.White) },
            text = {
                Text(
                    text = "Choose whether to clear temporary Smart Cache or wipe all downloaded offline music.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        downloadsState.clearCache()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Clear Cache Only", color = AuraColors.NeonCyan)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        downloadsState.removeAllDownloads()
                        downloadsState.clearCache()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Wipe All Downloads", color = AuraColors.GoldAmber)
                }
            },
            containerColor = AuraColors.DarkSurfaceVariant
        )
    }
}

@Composable
fun ActiveDownloadCard(
    item: DownloadItem,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AuraRadius.Medium))
            .background(AuraColors.DarkSurfaceVariant.copy(alpha = 0.8f))
            .padding(12.dp),
        color = Color.Transparent
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = item.song.coverUrl,
                        contentDescription = "Cover",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(AuraRadius.Small)),
                        contentScale = ContentScale.Crop
                    )

                    Column {
                        Text(
                            text = item.song.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.song.artistName} • ${item.quality.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    when (item.state) {
                        DownloadState.DOWNLOADING -> {
                            IconButton(onClick = onPause) {
                                Icon(Icons.Default.Pause, contentDescription = "Pause", tint = AuraColors.GoldAmber)
                            }
                        }
                        DownloadState.PAUSED -> {
                            IconButton(onClick = onResume) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = AuraColors.NeonCyan)
                            }
                        }
                        DownloadState.FAILED -> {
                            IconButton(onClick = onRetry) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = AuraColors.GoldAmber)
                            }
                        }
                        else -> {}
                    }

                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AuraLinearProgress(progress = item.progress)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val statusText = when (item.state) {
                        DownloadState.QUEUED -> "Queued..."
                        DownloadState.DOWNLOADING -> "${(item.progress * 100).toInt()}% • ${"%.1f".format(item.downloadSpeedKbps / 1024.0)} MB/s"
                        DownloadState.PAUSED -> "Paused"
                        DownloadState.FAILED -> "Failed: ${item.errorMessage ?: "Network error"}"
                        DownloadState.DOWNLOADED -> "Completed"
                        DownloadState.NOT_DOWNLOADED -> ""
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.state == DownloadState.FAILED) AuraColors.GoldAmber else AuraColors.NeonCyan
                    )

                    if (item.state == DownloadState.DOWNLOADING && item.remainingTimeSeconds > 0) {
                        Text(
                            text = "ETA ${item.remainingTimeSeconds}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadedCollectionCard(
    title: String,
    subtitle: String,
    coverUrl: String,
    sizeMb: Double,
    onPlay: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AuraRadius.Medium))
            .background(AuraColors.DarkSurfaceVariant.copy(alpha = 0.6f))
            .clickable { onPlay() }
            .padding(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = "Cover",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(AuraRadius.Small)),
                    contentScale = ContentScale.Crop
                )

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "$subtitle • ${"%.1f".format(sizeMb)} MB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onPlay) {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = "Play Offline",
                    tint = AuraColors.NeonCyan,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
fun CacheMetricTile(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(AuraRadius.Small))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AuraColors.NeonCyan)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
