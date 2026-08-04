package com.example.feature.social

import com.example.core.catalog.MusicCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object ActivityFeedRepository {

    private val initialActivities = listOf(
        SocialActivityItem(
            id = "act_1",
            userId = "user_alex",
            userName = "Alex Mercer",
            userHandle = "@alexm",
            userAvatar = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=300",
            activityType = SocialActivityType.LISTENED_SONG,
            timestampMs = System.currentTimeMillis() - 180000, // 3 mins ago
            targetTitle = "Stellar Drift",
            targetSubtitle = "Synthwave King",
            targetImageUrl = MusicCatalog.ALL_SONGS.firstOrNull()?.coverUrl,
            targetId = "s1",
            song = MusicCatalog.ALL_SONGS.firstOrNull(),
            note = "Repeat mode engaged 🚀"
        ),
        SocialActivityItem(
            id = "act_2",
            userId = "user_sara",
            userName = "Sara Connor",
            userHandle = "@sarac",
            userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300",
            activityType = SocialActivityType.CREATED_PLAYLIST,
            timestampMs = System.currentTimeMillis() - 14400000, // 4 hours ago
            targetTitle = "Cyberpunk Night Drive 🚗⚡",
            targetSubtitle = "Collaborative Playlist • 16 tracks",
            targetImageUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
            targetId = "pl_collab_synth"
        ),
        SocialActivityItem(
            id = "act_3",
            userId = "user_david",
            userName = "David Chen",
            userHandle = "@davidc",
            userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
            activityType = SocialActivityType.LIKED_SONG,
            timestampMs = System.currentTimeMillis() - 28800000, // 8 hours ago
            targetTitle = "Neon Horizon",
            targetSubtitle = "Cyber Vibe",
            targetImageUrl = MusicCatalog.ALL_SONGS.getOrNull(1)?.coverUrl,
            targetId = "s2",
            song = MusicCatalog.ALL_SONGS.getOrNull(1)
        ),
        SocialActivityItem(
            id = "act_4",
            userId = "user_sara",
            userName = "Sara Connor",
            userHandle = "@sarac",
            userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300",
            activityType = SocialActivityType.FOLLOWED_ARTIST,
            timestampMs = System.currentTimeMillis() - 86400000, // 1 day ago
            targetTitle = "Retro Dreamer",
            targetSubtitle = "Electronic & Chillwave",
            targetImageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
            targetId = "a2"
        ),
        SocialActivityItem(
            id = "act_5",
            userId = "user_alex",
            userName = "Alex Mercer",
            userHandle = "@alexm",
            userAvatar = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=300",
            activityType = SocialActivityType.UPDATED_SHARED_PLAYLIST,
            timestampMs = System.currentTimeMillis() - 172800000, // 2 days ago
            targetTitle = "Added 3 tracks to Cyberpunk Night Drive",
            targetSubtitle = "Collaborative Playlist",
            targetImageUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
            targetId = "pl_collab_synth"
        )
    )

    private val _activities = MutableStateFlow<List<SocialActivityItem>>(initialActivities)
    val activities: StateFlow<List<SocialActivityItem>> = _activities.asStateFlow()

    fun recordListeningActivity(title: String, artist: String, coverUrl: String, songId: String) {
        if (!ProfileVisibilityManager.privacySettings.value.showListeningActivity) return

        val newActivity = SocialActivityItem(
            id = UUID.randomUUID().toString(),
            userId = "user_me",
            userName = "You",
            userHandle = "@you",
            userAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
            activityType = SocialActivityType.LISTENED_SONG,
            timestampMs = System.currentTimeMillis(),
            targetTitle = title,
            targetSubtitle = artist,
            targetImageUrl = coverUrl,
            targetId = songId,
            song = MusicCatalog.ALL_SONGS.find { it.id == songId }
        )
        _activities.value = listOf(newActivity) + _activities.value
    }

    fun recordLikedSongActivity(title: String, artist: String, coverUrl: String, songId: String) {
        val newActivity = SocialActivityItem(
            id = UUID.randomUUID().toString(),
            userId = "user_me",
            userName = "You",
            userHandle = "@you",
            userAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
            activityType = SocialActivityType.LIKED_SONG,
            timestampMs = System.currentTimeMillis(),
            targetTitle = title,
            targetSubtitle = artist,
            targetImageUrl = coverUrl,
            targetId = songId,
            song = MusicCatalog.ALL_SONGS.find { it.id == songId }
        )
        _activities.value = listOf(newActivity) + _activities.value
    }
}
