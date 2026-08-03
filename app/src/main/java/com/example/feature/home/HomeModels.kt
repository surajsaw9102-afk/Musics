package com.example.feature.home

import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity

enum class HomeSectionType {
    SPEED_DIAL,
    CONTINUE_LISTENING,
    BECAUSE_YOU_LISTENED_TO,
    YOUR_CHOICES,
    MORE_LIKE_THIS,
    RECOMMENDED_ARTISTS,
    RECOMMENDED_ALBUMS,
    MOOD_PICKS,
    GENRE_PICKS,
    TRENDING_NOW,
    DISCOVER_NEW,
    FAVORITES
}

data class HomeSection(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val rationaleBadge: String? = null,
    val type: HomeSectionType,
    val priorityScore: Float = 0.5f,
    val items: List<Any> = emptyList()
)

data class MoodPickItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val coverUrl: String,
    val targetGenreOrMood: String
)

data class HomeFeedData(
    val sections: List<HomeSection> = emptyList(),
    val activeCategoryFilter: String = "All",
    val isLoading: Boolean = false,
    val isFreshUser: Boolean = false,
    val topVibeSummary: String = "85% Synthwave • 65% Ambient",
    val timeOfDayGreeting: String = "Good Evening",
    val timeOfDaySubtext: String = "Late Night Cyber Drive • 24-bit FLAC"
)
