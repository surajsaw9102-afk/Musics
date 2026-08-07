package com.example.feature.search

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
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
import com.example.core.search.*
import com.example.core.state.SearchData
import com.example.core.state.SearchState

@Composable
fun SearchScreen(
    onSongSelect: (SongEntity) -> Unit,
    searchState: SearchState = viewModel(),
    modifier: Modifier = Modifier,
    testTag: String = "search_screen"
) {
    val searchData by searchState.searchData.collectAsState()
    var selectedOverflowSong by remember { mutableStateOf<SongEntity?>(null) }

    Column(
        modifier = modifier
            .testTag(testTag)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Online Search & Discovery",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "100% Free Online Music Catalog & Local Files",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AuraFreeBadge()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar with Voice & Filter Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                AuraTextField(
                    value = searchData.query,
                    onValueChange = { newQuery -> searchState.updateQuery(newQuery) },
                    placeholder = "Search songs, artists, albums, playlists...",
                    leadingIcon = Icons.Default.Search,
                    trailingIcon = if (searchData.query.isNotEmpty()) Icons.Default.Close else null,
                    onTrailingIconClick = { searchState.updateQuery("") }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Voice Search Icon Button
            AuraIconButton(
                icon = Icons.Default.Mic,
                contentDescription = "Voice Search",
                onClick = searchState::startVoiceSearch,
                size = 48.dp,
                tint = AuraColors.NeonCyan
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Filter Icon Button
            AuraIconButton(
                icon = Icons.Default.Tune,
                contentDescription = "Filter and Sort",
                onClick = { searchState.toggleFilterSheet(true) },
                size = 48.dp,
                tint = if (searchData.filterOptions.category != SearchCategory.ALL || searchData.filterOptions.sortBy != SortByOption.RELEVANCE) AuraColors.MagentaPulse else MaterialTheme.colorScheme.onSurface
            )
        }

        // Autocomplete Suggestion Dropdown Chips
        if (searchData.query.isNotEmpty() && searchData.autocompleteSuggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(searchData.autocompleteSuggestions, key = { it }) { suggestion ->
                    Surface(
                        modifier = Modifier.clickable {
                            searchState.addRecentSearch(suggestion)
                            searchState.updateQuery(suggestion)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = AuraColors.DarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AuraColors.ElectricPurple.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = AuraColors.NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content Category Filter Chips Row
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SearchCategory.entries.toTypedArray(), key = { it.name }) { category ->
                AuraChip(
                    label = category.label,
                    isSelected = searchData.filterOptions.category == category,
                    onClick = { searchState.selectCategory(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SEARCH STATES DISPATCH
        if (searchData.query.isEmpty()) {
            // STATE A: DISCOVERY MODE (EMPTY QUERY)
            DiscoveryModeContent(
                searchData = searchData,
                searchState = searchState,
                onSongSelect = onSongSelect,
                onSongOverflow = { selectedOverflowSong = it }
            )
        } else {
            // STATE B: ACTIVE SEARCH RESULTS MODE
            ActiveSearchResultsContent(
                searchData = searchData,
                searchState = searchState,
                onSongSelect = onSongSelect,
                onSongOverflow = { selectedOverflowSong = it }
            )
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

    // Voice Search Popup Dialog
    if (searchData.isVoiceSearching) {
        VoiceSearchDialog(
            onDismiss = searchState::stopVoiceSearch,
            onResult = searchState::applyVoiceQuery
        )
    }

    // Filter & Sort Bottom Sheet
    if (searchData.showFilterSheet) {
        FilterBottomSheet(
            searchData = searchData,
            searchState = searchState,
            onDismiss = { searchState.toggleFilterSheet(false) }
        )
    }
}

@Composable
private fun DiscoveryModeContent(
    searchData: SearchData,
    searchState: SearchState,
    onSongSelect: (SongEntity) -> Unit,
    onSongOverflow: (SongEntity) -> Unit = {}
) {
    Column {
        // Recent Searches
        if (searchData.recentSearches.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Searches",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Clear All",
                    style = MaterialTheme.typography.labelSmall,
                    color = AuraColors.ErrorRed,
                    modifier = Modifier.clickable(onClick = searchState::clearRecentSearches)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(searchData.recentSearches, key = { it }) { recent ->
                    Surface(
                        modifier = Modifier.clickable { searchState.updateQuery(recent) },
                        shape = RoundedCornerShape(16.dp),
                        color = AuraColors.DarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AuraColors.DarkGlassBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = recent,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { searchState.removeRecentSearch(recent) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Trending Searches
        Text(
            text = "Trending Now ⚡",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(searchData.trendingSearches, key = { it }) { trending ->
                Surface(
                    modifier = Modifier.clickable {
                        searchState.addRecentSearch(trending)
                        searchState.updateQuery(trending)
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = AuraColors.ElectricPurple.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraColors.ElectricPurple)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = AuraColors.NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = trending,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Mood & Activity Picks
        AuraSectionHeader(
            title = "Mood Picks",
            subtitle = "Curated audio streams for every state of mind"
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(searchData.moodCategories, key = { it.id }) { mood ->
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(100.dp)
                        .clip(RoundedCornerShape(AuraRadius.Medium))
                        .clickable { searchState.selectMood(mood) }
                ) {
                    AsyncImage(
                        model = mood.coverUrl,
                        contentDescription = mood.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column {
                            Text(
                                text = mood.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = mood.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = AuraColors.NeonCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Online Curated Catalog Playlists
        AuraSectionHeader(
            title = "Editor Pick Playlists",
            subtitle = "Stream full lossless mixes 100% free"
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            searchData.onlinePlaylists.take(3).forEach { playlist ->
                AuraGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val song = MusicCatalog.ALL_SONGS.firstOrNull()
                        if (song != null) onSongSelect(song)
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = playlist.coverUrl,
                            contentDescription = playlist.name,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${playlist.curator} • ${playlist.trackCount} tracks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AuraHdBadge()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Online Catalog Songs
        AuraSectionHeader(
            title = "Top Online Catalog Songs",
            subtitle = "Tap to stream full track immediately"
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MusicCatalog.ALL_SONGS.forEach { song ->
                AuraListTile(
                    title = song.title,
                    subtitle = "${song.artistName} • ${song.audioQuality}",
                    coverUrl = song.coverUrl,
                    isHdAudio = song.isHdAudio,
                    onClick = {
                        searchState.addRecentSearch(song.title)
                        onSongSelect(song)
                    },
                    onMenuClick = { onSongOverflow(song) }
                )
            }
        }
    }
}

@Composable
private fun ActiveSearchResultsContent(
    searchData: SearchData,
    searchState: SearchState,
    onSongSelect: (SongEntity) -> Unit,
    onSongOverflow: (SongEntity) -> Unit = {}
) {
    val results = searchData.searchResults

    Column {
        // Typo Correction Banner
        if (results.autocorrectedQuery != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(10.dp),
                color = AuraColors.ElectricPurple.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AuraColors.ElectricPurple)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        tint = AuraColors.NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Showing results for \"${results.autocorrectedQuery}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }

        // Loading State
        if (searchData.isSearching) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(4) {
                    AuraSkeletonItem(height = 64.dp)
                }
            }
            return
        }

        // No Results Fallback
        if (results.totalResultCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No tracks found for \"${searchData.query}\"",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Try searching for artists like Lumina Eclipse or CyberPulse",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (results.suggestions.isNotEmpty()) {
                        Text(
                            text = "Suggested Searches:",
                            style = MaterialTheme.typography.labelMedium,
                            color = AuraColors.NeonCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            results.suggestions.forEach { sug ->
                                AuraChip(
                                    label = sug,
                                    isSelected = false,
                                    onClick = { searchState.updateQuery(sug) }
                                )
                            }
                        }
                    }
                }
            }
            return
        }

        // 1. TOP RESULT CARD
        results.topResult?.let { top ->
            Text(
                text = "Top Match",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (top) {
                is SongEntity -> {
                    AuraGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            searchState.addRecentSearch(top.title)
                            onSongSelect(top)
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = top.coverUrl,
                                contentDescription = top.title,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = top.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Song • ${top.artistName} • ${top.albumTitle}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Play",
                                tint = AuraColors.NeonCyan,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
                is ArtistEntity -> {
                    AuraGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val song = MusicCatalog.getSongsByArtist(top.id).firstOrNull()
                            if (song != null) onSongSelect(song)
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AuraAvatar(
                                photoUrl = top.avatarUrl,
                                size = 64.dp,
                                showFreeBadge = false
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = top.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (top.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Verified",
                                            tint = AuraColors.NeonCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Artist • ${top.monthlyListeners / 1000000f}M listeners",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 2. ONLINE SONGS SECTION
        if (results.songs.isNotEmpty()) {
            AuraSectionHeader(
                title = "Online Songs (${results.songs.size})",
                subtitle = "Stream full track instantly"
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                results.songs.forEach { song ->
                    AuraListTile(
                        title = song.title,
                        subtitle = "${song.artistName} • ${song.audioQuality}",
                        coverUrl = song.coverUrl,
                        isHdAudio = song.isHdAudio,
                        onClick = {
                            searchState.addRecentSearch(song.title)
                            onSongSelect(song)
                        },
                        onMenuClick = { onSongOverflow(song) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 3. ARTISTS SECTION
        if (results.artists.isNotEmpty()) {
            AuraSectionHeader(
                title = "Artists (${results.artists.size})"
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(results.artists, key = { it.id }) { artist ->
                    AuraArtistCard(
                        name = artist.name,
                        avatarUrl = artist.avatarUrl,
                        monthlyListeners = "${artist.monthlyListeners / 1000000f}M",
                        onClick = {
                            val artistSong = MusicCatalog.getSongsByArtist(artist.id).firstOrNull()
                            if (artistSong != null) onSongSelect(artistSong)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 4. ALBUMS SECTION
        if (results.albums.isNotEmpty()) {
            AuraSectionHeader(
                title = "Albums (${results.albums.size})"
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(results.albums, key = { it.id }) { album ->
                    AuraAlbumCard(
                        title = album.title,
                        artistName = album.artistName,
                        coverUrl = album.coverUrl,
                        onClick = {
                            val albumSong = MusicCatalog.getSongsByAlbum(album.id).firstOrNull()
                            if (albumSong != null) onSongSelect(albumSong)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 5. PLAYLISTS SECTION
        if (results.playlists.isNotEmpty()) {
            AuraSectionHeader(
                title = "Curated Playlists (${results.playlists.size})"
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                results.playlists.forEach { playlist ->
                    AuraListTile(
                        title = playlist.name,
                        subtitle = "Curated by ${playlist.curator} • ${playlist.trackCount} tracks",
                        coverUrl = playlist.coverUrl,
                        onClick = {
                            val song = MusicCatalog.ALL_SONGS.firstOrNull()
                            if (song != null) onSongSelect(song)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 6. CLEARLY SEPARATED LOCAL DEVICE FILES SECTION
        if (results.localDeviceTracks.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = AuraColors.DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, AuraColors.NeonCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = "On This Device",
                            tint = AuraColors.NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ON THIS DEVICE (Local Library)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AuraColors.NeonCyan
                        )
                    }
                    Text(
                        text = "Local files stored in device storage • Clearly separated from Online Catalog",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        results.localDeviceTracks.forEach { localTrack ->
                            val localSongEntity = SearchRepository.convertLocalTrackToSongEntity(localTrack)

                            AuraGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onSongSelect(localSongEntity) }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FolderZip,
                                        contentDescription = "Local File",
                                        tint = AuraColors.ElectricPurple,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = localTrack.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${localTrack.fileName} • ${localTrack.format} • ${localTrack.fileSizeMb} MB",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AuraColors.ElectricPurple.copy(alpha = 0.3f)
                                    ) {
                                        Text(
                                            text = "LOCAL",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
}

@Composable
private fun VoiceSearchDialog(
    onDismiss: () -> Unit,
    onResult: (String) -> Unit
) {
    var isListening by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "voice_pulse"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .auraGlass(shape = RoundedCornerShape(AuraRadius.Large)),
            shape = RoundedCornerShape(AuraRadius.Large),
            color = AuraColors.DarkSurfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Aura Voice Search",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isListening) "Listening... Speak artist or song name" else "Processing voice audio...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(AuraColors.ElectricPurple.copy(alpha = pulseScale * 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AuraColors.NeonCyan)
                            .clickable {
                                onResult("CyberPulse")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Mic",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Tap sample voice commands:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Lumina Eclipse", "CyberPulse", "Midnight Horizon", "Zero Gravity").forEach { sample ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onResult(sample) },
                            shape = RoundedCornerShape(8.dp),
                            color = AuraColors.DarkBackground
                        ) {
                            Text(
                                text = "🎤 \"Play $sample\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = AuraColors.NeonCyan,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    searchData: SearchData,
    searchState: SearchState,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AuraColors.DarkBackground,
        scrimColor = Color.Black.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search Filters & Sorting",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = {
                    searchState.selectCategory(SearchCategory.ALL)
                    searchState.setSortBy(SortByOption.RELEVANCE)
                    searchState.setDurationFilter("All")
                    searchState.setAudioQualityFilter("All")
                }) {
                    Text("Reset", color = AuraColors.MagentaPulse)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sort By
            Text(
                text = "Sort Results By",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SortByOption.entries.toTypedArray(), key = { it.name }) { sortBy ->
                    AuraChip(
                        label = sortBy.label,
                        isSelected = searchData.filterOptions.sortBy == sortBy,
                        onClick = { searchState.setSortBy(sortBy) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Duration Filter
            Text(
                text = "Track Duration",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All", "< 3 min", "3-5 min", "> 5 min"), key = { it }) { duration ->
                    AuraChip(
                        label = duration,
                        isSelected = searchData.filterOptions.durationFilter == duration,
                        onClick = { searchState.setDurationFilter(duration) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Audio Quality Filter
            Text(
                text = "Audio Quality / Format",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All", "Lossless FLAC", "Hi-Res Masters"), key = { it }) { quality ->
                    AuraChip(
                        label = quality,
                        isSelected = searchData.filterOptions.audioQualityFilter == quality,
                        onClick = { searchState.setAudioQualityFilter(quality) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            AuraButton(
                text = "Apply Filters",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                variant = AuraButtonVariant.PRIMARY_GRADIENT
            )
        }
    }
}
