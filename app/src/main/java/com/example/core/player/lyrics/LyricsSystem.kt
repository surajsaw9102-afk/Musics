package com.example.core.player.lyrics

import com.example.core.database.entities.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

sealed class LyricsData {
    data class Synced(val lines: List<LyricLine>) : LyricsData()
    data class Plain(val text: String) : LyricsData()
    object Instrumental : LyricsData()
    object None : LyricsData()
    object Loading : LyricsData()
}

/**
 * Modular Provider Interface for lyrics fetching.
 * Future lyrics sources (Genius, Musixmatch, Spotify, LrcLib) can implement this interface
 * without modifying UI or Player logic.
 */
interface LyricsProvider {
    val providerName: String
    suspend fun getLyrics(songId: String, title: String, artist: String): LyricsData?
}

/**
 * Built-in provider supplying time-synced lyrics for catalog songs.
 */
class LocalCatalogLyricsProvider : LyricsProvider {
    override val providerName: String = "Aura Local Catalog Lyrics"

    override suspend fun getLyrics(songId: String, title: String, artist: String): LyricsData? {
        return withContext(Dispatchers.Default) {
            when (songId) {
                "song_101" -> LyricsData.Synced(
                    listOf(
                        LyricLine(0L, "♪ (Synth Intro building up) ♪"),
                        LyricLine(8000L, "City lights flickers in the neon glow"),
                        LyricLine(14000L, "Midnight driving, nowhere left to go"),
                        LyricLine(20000L, "Cruising down the avenue, pulse in sync"),
                        LyricLine(26000L, "Faster than the thoughts that I can think"),
                        LyricLine(32000L, "Midnight city... shines so bright"),
                        LyricLine(38000L, "Lost inside the electric night"),
                        LyricLine(45000L, "Bassline rolling through the summer breeze"),
                        LyricLine(52000L, "Melodies floating through the canopy trees"),
                        LyricLine(60000L, "♪ (Saxophone & Synth Solo) ♪"),
                        LyricLine(78000L, "When the dark settles on the street"),
                        LyricLine(85000L, "We find freedom in the synthwave beat"),
                        LyricLine(92000L, "Midnight city... shines so bright"),
                        LyricLine(100000L, "Lost inside the electric night"),
                        LyricLine(120000L, "♪ (Fading Out with Echoes) ♪")
                    )
                )

                "song_102" -> LyricsData.Synced(
                    listOf(
                        LyricLine(0L, "♪ (Celestial Ambient Chimes) ♪"),
                        LyricLine(10000L, "Stars alignment across the deep blue sky"),
                        LyricLine(18000L, "Watching galaxies quietly drift by"),
                        LyricLine(25000L, "Waves of harmony, weightless and free"),
                        LyricLine(33000L, "Floating through endless eternity"),
                        LyricLine(42000L, "Celestial waves... carry me home"),
                        LyricLine(50000L, "Through cosmic realms where spirits roam"),
                        LyricLine(65000L, "♪ (Atmospheric Guitar Sweep) ♪"),
                        LyricLine(80000L, "Celestial waves... carry me home"),
                        LyricLine(95000L, "In eternal peace... we are whole")
                    )
                )

                "song_103" -> LyricsData.Synced(
                    listOf(
                        LyricLine(0L, "♪ (Pulsing 80s Arpeggio) ♪"),
                        LyricLine(6000L, "Neon shadows on the rain-slick ground"),
                        LyricLine(12000L, "Frequency rising, turning all around"),
                        LyricLine(18000L, "Electric heartbeats, digital dream"),
                        LyricLine(24000L, "Nothing is ever quite what it seems"),
                        LyricLine(30000L, "Live in neon dreams!"),
                        LyricLine(36000L, "Where the synthwave gleams!"),
                        LyricLine(48000L, "♪ (Drum Break & Solo) ♪"),
                        LyricLine(60000L, "Neon dreams... taking control!")
                    )
                )

                "song_104" -> LyricsData.Synced(
                    listOf(
                        LyricLine(0L, "♪ (Velvet Piano Chords) ♪"),
                        LyricLine(8000L, "Sunset fading into purple twilight"),
                        LyricLine(15000L, "Whispers softly lingering in the night"),
                        LyricLine(22000L, "Smooth velvet breeze across the shore"),
                        LyricLine(30000L, "Could we ever ask for anything more?"),
                        LyricLine(40000L, "Velvet horizon... stay with me"),
                        LyricLine(50000L, "By the quiet, calm glass sea")
                    )
                )

                "song_107" -> LyricsData.Instrumental

                else -> LyricsData.Synced(
                    listOf(
                        LyricLine(0L, "♪ (Intro Instrumental) ♪"),
                        LyricLine(6000L, "Rhythm pulsing through the atmosphere"),
                        LyricLine(12000L, "Clear acoustic sounds, crystal and clear"),
                        LyricLine(18000L, "Lost in the audio frequency high"),
                        LyricLine(25000L, "Sailing under the open sky"),
                        LyricLine(32000L, "Feel the music take you away"),
                        LyricLine(40000L, "Live for the sound of today"),
                        LyricLine(50000L, "♪ (Outro Lead Solo) ♪")
                    )
                )
            }
        }
    }
}

/**
 * Singleton repository coordinating lyrics fetching from registered providers.
 */
object LyricsRepository {
    private val providers = mutableListOf<LyricsProvider>(
        LocalCatalogLyricsProvider()
    )

    private val lyricsCache = mutableMapOf<String, LyricsData>()

    private val _currentLyricsState = MutableStateFlow<LyricsData>(LyricsData.None)
    val currentLyricsState: StateFlow<LyricsData> = _currentLyricsState.asStateFlow()

    fun registerProvider(provider: LyricsProvider) {
        if (!providers.contains(provider)) {
            providers.add(0, provider) // High priority
        }
    }

    suspend fun loadLyricsForSong(song: SongEntity?) {
        if (song == null) {
            _currentLyricsState.value = LyricsData.None
            return
        }

        lyricsCache[song.id]?.let { cached ->
            _currentLyricsState.value = cached
            return
        }

        _currentLyricsState.value = LyricsData.Loading

        for (provider in providers) {
            val result = provider.getLyrics(song.id, song.title, song.artistName)
            if (result != null && result !is LyricsData.None) {
                lyricsCache[song.id] = result
                _currentLyricsState.value = result
                return
            }
        }

        _currentLyricsState.value = LyricsData.None
    }
}
