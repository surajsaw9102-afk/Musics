package com.example.feature.user

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.core.components.*
import com.example.core.database.entities.UserEntity
import com.example.core.designsystem.AuraColors
import com.example.core.repository.StatisticsRepository
import com.example.core.state.*

@Composable
fun ProfileScreen(
    userState: UserState,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
    testTag: String = "profile_screen"
) {
    val session by userState.session.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val stats by StatisticsRepository.stats.collectAsState()
    val playHistory by HistoryManager.playHistory.collectAsState()
    val prefs by PreferencesManager.personalization.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    val user = session.user

    Column(
        modifier = Modifier
            .testTag(testTag)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Section Header
        AuraSectionHeader(
            title = "User Profile",
            subtitle = "Your personalized music identity"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Profile Card
        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    AuraIconButton(
                        icon = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        onClick = { showEditDialog = true },
                        size = 36.dp
                    )
                }

                AuraAvatar(
                    photoUrl = user?.photoUrl ?: uiState.photoUrl,
                    size = 96.dp,
                    showFreeBadge = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user?.displayName ?: uiState.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = user?.username ?: uiState.username,
                    style = MaterialTheme.typography.bodySmall,
                    color = AuraColors.NeonCyan
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = user?.bio ?: uiState.bio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AuraFreeBadge()

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(uiState.accentColor.hexValue).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(uiState.accentColor.hexValue))
                    ) {
                        Text(
                            text = uiState.accentColor.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(uiState.accentColor.hexValue),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isSaved) {
            AuraToastBanner(
                message = "Profile & preferences saved successfully!",
                visible = true,
                type = AuraToastType.SUCCESS
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Profile Tab Switcher
        ScrollableTabRow(
            selectedTabIndex = uiState.selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            contentColor = AuraColors.NeonCyan,
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            val tabTitles = listOf("Overview", "Insights", "Favorites", "History")
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.selectedTab == index,
                    onClick = { viewModel.selectTab(index) },
                    text = { Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        when (uiState.selectedTab) {
            0 -> ProfileOverviewTab(
                uiState = uiState,
                stats = stats,
                playHistory = playHistory,
                user = user
            )
            1 -> ListeningInsightsScreen()
            2 -> FavoritesAndPinnedTab(prefs = prefs)
            3 -> ProfileHistoryTab()
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Sign Out
        AuraButton(
            text = "Sign Out",
            onClick = onLogout,
            icon = Icons.Default.Logout,
            modifier = Modifier.fillMaxWidth(),
            variant = AuraButtonVariant.OUTLINED
        )
    }

    // Edit Profile Sheet/Dialog
    if (showEditDialog) {
        EditProfileDialog(
            uiState = uiState,
            viewModel = viewModel,
            userState = userState,
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
private fun ProfileOverviewTab(
    uiState: ProfileUiState,
    stats: com.example.core.repository.ListeningStatistics,
    playHistory: List<PlayHistoryItem>,
    user: UserEntity?
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Quick Stats
        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatColumn("${stats.activeListeningStreakDays}d", "Streak")
                ProfileStatColumn("${stats.totalHours}h", "Total Time")
                ProfileStatColumn("${stats.songsPlayedCount}", "Tracks")
                ProfileStatColumn("${stats.playlistsCreatedCount}", "Playlists")
            }
        }

        // Favorite Genres
        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Favorite Genres",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                uiState.favoriteGenres.take(4).forEach { genre ->
                    AuraChip(
                        label = genre,
                        isSelected = true,
                        onClick = {}
                    )
                }
            }
        }

        // Favorite Artists
        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Favorite Artists",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                uiState.favoriteArtists.forEach { artist ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AuraColors.ElectricPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = artist,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Recently Played Summary
        if (playHistory.isNotEmpty()) {
            AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Recently Played Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                playHistory.take(3).forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = item.song.coverUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.song.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.song.artistName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesAndPinnedTab(prefs: PersonalizationPreferences) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Pinned Items
        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pinned Items (${prefs.pinnedItemIds.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = null,
                    tint = AuraColors.AmberGlow
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            val pinnedList = listOf(
                Pair("art_01", "Aura Synthetics (Artist)"),
                Pair("alb_01", "Neon Horizon (Album)"),
                Pair("sng_01", "Blinding Lights Cyber (Song)")
            )

            pinnedList.forEach { pair ->
                val isPinned = prefs.pinnedItemIds.contains(pair.first)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pair.second,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(
                        onClick = { PreferencesManager.togglePinItem(pair.first) }
                    ) {
                        Icon(
                            imageVector = if (isPinned) Icons.Default.PushPin else Icons.Default.PinEnd,
                            contentDescription = "Pin/Unpin",
                            tint = if (isPinned) AuraColors.AmberGlow else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Favorites Access
        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Library Favorites Quick Access",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            FavoriteCategoryRow("Liked Songs", "14 Tracks", Icons.Default.Favorite, AuraColors.MagentaFlare)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            FavoriteCategoryRow("Liked Albums", "6 Albums", Icons.Default.Album, AuraColors.NeonCyan)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            FavoriteCategoryRow("Followed Artists", "8 Artists", Icons.Default.Person, AuraColors.ElectricPurple)
        }
    }
}

@Composable
private fun FavoriteCategoryRow(title: String, count: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        Text(text = count, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ProfileHistoryTab() {
    val playHistory by HistoryManager.playHistory.collectAsState()
    val searchHistory by HistoryManager.searchHistory.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Playback History (${playHistory.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = HistoryManager::clearPlayHistory) {
                    Text("Clear All", color = AuraColors.MagentaFlare, style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            playHistory.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item.song.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(text = item.song.artistName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    AuraIconButton(
                        icon = Icons.Default.Clear,
                        contentDescription = "Delete",
                        onClick = { HistoryManager.removePlayHistoryItem(item.id) },
                        size = 28.dp
                    )
                }
            }
        }

        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Searches (${searchHistory.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = HistoryManager::clearSearchHistory) {
                    Text("Clear All", color = AuraColors.MagentaFlare, style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            searchHistory.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item.query, style = MaterialTheme.typography.bodyMedium)
                    AuraIconButton(
                        icon = Icons.Default.Clear,
                        contentDescription = "Delete",
                        onClick = { HistoryManager.removeSearchQuery(item.id) },
                        size = 28.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileStatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AuraColors.NeonCyan
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditProfileDialog(
    uiState: ProfileUiState,
    viewModel: ProfileViewModel,
    userState: UserState,
    onDismiss: () -> Unit
) {
    AuraGlassDialog(
        onDismissRequest = onDismiss,
        title = "Edit Profile & Personalization"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar Selector
            Text(text = "Choose Profile Avatar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AVATAR_PRESETS) { url ->
                    val isSelected = uiState.photoUrl == url
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) AuraColors.NeonCyan else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { viewModel.updatePhotoUrl(url) },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            AuraTextField(
                value = uiState.displayName,
                onValueChange = viewModel::updateName,
                placeholder = "Display Name"
            )

            AuraTextField(
                value = uiState.username,
                onValueChange = viewModel::updateUsername,
                placeholder = "Username (@listener)"
            )

            AuraTextField(
                value = uiState.bio,
                onValueChange = viewModel::updateBio,
                placeholder = "Bio / Short Description"
            )

            // Accent Color Selector
            Text(text = "Profile Accent Theme", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AuraAccentColor.values().take(4).forEach { accent ->
                    val isSelected = uiState.accentColor == accent
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(accent.hexValue))
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clickable { viewModel.updateAccentColor(accent) }
                    )
                }
            }

            // Genres
            Text(text = "Favorite Genres", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ALL_GENRE_OPTIONS.forEach { genre ->
                    val isSel = genre in uiState.favoriteGenres
                    AuraChip(
                        label = genre,
                        isSelected = isSel,
                        onClick = { viewModel.toggleGenreSelection(genre) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            AuraButton(
                text = "Save Profile Changes",
                onClick = {
                    viewModel.saveProfile(userState)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                variant = AuraButtonVariant.PRIMARY_GRADIENT
            )

            AuraButton(
                text = "Cancel",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                variant = AuraButtonVariant.OUTLINED
            )
        }
    }
}
