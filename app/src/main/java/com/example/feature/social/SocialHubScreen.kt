package com.example.feature.social

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.catalog.MusicCatalog
import com.example.core.components.*
import com.example.core.database.entities.SongEntity
import com.example.core.designsystem.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialHubScreen(
    onPlaySong: (SongEntity) -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenPlaylist: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Activity Feed, 1 = Followed Artists, 2 = Friends / Search, 3 = Discovery

    val activities by ActivityFeedRepository.activities.collectAsState()
    val artistReleases by FollowedArtistsFeedRepository.artistReleases.collectAsState()
    val connections by ConnectionRepository.connections.collectAsState()
    val collaborativePlaylists by CollaborationManager.collaborativePlaylists.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var shareTargetContent by remember { mutableStateOf<ShareContent?>(null) }
    var showJoinCollabSheet by remember { mutableStateOf(false) }

    val filteredConnections = remember(searchQuery, connections) {
        ConnectionRepository.searchUsers(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Social Hub & Community",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "100% Free Music Sharing & Friends Activity",
                            style = MaterialTheme.typography.labelSmall.copy(color = AuraColors.NeonCyan)
                        )
                    }
                },
                actions = {
                    AuraFreeBadge(modifier = Modifier.padding(end = 16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Social Hub Main Navigation Tabs
            AuraSegmentedTabs(
                tabs = listOf("Activity Feed", "Artists Feed", "Friends", "Discovery"),
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            AnimatedContent(
                targetState = selectedTab,
                label = "social_hub_tab_transition",
                modifier = Modifier.weight(1f)
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> ActivityFeedTab(
                        activities = activities,
                        onPlaySong = onPlaySong,
                        onOpenProfile = onOpenProfile,
                        onOpenPlaylist = onOpenPlaylist,
                        onShareContent = { shareTargetContent = it }
                    )
                    1 -> FollowedArtistsTab(
                        artistReleases = artistReleases,
                        onPlaySong = onPlaySong,
                        onOpenArtist = { artistId ->
                            Toast.makeText(context, "Opening Artist $artistId", Toast.LENGTH_SHORT).show()
                        }
                    )
                    2 -> FriendsTab(
                        connections = filteredConnections,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onOpenProfile = onOpenProfile,
                        onConnect = { userId ->
                            ConnectionRepository.sendConnectionRequest(userId)
                            Toast.makeText(context, "Connection request sent!", Toast.LENGTH_SHORT).show()
                        },
                        onAccept = { userId ->
                            ConnectionRepository.acceptConnectionRequest(userId)
                            Toast.makeText(context, "Connection accepted!", Toast.LENGTH_SHORT).show()
                        },
                        onDecline = { userId ->
                            ConnectionRepository.declineConnectionRequest(userId)
                        }
                    )
                    3 -> SocialDiscoveryTab(
                        onPlaySong = onPlaySong,
                        onOpenPlaylist = onOpenPlaylist,
                        onOpenProfile = onOpenProfile,
                        collaborativePlaylists = collaborativePlaylists,
                        onJoinCollabClick = { showJoinCollabSheet = true }
                    )
                }
            }
        }
    }

    // Share Sheet Modal
    shareTargetContent?.let { content ->
        AuraShareSheet(
            shareContent = content,
            onDismiss = { shareTargetContent = null },
            onPlayClick = content.song?.let { song -> { onPlaySong(song) } }
        )
    }

    // Join Collaborative Playlist Sheet
    if (showJoinCollabSheet) {
        CollaborativePlaylistManagerDialog(
            playlistId = "pl_collab_synth",
            onDismiss = { showJoinCollabSheet = false }
        )
    }
}

@Composable
private fun ActivityFeedTab(
    activities: List<SocialActivityItem>,
    onPlaySong: (SongEntity) -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenPlaylist: (String) -> Unit,
    onShareContent: (ShareContent) -> Unit
) {
    val context = LocalContext.current

    if (activities.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AuraEmptyState(
                title = "No Friends Activity Yet",
                description = "Follow users or connect with friends to see listening activity and created playlists!",
                icon = Icons.Filled.People
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Live Activity Stream",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(activities) { activity ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .auraGlass(shape = RoundedCornerShape(AuraRadius.Medium))
                        .padding(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // User Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onOpenProfile(activity.userId) }
                            ) {
                                AuraAvatar(
                                    photoUrl = activity.userAvatar,
                                    size = 40.dp,
                                    showFreeBadge = false
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = activity.userName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = "${activity.userHandle} • ${getActivityBadgeText(activity.activityType)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = AuraColors.NeonCyan
                                        )
                                    )
                                }
                            }

                            AuraChip(
                                label = activity.activityType.name.replace("_", " "),
                                isSelected = false,
                                onClick = {}
                            )
                        }

                        // Target Content Media Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AuraRadius.Small))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            activity.targetImageUrl?.let { imgUrl ->
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(AuraRadius.Small))
                                        .clickable {
                                            if (activity.song != null) {
                                                onPlaySong(activity.song)
                                            } else if (activity.targetId != null) {
                                                onOpenPlaylist(activity.targetId)
                                            }
                                        }
                                ) {
                                    AsyncImage(
                                        model = imgUrl,
                                        contentDescription = activity.targetTitle,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activity.targetTitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                activity.targetSubtitle?.let { sub ->
                                    Text(
                                        text = sub,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (activity.song != null) {
                                IconButton(onClick = { onPlaySong(activity.song) }) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = "Play",
                                        tint = AuraColors.ElectricPurple
                                    )
                                }
                            }
                        }

                        if (!activity.note.isNullOrBlank()) {
                            Text(
                                text = "💬 \"${activity.note}\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        // Action Footer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                Toast.makeText(context, "Liked activity!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(onClick = {
                                onShareContent(
                                    ShareContent(
                                        contentType = ShareContentType.LISTENING_ACTIVITY,
                                        id = activity.id,
                                        title = "${activity.userName}'s Activity",
                                        subtitle = activity.targetTitle,
                                        imageUrl = activity.targetImageUrl ?: "",
                                        shareUrl = ShareManager.createShareUrl(ShareContentType.LISTENING_ACTIVITY, activity.id),
                                        deepLink = ShareManager.createDeepLink(ShareContentType.LISTENING_ACTIVITY, activity.id),
                                        description = activity.note ?: activity.targetSubtitle,
                                        song = activity.song
                                    )
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = "Share",
                                    tint = AuraColors.NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun FollowedArtistsTab(
    artistReleases: List<FollowedArtistRelease>,
    onPlaySong: (SongEntity) -> Unit,
    onOpenArtist: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "New Releases from Artists You Follow",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(artistReleases) { release ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .auraGlass(shape = RoundedCornerShape(AuraRadius.Large))
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onOpenArtist(release.artistId) }
                        ) {
                            AuraAvatar(
                                photoUrl = release.artistAvatar,
                                size = 42.dp,
                                showFreeBadge = false
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = release.artistName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${release.releaseType} • ${release.releaseDate}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = AuraColors.NeonCyan)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AuraRadius.Pill))
                                .background(AuraColors.ElectricPurple.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = release.releaseType,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AuraColors.NeonCyan
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(AuraRadius.Medium))
                        ) {
                            AsyncImage(
                                model = release.coverUrl,
                                contentDescription = release.releaseTitle,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = release.releaseTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${release.songs.size} tracks available",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (release.songs.isNotEmpty()) {
                                AuraButton(
                                    text = "Play Release",
                                    icon = Icons.Filled.PlayArrow,
                                    onClick = { onPlaySong(release.songs.first()) }
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun FriendsTab(
    connections: List<UserConnection>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onConnect: (String) -> Unit,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        AuraSearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = "Search users by name, handle or genre...",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(connections) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .auraGlass(shape = RoundedCornerShape(AuraRadius.Medium))
                        .clickable { onOpenProfile(user.userId) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AuraAvatar(
                        photoUrl = user.avatarUrl,
                        size = 44.dp,
                        showFreeBadge = false
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "@${user.username} • ${user.mutualFollowsCount} mutual connects • ${user.favoriteGenre}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    when (user.status) {
                        ConnectionStatus.CONNECTED -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(AuraRadius.Pill))
                                    .background(AuraColors.ElectricPurple.copy(alpha = 0.25f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Connected",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraColors.NeonCyan
                                )
                            }
                        }
                        ConnectionStatus.PENDING_RECEIVED -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { onAccept(user.userId) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Accept",
                                        tint = AuraColors.NeonCyan
                                    )
                                }
                                IconButton(onClick = { onDecline(user.userId) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Decline",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        ConnectionStatus.PENDING_SENT -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(AuraRadius.Pill))
                                    .background(Color.Gray.copy(alpha = 0.25f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Pending",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        ConnectionStatus.NOT_CONNECTED -> {
                            AuraButton(
                                text = "Connect",
                                icon = Icons.Filled.PersonAdd,
                                onClick = { onConnect(user.userId) }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SocialDiscoveryTab(
    onPlaySong: (SongEntity) -> Unit,
    onOpenPlaylist: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    collaborativePlaylists: List<CollaborativePlaylist>,
    onJoinCollabClick: () -> Unit
) {
    val songs = remember { MusicCatalog.ALL_SONGS }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Friends are Listening To Carousel
        item {
            AuraSectionHeader(
                title = "Friends Are Listening To",
                subtitle = "Real-time trends from your music network"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(songs.take(5)) { song ->
                    AuraAlbumCard(
                        title = song.title,
                        artistName = song.artistName,
                        coverUrl = song.coverUrl,
                        onClick = { onPlaySong(song) }
                    )
                }
            }
        }

        // Collaborative Playlists Section
        item {
            AuraSectionHeader(
                title = "Collaborative Playlists",
                subtitle = "Add songs with friends in real time",
                onSeeAllClick = onJoinCollabClick
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(collaborativePlaylists) { playlist ->
                    AuraAlbumCard(
                        title = playlist.title,
                        artistName = "${playlist.collaborators.size} contributors",
                        coverUrl = playlist.coverUrl,
                        onClick = { onOpenPlaylist(playlist.playlistId) }
                    )
                }
            }
        }

        // Recommended Profiles
        item {
            AuraSectionHeader(title = "Recommended Music Connections")

            val suggestedUsers = listOf(
                Pair("user_alex", SocialRepository.getUserProfile("user_alex")),
                Pair("user_sara", SocialRepository.getUserProfile("user_sara"))
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                suggestedUsers.forEach { (id, profile) ->
                    if (profile != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .auraGlass(shape = RoundedCornerShape(AuraRadius.Medium))
                                .clickable { onOpenProfile(id) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AuraAvatar(
                                photoUrl = profile.avatarUrl,
                                size = 44.dp,
                                showFreeBadge = false
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "@${profile.username} • ${profile.bio}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            AuraButton(
                                text = "View Profile",
                                icon = Icons.Filled.Person,
                                onClick = { onOpenProfile(id) }
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

private fun getActivityBadgeText(type: SocialActivityType): String {
    return when (type) {
        SocialActivityType.LISTENED_SONG -> "listened to a song"
        SocialActivityType.LIKED_SONG -> "liked a song"
        SocialActivityType.CREATED_PLAYLIST -> "created a playlist"
        SocialActivityType.FOLLOWED_ARTIST -> "followed an artist"
        SocialActivityType.UPDATED_SHARED_PLAYLIST -> "updated a shared playlist"
    }
}
