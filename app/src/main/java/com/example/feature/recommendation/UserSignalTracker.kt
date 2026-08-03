package com.example.feature.recommendation

import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.SongEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

object UserSignalTracker {

    private val initialSongs = MusicCatalog.ALL_SONGS

    private val _tasteProfile = MutableStateFlow(
        UserTasteProfile(
            totalPlays = 12,
            totalSkips = 1,
            totalLikes = 3,
            lastPlayedSong = initialSongs.firstOrNull(),
            playHistory = initialSongs.take(4),
            playCountMap = mapOf("song_101" to 6, "song_102" to 4, "song_103" to 3, "song_104" to 2),
            skippedSongIds = emptySet(),
            likedSongIds = setOf("song_101", "song_103"),
            topGenres = mapOf("Synthwave" to 0.85f, "Ambient" to 0.65f, "Cyberpunk" to 0.50f),
            topArtistIds = mapOf("art_101" to 0.90f, "art_102" to 0.70f),
            currentTimeOfDay = calculateCurrentTimeOfDay()
        )
    )

    val tasteProfile: StateFlow<UserTasteProfile> = _tasteProfile.asStateFlow()

    fun recordSongPlay(song: SongEntity, playedDurationMs: Long = 0L) {
        val current = _tasteProfile.value
        val updatedPlayCountMap = current.playCountMap.toMutableMap()
        val prevCount = updatedPlayCountMap[song.id] ?: 0
        updatedPlayCountMap[song.id] = prevCount + 1

        val updatedHistory = (listOf(song) + current.playHistory.filter { it.id != song.id }).take(15)

        // Boost genre weight
        val updatedGenres = current.topGenres.toMutableMap()
        val genreScore = updatedGenres[song.genre] ?: 0.2f
        updatedGenres[song.genre] = (genreScore + 0.15f).coerceAtMost(1.0f)

        // Boost artist weight
        val updatedArtists = current.topArtistIds.toMutableMap()
        val artistScore = updatedArtists[song.artistId] ?: 0.2f
        updatedArtists[song.artistId] = (artistScore + 0.15f).coerceAtMost(1.0f)

        _tasteProfile.value = current.copy(
            totalPlays = current.totalPlays + 1,
            lastPlayedSong = song,
            playHistory = updatedHistory,
            playCountMap = updatedPlayCountMap,
            topGenres = updatedGenres,
            topArtistIds = updatedArtists,
            currentTimeOfDay = calculateCurrentTimeOfDay()
        )
    }

    fun recordSongSkip(song: SongEntity) {
        val current = _tasteProfile.value
        val updatedSkipped = current.skippedSongIds.toMutableSet()
        updatedSkipped.add(song.id)

        // Slightly lower genre weight on skip
        val updatedGenres = current.topGenres.toMutableMap()
        val genreScore = updatedGenres[song.genre] ?: 0.5f
        updatedGenres[song.genre] = (genreScore - 0.1f).coerceAtLeast(0.05f)

        _tasteProfile.value = current.copy(
            totalSkips = current.totalSkips + 1,
            skippedSongIds = updatedSkipped,
            topGenres = updatedGenres
        )
    }

    fun recordSongLike(song: SongEntity, isLiked: Boolean) {
        val current = _tasteProfile.value
        val updatedLikes = current.likedSongIds.toMutableSet()
        if (isLiked) {
            updatedLikes.add(song.id)
        } else {
            updatedLikes.remove(song.id)
        }

        // Boost genre weight on like
        val updatedGenres = current.topGenres.toMutableMap()
        val genreScore = updatedGenres[song.genre] ?: 0.3f
        updatedGenres[song.genre] = if (isLiked) (genreScore + 0.25f).coerceAtMost(1.0f) else (genreScore - 0.15f).coerceAtLeast(0.05f)

        _tasteProfile.value = current.copy(
            totalLikes = updatedLikes.size,
            likedSongIds = updatedLikes,
            topGenres = updatedGenres
        )
    }

    fun recordSearch(query: String) {
        if (query.trim().isEmpty()) return
        val current = _tasteProfile.value
        val updatedHistory = (listOf(query.trim()) + current.searchHistory.filter { !it.equals(query.trim(), ignoreCase = true) }).take(10)

        _tasteProfile.value = current.copy(
            searchHistory = updatedHistory
        )
    }

    fun resetProfileForNewUser() {
        _tasteProfile.value = UserTasteProfile(
            totalPlays = 0,
            totalSkips = 0,
            totalLikes = 0,
            lastPlayedSong = null,
            playHistory = emptyList(),
            playCountMap = emptyMap(),
            skippedSongIds = emptySet(),
            likedSongIds = emptySet(),
            topGenres = emptyMap(),
            topArtistIds = emptyMap(),
            searchHistory = emptyList(),
            currentTimeOfDay = calculateCurrentTimeOfDay()
        )
    }

    private fun calculateCurrentTimeOfDay(): TimeOfDay {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> TimeOfDay.MORNING
            in 12..16 -> TimeOfDay.AFTERNOON
            in 17..22 -> TimeOfDay.EVENING
            else -> TimeOfDay.LATE_NIGHT
        }
    }
}
