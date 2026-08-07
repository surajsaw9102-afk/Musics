package com.example.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.core.components.*
import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass

@Composable
fun HomeScreen(
    onSongSelect: (SongEntity) -> Unit,
    onAlbumSelect: (AlbumEntity) -> Unit,
    onArtistSelect: (ArtistEntity) -> Unit,
    onNavigateToProfile: (() -> Unit)? = null,
    onNavigateToDownloads: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    onNavigateToSocial: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: HomeState = viewModel(),
    testTag: String = "home_screen"
) {
    val feedState by viewModel.homeFeed.collectAsState()
    val categories = remember { listOf("All", "Made For You", "Chill", "Energy", "Focus") }
    var selectedOverflowSong by remember { mutableStateOf<SongEntity?>(null) }

    Column(
        modifier = modifier
            .testTag(testTag)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp) // Space for bottom player bar
    ) {
        // Dynamic Time-of-Day Personalized Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(AuraRadius.Large))
                .auraGlass(shape = RoundedCornerShape(AuraRadius.Large))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            AuraColors.DarkSurfaceVariant,
                            AuraColors.ElectricPurple.copy(alpha = 0.35f),
                            AuraColors.NeonCyan.copy(alpha = 0.15f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AuraColors.NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        AuraFreeBadge()
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (onNavigateToSocial != null) {
                            IconButton(onClick = onNavigateToSocial, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.People, contentDescription = "Social Hub", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        if (onNavigateToDownloads != null) {
                            IconButton(onClick = onNavigateToDownloads, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.CloudDownload, contentDescription = "Downloads", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        if (onNavigateToSettings != null) {
                            IconButton(onClick = onNavigateToSettings, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        if (onNavigateToProfile != null) {
                            IconButton(onClick = onNavigateToProfile, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Person, contentDescription = "Profile", tint = AuraColors.NeonCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                        IconButton(
                            onClick = { viewModel.refreshFeed() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Feed",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = feedState.timeOfDayGreeting,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = feedState.timeOfDaySubtext,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Taste Profile Vibe Badge
                Surface(
                    shape = RoundedCornerShape(AuraRadius.Pill),
                    color = AuraColors.ElectricPurple.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        AuraColors.ElectricPurple.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AuraColors.NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Taste Pulse: ${feedState.topVibeSummary}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories, key = { it }) { category ->
                AuraChip(
                    label = category,
                    isSelected = category == feedState.activeCategoryFilter,
                    onClick = { viewModel.setCategoryFilter(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Loading Skeleton
        if (feedState.isLoading) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AuraSkeletonItem(height = 120.dp)
                AuraSkeletonItem(height = 180.dp)
                AuraSkeletonItem(height = 220.dp)
            }
        } else if (feedState.sections.isEmpty()) {
            AuraEmptyState(
                title = "Your Feed is Ready",
                description = "Start listening to songs to generate your personalized home recommendations.",
                actionButtonText = "Reset Profile",
                onActionClick = { viewModel.resetToFreshUser() },
                modifier = Modifier.padding(24.dp)
            )
        } else {
            // Render Dynamic Sections
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                feedState.sections.forEach { section ->
                    HomeSectionView(
                        section = section,
                        onSongSelect = { song ->
                            viewModel.onSongPlayed(song)
                            onSongSelect(song)
                        },
                        onAlbumSelect = onAlbumSelect,
                        onArtistSelect = onArtistSelect,
                        onLikeToggle = { song, isLiked ->
                            viewModel.onSongLiked(song, isLiked)
                        },
                        onSongOverflow = { song -> selectedOverflowSong = song }
                    )
                }
            }
        }
    }

    if (selectedOverflowSong != null) {
        val song = selectedOverflowSong!!
        com.example.core.components.SongOverflowSheet(
            song = song,
            onDismiss = { selectedOverflowSong = null },
            onPlayNow = {
                onSongSelect(song)
                selectedOverflowSong = null
            }
        )
    }
}

@Composable
private fun HomeSectionView(
    section: HomeSection,
    onSongSelect: (SongEntity) -> Unit,
    onAlbumSelect: (AlbumEntity) -> Unit,
    onArtistSelect: (ArtistEntity) -> Unit,
    onLikeToggle: (SongEntity, Boolean) -> Unit,
    onSongOverflow: (SongEntity) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header with Rationale Badge if available
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuraSectionHeader(
                title = section.title,
                subtitle = section.subtitle,
                modifier = Modifier.weight(1f)
            )

            if (section.rationaleBadge != null) {
                Surface(
                    shape = RoundedCornerShape(AuraRadius.Small),
                    color = AuraColors.ElectricPurple.copy(alpha = 0.25f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = section.rationaleBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = AuraColors.NeonCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (section.type) {
            HomeSectionType.SPEED_DIAL -> {
                SpeedDialGrid(
                    songs = section.items.filterIsInstance<SongEntity>(),
                    onSongClick = onSongSelect
                )
            }

            HomeSectionType.CONTINUE_LISTENING -> {
                val heroSong = section.items.filterIsInstance<SongEntity>().firstOrNull()
                if (heroSong != null) {
                    ContinueListeningHeroCard(
                        song = heroSong,
                        onResumeClick = { onSongSelect(heroSong) }
                    )
                }
            }

            HomeSectionType.MOOD_PICKS -> {
                val moodPicks = section.items.filterIsInstance<MoodPickItem>()
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(moodPicks, key = { it.id }) { pick ->
                        MoodPickCard(
                            pick = pick,
                            onClick = {
                                // Select first song matching target genre
                                val matchingSongs = com.example.core.catalog.MusicCatalog.getSongsByGenre(pick.targetGenreOrMood)
                                if (matchingSongs.isNotEmpty()) {
                                    onSongSelect(matchingSongs.first())
                                }
                            }
                        )
                    }
                }
            }

            HomeSectionType.RECOMMENDED_ARTISTS -> {
                val artists = section.items.filterIsInstance<ArtistEntity>()
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(artists, key = { it.id }) { artist ->
                        AuraArtistCard(
                            name = artist.name,
                            avatarUrl = artist.avatarUrl,
                            monthlyListeners = "${artist.monthlyListeners / 1000000f}M",
                            onClick = {
                                val artistSongs = com.example.core.catalog.MusicCatalog.getSongsByArtist(artist.id)
                                if (artistSongs.isNotEmpty()) {
                                    onSongSelect(artistSongs.first())
                                } else {
                                    onArtistSelect(artist)
                                }
                            }
                        )
                    }
                }
            }

            HomeSectionType.RECOMMENDED_ALBUMS -> {
                val albums = section.items.filterIsInstance<AlbumEntity>()
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(albums, key = { it.id }) { album ->
                        AuraAlbumCard(
                            title = album.title,
                            artistName = album.artistName,
                            coverUrl = album.coverUrl,
                            onClick = {
                                val albumSongs = com.example.core.catalog.MusicCatalog.getSongsByAlbum(album.id)
                                if (albumSongs.isNotEmpty()) {
                                    onSongSelect(albumSongs.first())
                                } else {
                                    onAlbumSelect(album)
                                }
                            }
                        )
                    }
                }
            }

            HomeSectionType.DISCOVER_NEW -> {
                val songs = section.items.filterIsInstance<SongEntity>()
                DiscoverNewBanner(
                    songs = songs,
                    onSongClick = onSongSelect
                )
            }

            else -> {
                // Vertical list tiles for songs (YOUR_CHOICES, BECAUSE_YOU_LISTENED_TO, MORE_LIKE_THIS, TRENDING_NOW)
                val songs = section.items.filterIsInstance<SongEntity>()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    songs.take(5).forEach { song ->
                        AuraListTile(
                            title = song.title,
                            subtitle = "${song.artistName} • ${song.albumTitle}",
                            coverUrl = song.coverUrl,
                            isHdAudio = song.isHdAudio,
                            onClick = { onSongSelect(song) },
                            onMenuClick = { onSongOverflow(song) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedDialGrid(
    songs: List<SongEntity>,
    onSongClick: (SongEntity) -> Unit
) {
    val displaySongs = songs.take(6)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        displaySongs.chunked(2).forEach { rowSongs ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowSongs.forEach { song ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(AuraRadius.Medium))
                            .auraGlass(shape = RoundedCornerShape(AuraRadius.Medium))
                            .clickable { onSongClick(song) }
                            .padding(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            AsyncImage(
                                model = song.coverUrl,
                                contentDescription = song.title,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(AuraRadius.Small)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
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

                            Box(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(AuraColors.ElectricPurple)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueListeningHeroCard(
    song: SongEntity,
    onResumeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AuraRadius.Medium))
            .auraGlass(shape = RoundedCornerShape(AuraRadius.Medium))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        AuraColors.ElectricPurple.copy(alpha = 0.25f),
                        AuraColors.DarkSurfaceVariant
                    )
                )
            )
            .clickable(onClick = onResumeClick)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = song.coverUrl,
                contentDescription = song.title,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(AuraRadius.Medium)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LAST PLAYED TRACK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AuraColors.NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${song.artistName} • ${song.albumTitle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { 0.45f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = AuraColors.NeonCyan,
                    trackColor = Color.White.copy(alpha = 0.15f)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AuraColors.ElectricPurple)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Resume",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun MoodPickCard(
    pick: MoodPickItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(AuraRadius.Medium))
            .auraGlass(shape = RoundedCornerShape(AuraRadius.Medium))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = pick.coverUrl,
            contentDescription = pick.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(12.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                Text(
                    text = pick.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = pick.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DiscoverNewBanner(
    songs: List<SongEntity>,
    onSongClick: (SongEntity) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        songs.take(3).forEach { song ->
            AuraListTile(
                title = song.title,
                subtitle = "${song.genre} • ${song.artistName}",
                coverUrl = song.coverUrl,
                isHdAudio = song.isHdAudio,
                onClick = { onSongClick(song) },
                onMenuClick = {}
            )
        }
    }
}
