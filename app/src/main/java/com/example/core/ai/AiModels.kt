package com.example.core.ai

import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity

enum class MoodType(val displayName: String, val emoji: String, val description: String) {
    HAPPY("Happy", "😊", "Upbeat and uplifting rhythms"),
    SAD("Melancholic", "🌧️", "Deep emotional and introspective tunes"),
    CALM("Calm", "🧘", "Peaceful ambient and soothing acoustics"),
    ROMANTIC("Romantic", "💖", "Warm acoustic and soul melodies"),
    ENERGETIC("Energetic", "⚡", "High tempo synth and dance tracks"),
    WORKOUT("Workout", "🏋️", "Driving basslines for peak performance"),
    STUDY("Study Focus", "🎧", "Lo-fi beats and non-distracting soundscapes"),
    NIGHT_DRIVE("Night Drive", "🌙", "Atmospheric synthwave and deep bass"),
    RAINY("Rainy Day", "☔", "Cozy acoustic, lofi, and ambient rain beats"),
    MEDITATION("Meditation", "🌌", "Zero-gravity drone and binaural tones"),
    PARTY("Party", "🎉", "Crowd-pleasing electronic dance grooves"),
    FOCUS("Deep Focus", "🎯", "Minimalist instrumental flow states"),
    SLEEP("Sleep", "💤", "Drifting lulls and soft ambient waves"),
    TRAVEL("Travel", "✈️", "Expansive soundscapes for long journeys")
}

enum class SmartQueueMode(val title: String, val description: String) {
    SMART_SIMILAR("Smart Similar", "Continuously plays songs matching the vibe of the current track"),
    MOOD_FLOW("Mood Flow", "Adapts transition between songs based on your current emotional context"),
    ENERGY_FLOW("Energy Flow", "Gradually builds or dials down energy levels for smooth listening"),
    ARTIST_FLOW("Artist Flow", "Mixes top tracks from artists with a similar sound profile"),
    DISCOVERY("Discovery Mode", "Unearths hidden gems and lesser-known tracks tuned to your taste"),
    COMFORT("Comfort Mode", "Sticks close to your most replayed and favorite comfort songs"),
    SURPRISE("Surprise Mode", "Eclectic curated wildcards across genres you enjoy")
}

enum class IntentType {
    PLAY_SONGS,
    CREATE_PLAYLIST,
    SEARCH_FILTER,
    CHANGE_MOOD,
    GET_INSIGHTS,
    START_DJ,
    EXPLAIN_TRACK,
    NAVIGATE,
    GENERAL_QNA
}

data class SearchFilterResult(
    val rawQuery: String,
    val matchedSongs: List<SongEntity> = emptyList(),
    val matchedArtists: List<ArtistEntity> = emptyList(),
    val matchedAlbums: List<AlbumEntity> = emptyList(),
    val detectedMood: MoodType? = null,
    val detectedGenre: String? = null,
    val detectedTempo: String? = null, // e.g. "Fast", "Mid-tempo", "Slow"
    val detectedLanguage: String? = null, // e.g. "English", "Hindi", "Instrumental"
    val detectedEnergy: String? = null // e.g. "High", "Chill"
)

data class GeneratedPlaylistResult(
    val id: String = "ai_pl_${System.currentTimeMillis()}",
    val title: String,
    val description: String,
    val coverUrl: String,
    val mood: MoodType?,
    val tracks: List<SongEntity>,
    val suggestedThemeHex: String = "#00E5FF"
)

data class RecommendationSection(
    val id: String,
    val title: String,
    val subtitle: String,
    val tracks: List<SongEntity>,
    val categoryType: String
)

data class InsightCard(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconName: String,
    val accentHex: String = "#7C4DFF"
)

data class MusicInsights(
    val topArtists: List<ArtistEntity>,
    val topGenres: List<Pair<String, Float>>,
    val peakListeningTime: String,
    val totalMinutesListened: Int,
    val skipRatePercent: Int,
    val repeatRatePercent: Int,
    val discoveryScore: Int,
    val personalitySummary: String,
    val personalityTitle: String,
    val insightCards: List<InsightCard>
)

data class DjHostSpeech(
    val greeting: String,
    val trackComment: String,
    val moodTransitionText: String,
    val nextTrackRecommendation: String
)

data class AiActionChip(
    val label: String,
    val actionQuery: String,
    val iconName: String = "AUTO_AWESOME"
)

data class AiChatMessage(
    val id: String = "msg_${System.currentTimeMillis()}_${(1000..9999).random()}",
    val sender: String, // "USER" | "ASSISTANT" | "DJ"
    val text: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val tracks: List<SongEntity> = emptyList(),
    val generatedPlaylist: GeneratedPlaylistResult? = null,
    val actionChips: List<AiActionChip> = emptyList(),
    val intent: IntentType = IntentType.GENERAL_QNA
)

data class AiContext(
    val currentSong: SongEntity? = null,
    val currentMood: MoodType? = null,
    val recentHistory: List<SongEntity> = emptyList(),
    val likedSongs: List<SongEntity> = emptyList(),
    val activeRoute: String = "HOME"
)
