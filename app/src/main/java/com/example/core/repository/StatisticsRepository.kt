package com.example.core.repository

import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GenreListeningData(
    val genreName: String,
    val playCount: Int,
    val percentage: Float
)

data class DailyListeningData(
    val dayLabel: String, // e.g. "Mon", "Tue"
    val hours: Float
)

data class ListeningStatistics(
    val totalHours: Float = 184.5f,
    val totalMinutes: Long = 11070L,
    val songsPlayedCount: Int = 1420,
    val albumsPlayedCount: Int = 86,
    val playlistsCreatedCount: Int = 8,
    val activeListeningStreakDays: Int = 14,
    val peakListeningTime: String = "Night Owl (10 PM - 1 AM)",
    val topGenres: List<GenreListeningData> = listOf(
        GenreListeningData("Synthwave", 480, 0.35f),
        GenreListeningData("Cyberpunk", 320, 0.25f),
        GenreListeningData("Ambient & Lofi", 260, 0.20f),
        GenreListeningData("Electronic", 180, 0.12f),
        GenreListeningData("Indie Rock", 110, 0.08f)
    ),
    val weeklyActivity: List<DailyListeningData> = listOf(
        DailyListeningData("Mon", 3.2f),
        DailyListeningData("Tue", 4.5f),
        DailyListeningData("Wed", 2.8f),
        DailyListeningData("Thu", 5.1f),
        DailyListeningData("Fri", 6.8f),
        DailyListeningData("Sat", 7.4f),
        DailyListeningData("Sun", 4.2f)
    ),
    val topArtists: List<Pair<ArtistEntity, Int>> = listOf(
        Pair(ArtistEntity(id = "art_01", name = "Aura Synthetics", avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500", monthlyListeners = 2800000), 342),
        Pair(ArtistEntity(id = "art_02", name = "Starlight Protocol", avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500", monthlyListeners = 1950000), 284),
        Pair(ArtistEntity(id = "art_03", name = "Lofi Spheres", avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500", monthlyListeners = 1420000), 210)
    ),
    val topAlbums: List<Pair<AlbumEntity, Int>> = listOf(
        Pair(AlbumEntity(id = "alb_01", title = "Neon Horizon", artistId = "art_01", artistName = "Aura Synthetics", coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500", releaseYear = 2025), 180),
        Pair(AlbumEntity(id = "alb_02", title = "Orbital Dreams", artistId = "art_02", artistName = "Starlight Protocol", coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500", releaseYear = 2024), 145)
    ),
    val topSongs: List<Pair<SongEntity, Int>> = listOf(
        Pair(
            SongEntity(
                id = "sng_01",
                title = "Blinding Lights (Cyber Version)",
                artistId = "art_01",
                artistName = "Aura Synthetics",
                albumId = "alb_01",
                albumTitle = "Neon Horizon",
                durationMs = 200000,
                coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500"
            ), 89
        ),
        Pair(
            SongEntity(
                id = "sng_02",
                title = "Midnight Odyssey",
                artistId = "art_02",
                artistName = "Starlight Protocol",
                albumId = "alb_02",
                albumTitle = "Orbital Dreams",
                durationMs = 240000,
                coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500"
            ), 74
        )
    )
)

object StatisticsRepository {

    private val _stats = MutableStateFlow(ListeningStatistics())
    val stats: StateFlow<ListeningStatistics> = _stats.asStateFlow()

    fun recordSongPlayback(durationMs: Long) {
        val current = _stats.value
        val additionalHours = durationMs / 3600000f
        val newTotalHours = current.totalHours + additionalHours
        val newTotalMins = current.totalMinutes + (durationMs / 60000L)
        val newSongsCount = current.songsPlayedCount + 1

        _stats.value = current.copy(
            totalHours = (newTotalHours * 10f).toInt() / 10f,
            totalMinutes = newTotalMins,
            songsPlayedCount = newSongsCount
        )
    }

    fun resetStatistics() {
        _stats.value = ListeningStatistics()
    }
}
