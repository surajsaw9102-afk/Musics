package com.example.core.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AuraAccentColor(val displayName: String, val hexValue: Long) {
    NEON_CYAN("Neon Cyan", 0xFF00F0FF),
    ELECTRIC_PURPLE("Electric Purple", 0xFF7000FF),
    EMERALD_PULSE("Emerald Pulse", 0xFF00FFA3),
    AMBER_GLOW("Amber Glow", 0xFFFFB800),
    MAGENTA_FLARE("Magenta Flare", 0xFFFF007A),
    SOLAR_YELLOW("Solar Yellow", 0xFFFFE600)
}

enum class DiscoveryStyle(val title: String, val description: String) {
    BALANCED("Balanced", "Mix of familiar favorites and new releases"),
    SURPRISE_ME("Surprise Me", "Adventurous picks from hidden indie gems"),
    DEEP_CUTS("Deep Cuts", "Unreleased tracks, B-sides, and live versions"),
    CHILL_VIBE("Chill Vibe", "Smooth, relaxing, and ambient beats"),
    HIGH_ENERGY("High Energy", "Upbeat, fast-tempo, synthwave and rock")
}

enum class StartupScreen(val routeTitle: String) {
    HOME("Home Feed"),
    SEARCH("Search & Explore"),
    LIBRARY("Your Library"),
    INSIGHTS("Listening Insights"),
    PROFILE("User Profile")
}

enum class AudioOutputMode(val title: String, val description: String) {
    STEREO("Stereo 2.0", "Standard balanced spatial output"),
    SPATIAL_3D("3D Spatial Audio", "Immersive binaural soundstage"),
    MONO("Mono", "Single-channel unified mix")
}

enum class HistoryVisibility(val title: String) {
    PUBLIC("Public"),
    FOLLOWERS_ONLY("Followers Only"),
    PRIVATE("Private (Only You)")
}

data class PersonalizationPreferences(
    val favoriteGenres: List<String> = listOf("Synthwave", "Cyberpunk", "Lofi Beats", "Ambient", "Indie Rock"),
    val favoriteArtists: List<String> = listOf("The Weeknd", "Daft Punk", "Kavinsky", "Lorn", "Tycho"),
    val startupScreen: StartupScreen = StartupScreen.HOME,
    val discoveryStyle: DiscoveryStyle = DiscoveryStyle.BALANCED,
    val preferredMoods: List<String> = listOf("Focus", "Late Night", "Chill", "Workout"),
    val explicitContentAllowed: Boolean = true,
    val pinnedItemIds: Set<String> = setOf("art_01", "alb_01", "sng_01")
)

object PreferencesManager {

    private val _personalization = MutableStateFlow(PersonalizationPreferences())
    val personalization: StateFlow<PersonalizationPreferences> = _personalization.asStateFlow()

    fun updateFavoriteGenres(genres: List<String>) {
        _personalization.value = _personalization.value.copy(favoriteGenres = genres)
    }

    fun updateFavoriteArtists(artists: List<String>) {
        _personalization.value = _personalization.value.copy(favoriteArtists = artists)
    }

    fun setStartupScreen(screen: StartupScreen) {
        _personalization.value = _personalization.value.copy(startupScreen = screen)
    }

    fun setDiscoveryStyle(style: DiscoveryStyle) {
        _personalization.value = _personalization.value.copy(discoveryStyle = style)
    }

    fun toggleExplicitContent(allowed: Boolean) {
        _personalization.value = _personalization.value.copy(explicitContentAllowed = allowed)
    }

    fun togglePinItem(itemId: String) {
        val current = _personalization.value.pinnedItemIds.toMutableSet()
        if (itemId in current) {
            current.remove(itemId)
        } else {
            current.add(itemId)
        }
        _personalization.value = _personalization.value.copy(pinnedItemIds = current)
    }

    fun isItemPinned(itemId: String): Boolean {
        return itemId in _personalization.value.pinnedItemIds
    }
}
