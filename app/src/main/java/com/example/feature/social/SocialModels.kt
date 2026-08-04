package com.example.feature.social

import com.example.core.database.entities.SongEntity

enum class SocialActivityType {
    LISTENED_SONG,
    LIKED_SONG,
    CREATED_PLAYLIST,
    FOLLOWED_ARTIST,
    UPDATED_SHARED_PLAYLIST
}

data class SocialActivityItem(
    val id: String,
    val userId: String,
    val userName: String,
    val userHandle: String,
    val userAvatar: String,
    val activityType: SocialActivityType,
    val timestampMs: Long,
    val targetTitle: String,
    val targetSubtitle: String? = null,
    val targetImageUrl: String? = null,
    val targetId: String? = null,
    val song: SongEntity? = null,
    val note: String? = null
)

data class PrivacySettings(
    val showListeningActivity: Boolean = true,
    val showPlayedSongs: Boolean = true,
    val showPublicPlaylists: Boolean = true,
    val isPrivateProfile: Boolean = false,
    val friendsOnlyVisibility: Boolean = false
)

data class UserSocialProfile(
    val id: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String,
    val bio: String,
    val favoriteGenres: List<String>,
    val favoriteArtists: List<String>,
    val publicPlaylistsCount: Int,
    val followersCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean = false,
    val isConnected: Boolean = false,
    val privacySettings: PrivacySettings = PrivacySettings(),
    val listeningActivitySummary: String = "Listened to 42 songs this week"
)

enum class ConnectionStatus {
    CONNECTED,
    PENDING_SENT,
    PENDING_RECEIVED,
    NOT_CONNECTED
}

data class UserConnection(
    val userId: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String,
    val bio: String,
    val status: ConnectionStatus,
    val mutualFollowsCount: Int = 0,
    val favoriteGenre: String = "Synthwave"
)

enum class CollaboratorRole {
    OWNER,
    CONTRIBUTOR
}

data class CollaboratorInfo(
    val userId: String,
    val name: String,
    val handle: String,
    val avatarUrl: String,
    val role: CollaboratorRole,
    val tracksAddedCount: Int = 0,
    val joinedAtMs: Long = System.currentTimeMillis()
)

data class PlaylistActivityLog(
    val id: String,
    val timestampMs: Long,
    val userId: String,
    val userName: String,
    val action: String, // e.g. "Added 'Stellar Drift'", "Removed 'Lunar Echo'"
    val songTitle: String? = null
)

data class CollaborativePlaylist(
    val playlistId: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val isCollaborative: Boolean = true,
    val inviteCode: String,
    val ownerId: String,
    val ownerName: String,
    val collaborators: List<CollaboratorInfo> = emptyList(),
    val activityLogs: List<PlaylistActivityLog> = emptyList(),
    val songIds: List<String> = emptyList()
)

enum class ShareContentType {
    SONG,
    ALBUM,
    ARTIST,
    PLAYLIST,
    PROFILE,
    CURRENT_PLAYBACK,
    LISTENING_ACTIVITY
}

data class ShareContent(
    val contentType: ShareContentType,
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val shareUrl: String,
    val deepLink: String,
    val description: String? = null,
    val song: SongEntity? = null
)

data class FollowedArtistRelease(
    val artistId: String,
    val artistName: String,
    val artistAvatar: String,
    val releaseTitle: String,
    val releaseType: String, // "Album" | "Single" | "EP"
    val coverUrl: String,
    val releaseDate: String,
    val songs: List<SongEntity>
)
