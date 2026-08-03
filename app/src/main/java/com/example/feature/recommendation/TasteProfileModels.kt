package com.example.feature.recommendation

import com.example.core.database.entities.SongEntity

enum class SignalType {
    PLAY,
    SKIP,
    REPEAT,
    LIKE,
    UNLIKE,
    SEARCH
}

enum class TimeOfDay(val displayName: String, val greeting: String, val defaultMood: String) {
    MORNING("Morning", "Good Morning", "Morning Focus & Chill"),
    AFTERNOON("Afternoon", "Good Afternoon", "Midday Flow & Energy"),
    EVENING("Evening", "Good Evening", "Evening Chill & Synth"),
    LATE_NIGHT("Late Night", "Late Night Vibe", "Late Night Cyber Drive")
}

data class UserSignal(
    val type: SignalType,
    val songId: String? = null,
    val artistId: String? = null,
    val genre: String? = null,
    val searchKeyword: String? = null,
    val playedDurationMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)

data class UserTasteProfile(
    val totalPlays: Int = 14,
    val totalSkips: Int = 2,
    val totalLikes: Int = 5,
    val lastPlayedSong: SongEntity? = null,
    val playHistory: List<SongEntity> = emptyList(),
    val playCountMap: Map<String, Int> = mapOf("song_101" to 5, "song_102" to 3, "song_103" to 4),
    val skippedSongIds: Set<String> = setOf(),
    val likedSongIds: Set<String> = setOf("song_101", "song_103", "song_105"),
    val topGenres: Map<String, Float> = mapOf(
        "Synthwave" to 0.85f,
        "Cyberpunk" to 0.70f,
        "Ambient" to 0.55f,
        "Electro House" to 0.40f
    ),
    val topArtistIds: Map<String, Float> = mapOf(
        "art_101" to 0.90f, // Lumina Eclipse
        "art_102" to 0.75f  // Neon Nexus
    ),
    val searchHistory: List<String> = listOf("Lumina Eclipse", "CyberPulse", "Night Drive"),
    val currentTimeOfDay: TimeOfDay = TimeOfDay.EVENING
)
