package com.example.feature.social

import com.example.core.catalog.MusicCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SocialRepository {

    private val userProfilesMap = mutableMapOf(
        "user_alex" to UserSocialProfile(
            id = "user_alex",
            displayName = "Alex Mercer",
            username = "alexm",
            avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=300",
            bio = "Synthwave producer & nocturnal motorist 🚗⚡ Creating retro-futuristic soundscapes.",
            favoriteGenres = listOf("Synthwave", "Cyberpunk", "Retrowave"),
            favoriteArtists = listOf("Synthwave King", "Retro Dreamer", "Kavinsky"),
            publicPlaylistsCount = 6,
            followersCount = 342,
            followingCount = 180,
            isFollowing = true,
            isConnected = true,
            listeningActivitySummary = "Streamed 84 hours of Synthwave this month"
        ),
        "user_sara" to UserSocialProfile(
            id = "user_sara",
            displayName = "Sara Connor",
            username = "sarac",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300",
            bio = "Lofi beats & ambient chill collector. Always coding with coffee ☕",
            favoriteGenres = listOf("Lofi", "Ambient", "Chillhop"),
            favoriteArtists = listOf("Retro Dreamer", "Tycho", "Lofi Girl"),
            publicPlaylistsCount = 12,
            followersCount = 890,
            followingCount = 210,
            isFollowing = true,
            isConnected = true,
            listeningActivitySummary = "Listened to 120 tracks this week"
        ),
        "user_david" to UserSocialProfile(
            id = "user_david",
            displayName = "David Chen",
            username = "davidc",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
            bio = "Ambient soundscapes & deep bass seeker.",
            favoriteGenres = listOf("Ambient", "Techno", "Deep House"),
            favoriteArtists = listOf("Brian Eno", "Jon Hopkins"),
            publicPlaylistsCount = 4,
            followersCount = 150,
            followingCount = 95,
            isFollowing = false,
            isConnected = false
        )
    )

    fun getUserProfile(userId: String): UserSocialProfile? {
        if (userId == "user_me" || userId == "self") {
            return UserSocialProfile(
                id = "user_me",
                displayName = "You",
                username = "you_music_fan",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                bio = "Free Music Streamer & Audiophile 🎧 Exploring glassmorphic soundscapes.",
                favoriteGenres = listOf("Synthwave", "Lofi", "Electronic", "Ambient"),
                favoriteArtists = listOf("Synthwave King", "Retro Dreamer"),
                publicPlaylistsCount = 3,
                followersCount = 28,
                followingCount = 45,
                isFollowing = false,
                isConnected = true,
                privacySettings = ProfileVisibilityManager.privacySettings.value
            )
        }
        val profile = userProfilesMap[userId]
        if (profile != null) {
            val isFollowing = FollowManager.isFollowingUser(userId)
            return profile.copy(isFollowing = isFollowing)
        }
        return null
    }

    fun getFriendsAreListeningTo(): List<SocialActivityItem> {
        return ActivityFeedRepository.activities.value.filter {
            it.activityType == SocialActivityType.LISTENED_SONG
        }.take(5)
    }

    fun getTrendingAmongConnections(): List<SocialActivityItem> {
        return ActivityFeedRepository.activities.value.take(6)
    }
}
