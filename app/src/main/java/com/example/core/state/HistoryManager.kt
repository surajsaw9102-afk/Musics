package com.example.core.state

import com.example.core.database.entities.SongEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayHistoryItem(
    val id: String,
    val song: SongEntity,
    val playedAtMs: Long = System.currentTimeMillis()
)

data class SearchHistoryItem(
    val id: String,
    val query: String,
    val searchedAtMs: Long = System.currentTimeMillis()
)

object HistoryManager {

    private val _playHistory = MutableStateFlow<List<PlayHistoryItem>>(
        listOf(
            PlayHistoryItem(
                id = "hist_1",
                song = SongEntity(
                    id = "sng_01",
                    title = "Blinding Lights (Cyber Version)",
                    artistId = "art_01",
                    artistName = "Aura Synthetics",
                    albumId = "alb_01",
                    albumTitle = "Neon Horizon",
                    durationMs = 200000,
                    coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500",
                    audioQuality = "Lossless FLAC 24-bit"
                )
            ),
            PlayHistoryItem(
                id = "hist_2",
                song = SongEntity(
                    id = "sng_02",
                    title = "Midnight Odyssey",
                    artistId = "art_02",
                    artistName = "Starlight Protocol",
                    albumId = "alb_02",
                    albumTitle = "Orbital Dreams",
                    durationMs = 240000,
                    coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500",
                    audioQuality = "Lossless FLAC 24-bit"
                )
            ),
            PlayHistoryItem(
                id = "hist_3",
                song = SongEntity(
                    id = "sng_03",
                    title = "Quantum Pulse",
                    artistId = "art_03",
                    artistName = "Lofi Spheres",
                    albumId = "alb_03",
                    albumTitle = "Chilled Frequencies",
                    durationMs = 180000,
                    coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500",
                    audioQuality = "High Quality 320kbps"
                )
            )
        )
    )
    val playHistory: StateFlow<List<PlayHistoryItem>> = _playHistory.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<SearchHistoryItem>>(
        listOf(
            SearchHistoryItem("sch_1", "Synthwave 80s"),
            SearchHistoryItem("sch_2", "Lofi Girl Study"),
            SearchHistoryItem("sch_3", "Aura Synthetics"),
            SearchHistoryItem("sch_4", "Cyberpunk 2077 OST"),
            SearchHistoryItem("sch_5", "Deep Chill Ambient")
        )
    )
    val searchHistory: StateFlow<List<SearchHistoryItem>> = _searchHistory.asStateFlow()

    fun addPlayHistory(song: SongEntity) {
        val newItem = PlayHistoryItem(
            id = "hist_${System.currentTimeMillis()}",
            song = song,
            playedAtMs = System.currentTimeMillis()
        )
        _playHistory.value = listOf(newItem) + _playHistory.value.filter { it.song.id != song.id }
    }

    fun removePlayHistoryItem(id: String) {
        _playHistory.value = _playHistory.value.filter { it.id != id }
    }

    fun clearPlayHistory() {
        _playHistory.value = emptyList()
    }

    fun addSearchQuery(query: String) {
        if (query.isBlank()) return
        val newItem = SearchHistoryItem(
            id = "sch_${System.currentTimeMillis()}",
            query = query.trim(),
            searchedAtMs = System.currentTimeMillis()
        )
        _searchHistory.value = listOf(newItem) + _searchHistory.value.filter { !it.query.equals(query, ignoreCase = true) }
    }

    fun removeSearchQuery(id: String) {
        _searchHistory.value = _searchHistory.value.filter { it.id != id }
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
    }
}
