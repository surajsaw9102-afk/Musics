package com.example.feature.social

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.catalog.MusicCatalog
import com.example.core.components.*
import com.example.core.database.entities.SongEntity
import com.example.core.designsystem.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserSocialProfileScreen(
    userId: String,
    onBackClick: () -> Unit,
    onPlaySong: (SongEntity) -> Unit,
    onOpenPlaylist: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val isSelf = userId == "user_me" || userId == "self"
    var profile by remember { mutableStateOf(SocialRepository.getUserProfile(userId)) }
    var isFollowing by remember { mutableStateOf(profile?.isFollowing ?: false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val privacySettings by ProfileVisibilityManager.privacySettings.collectAsState()

    val songs = remember { MusicCatalog.ALL_SONGS }
    val topSongs = remember { songs.take(4) }
    val collaborativePlaylists by CollaborationManager.collaborativePlaylists.collectAsState()

    if (profile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AuraErrorState(
                message = "Profile not found",
                onRetry = onBackClick
            )
        }
        return
    }

    val currentProfile = profile!!

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("profile_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "@${currentProfile.username}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row {
                    IconButton(
                        onClick = { showShareSheet = true },
                        modifier = Modifier.testTag("profile_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share Profile",
                            tint = AuraColors.NeonCyan
                        )
                    }

                    if (isSelf) {
                        IconButton(
                            onClick = { showPrivacyDialog = true },
                            modifier = Modifier.testTag("privacy_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = "Privacy Settings",
                                tint = AuraColors.ElectricPurple
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Hero Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .auraGlass(shape = RoundedCornerShape(AuraRadius.Large))
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            AuraAvatar(
                                photoUrl = currentProfile.avatarUrl,
                                size = 96.dp,
                                showFreeBadge = true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = currentProfile.displayName,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Text(
                            text = "@${currentProfile.username}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = AuraColors.NeonCyan
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentProfile.bio,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Follower Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatColumn(title = "Followers", value = "${currentProfile.followersCount}")
                            StatColumn(title = "Following", value = "${currentProfile.followingCount}")
                            StatColumn(title = "Playlists", value = "${currentProfile.publicPlaylistsCount}")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons
                        if (!isSelf) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (isFollowing) {
                                    AuraButton(
                                        text = "Following",
                                        icon = Icons.Filled.Check,
                                        variant = AuraButtonVariant.SECONDARY,
                                        onClick = {
                                            FollowManager.toggleFollowUser(userId)
                                            isFollowing = false
                                            Toast.makeText(context, "Unfollowed @${currentProfile.username}", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        testTag = "unfollow_user_button"
                                    )
                                } else {
                                    AuraButton(
                                        text = "Follow User",
                                        icon = Icons.Filled.PersonAdd,
                                        onClick = {
                                            FollowManager.toggleFollowUser(userId)
                                            isFollowing = true
                                            Toast.makeText(context, "Following @${currentProfile.username}!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        testTag = "follow_user_button"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Listening Activity Summary
            if (isSelf || privacySettings.showListeningActivity) {
                item {
                    AuraSectionHeader(
                        title = "Listening Summary",
                        subtitle = "Weekly activity highlights"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .auraGlass(shape = RoundedCornerShape(AuraRadius.Medium))
                            .padding(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.GraphicEq,
                                contentDescription = "Listening Summary",
                                tint = AuraColors.ElectricPurple,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentProfile.listeningActivitySummary,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "100% Free Streaming on Aura",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AuraColors.NeonCyan
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Favorite Genres Chips
            item {
                AuraSectionHeader(title = "Favorite Genres")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentProfile.favoriteGenres.forEach { genre ->
                        AuraChip(
                            label = genre,
                            isSelected = true,
                            onClick = {}
                        )
                    }
                }
            }

            // Top Songs Section
            if (isSelf || privacySettings.showPlayedSongs) {
                item {
                    AuraSectionHeader(title = "Top Songs This Month")
                }

                items(topSongs) { song ->
                    AuraListTile(
                        title = song.title,
                        subtitle = song.artistName,
                        coverUrl = song.coverUrl,
                        onClick = { onPlaySong(song) }
                    )
                }
            }

            // Public & Collaborative Playlists
            if (isSelf || privacySettings.showPublicPlaylists) {
                item {
                    AuraSectionHeader(title = "Public & Collaborative Playlists")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(collaborativePlaylists) { playlist ->
                            AuraAlbumCard(
                                title = playlist.title,
                                artistName = "${playlist.collaborators.size} collaborators",
                                coverUrl = playlist.coverUrl,
                                onClick = { onOpenPlaylist(playlist.playlistId) }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Share Sheet Dialog
    if (showShareSheet) {
        AuraShareSheet(
            shareContent = ShareContent(
                contentType = ShareContentType.PROFILE,
                id = currentProfile.id,
                title = currentProfile.displayName,
                subtitle = "@${currentProfile.username}",
                imageUrl = currentProfile.avatarUrl,
                shareUrl = ShareManager.createShareUrl(ShareContentType.PROFILE, currentProfile.id),
                deepLink = ShareManager.createDeepLink(ShareContentType.PROFILE, currentProfile.id),
                description = currentProfile.bio
            ),
            onDismiss = { showShareSheet = false }
        )
    }

    // Privacy Settings Dialog for Self
    if (showPrivacyDialog) {
        PrivacySettingsDialog(onDismiss = { showPrivacyDialog = false })
    }
}

@Composable
private fun StatColumn(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = AuraColors.NeonCyan
            )
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
fun PrivacySettingsDialog(onDismiss: () -> Unit) {
    val privacySettings by ProfileVisibilityManager.privacySettings.collectAsState()

    AuraGlassDialog(
        title = "Privacy & Visibility Controls",
        onDismissRequest = onDismiss,
        testTag = "privacy_controls_dialog"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Control what listening activity and playlists are visible to friends and other users on Aura.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PrivacyToggleRow(
                label = "Show Listening Activity",
                subtitle = "Let friends see songs you are listening to",
                checked = privacySettings.showListeningActivity,
                onCheckedChange = ProfileVisibilityManager::updateListeningActivityVisibility
            )

            PrivacyToggleRow(
                label = "Show Played Songs",
                subtitle = "Display top played tracks on your public profile",
                checked = privacySettings.showPlayedSongs,
                onCheckedChange = ProfileVisibilityManager::updatePlayedSongsVisibility
            )

            PrivacyToggleRow(
                label = "Show Public Playlists",
                subtitle = "Allow others to discover your created playlists",
                checked = privacySettings.showPublicPlaylists,
                onCheckedChange = ProfileVisibilityManager::updatePublicPlaylistsVisibility
            )

            PrivacyToggleRow(
                label = "Private Profile",
                subtitle = "Hide your social profile from public search",
                checked = privacySettings.isPrivateProfile,
                onCheckedChange = ProfileVisibilityManager::updatePrivateProfile
            )

            Spacer(modifier = Modifier.height(10.dp))

            AuraButton(
                text = "Done",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PrivacyToggleRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AuraRadius.Medium))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AuraColors.ElectricPurple
            )
        )
    }
}
