package com.example.core.search

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity

data class LocalDeviceTrack(
    val id: String,
    val title: String,
    val artistName: String,
    val albumTitle: String,
    val durationMs: Long,
    val fileName: String,
    val filePath: String,
    val format: String = "FLAC",
    val fileSizeMb: Float = 24.5f
)

data class OnlinePlaylist(
    val id: String,
    val name: String,
    val curator: String,
    val coverUrl: String,
    val trackCount: Int,
    val description: String,
    val moodCategory: String
)

data class MoodCategory(
    val id: String,
    val name: String,
    val description: String,
    val coverUrl: String
)

enum class SearchCategory(val label: String) {
    ALL("All"),
    SONGS("Songs"),
    ARTISTS("Artists"),
    ALBUMS("Albums"),
    PLAYLISTS("Playlists"),
    GENRES("Genres"),
    MOODS("Moods"),
    ON_DEVICE("📱 On This Device")
}

enum class SortByOption(val label: String) {
    RELEVANCE("Relevance"),
    POPULARITY("Popularity"),
    RELEASE_DATE("Release Date"),
    TITLE("Title (A-Z)")
}

data class SearchFilterOptions(
    val category: SearchCategory = SearchCategory.ALL,
    val sortBy: SortByOption = SortByOption.RELEVANCE,
    val durationFilter: String = "All", // "All", "< 3 min", "3-5 min", "> 5 min"
    val audioQualityFilter: String = "All", // "All", "Lossless FLAC", "Hi-Res Masters"
    val genre: String? = null,
    val mood: String? = null
)

data class SearchResultsGroup(
    val topResult: Any? = null,
    val songs: List<SongEntity> = emptyList(),
    val artists: List<ArtistEntity> = emptyList(),
    val albums: List<AlbumEntity> = emptyList(),
    val playlists: List<OnlinePlaylist> = emptyList(),
    val localDeviceTracks: List<LocalDeviceTrack> = emptyList(),
    val autocorrectedQuery: String? = null,
    val suggestions: List<String> = emptyList(),
    val totalResultCount: Int = 0
)
