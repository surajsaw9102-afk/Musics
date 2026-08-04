package com.example.core.ai

import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.SongEntity
import com.example.feature.library.LibraryRepository

object AiPlaylistGenerator {

    private val provider: AiProvider = GeminiAiProvider()

    suspend fun generateSmartPlaylist(
        prompt: String,
        mood: MoodType? = null,
        context: AiContext = AiContext(),
        catalog: List<SongEntity> = MusicCatalog.ALL_SONGS
    ): GeneratedPlaylistResult {
        return provider.generatePlaylist(prompt, mood, context, catalog)
    }

    fun savePlaylistToLibrary(playlist: GeneratedPlaylistResult) {
        LibraryRepository.createPlaylist(
            name = playlist.title,
            description = playlist.description,
            coverUrl = playlist.coverUrl,
            initialSongIds = playlist.tracks.map { it.id }
        )
    }
}
