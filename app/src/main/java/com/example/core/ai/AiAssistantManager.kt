package com.example.core.ai

import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.SongEntity
import com.example.core.player.AuraAudioPlayerManager
import com.example.feature.library.LibraryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AiAssistantUiState(
    val messages: List<AiChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isDjModeActive: Boolean = false,
    val activeMood: MoodType? = null,
    val activeQueueMode: SmartQueueMode = SmartQueueMode.SMART_SIMILAR,
    val djSpeech: DjHostSpeech? = null,
    val isVoiceListening: Boolean = false
)

object AiAssistantManager {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var provider: AiProvider = GeminiAiProvider()

    private val _uiState = MutableStateFlow(
        AiAssistantUiState(
            messages = listOf(
                AiChatMessage(
                    sender = "ASSISTANT",
                    text = "Welcome to Aura AI Music Companion! Ask me for mood playlists, natural music searches, or listening insights.",
                    actionChips = listOf(
                        AiActionChip("Play relaxing music", "play relaxing music"),
                        AiActionChip("Workout mix", "workout mix"),
                        AiActionChip("Road trip playlist", "make a road trip playlist"),
                        AiActionChip("What am I listening to most?", "what am I listening to most")
                    )
                )
            )
        )
    )
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    fun setAiProvider(newProvider: AiProvider) {
        provider = newProvider
    }

    fun sendPrompt(userPrompt: String) {
        if (userPrompt.isBlank()) return

        val userMsg = AiChatMessage(
            sender = "USER",
            text = userPrompt
        )

        val updatedMsgs = _uiState.value.messages + userMsg
        _uiState.value = _uiState.value.copy(
            messages = updatedMsgs,
            isLoading = true
        )

        scope.launch {
            val currState = AuraAudioPlayerManager.state.value
            val context = AiContext(
                currentSong = currState.currentSong,
                currentMood = _uiState.value.activeMood,
                recentHistory = LibraryRepository.state.value.likedSongs,
                likedSongs = LibraryRepository.state.value.likedSongs
            )

            val response = provider.generateAssistantResponse(userPrompt, context)

            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + response,
                isLoading = false
            )

            // Automatically trigger DJ host comment if DJ mode is enabled
            if (_uiState.value.isDjModeActive) {
                refreshDjSpeech()
            }
        }
    }

    fun handleChipAction(chip: AiActionChip) {
        sendPrompt(chip.actionQuery)
    }

    fun toggleDjMode() {
        val nextMode = !_uiState.value.isDjModeActive
        _uiState.value = _uiState.value.copy(isDjModeActive = nextMode)
        if (nextMode) {
            refreshDjSpeech()
        }
    }

    fun setMood(mood: MoodType?) {
        _uiState.value = _uiState.value.copy(activeMood = mood)
        AiMoodEngine.setMood(mood)
    }

    fun setQueueMode(mode: SmartQueueMode) {
        _uiState.value = _uiState.value.copy(activeQueueMode = mode)
        AiQueueManager.setQueueMode(mode)
    }

    fun startVoiceListening() {
        _uiState.value = _uiState.value.copy(isVoiceListening = true)
    }

    fun stopVoiceListeningAndProcess(simulatedText: String) {
        _uiState.value = _uiState.value.copy(isVoiceListening = false)
        sendPrompt(simulatedText)
    }

    fun playTracks(tracks: List<SongEntity>) {
        if (tracks.isNotEmpty()) {
            AuraAudioPlayerManager.playSong(tracks.first(), tracks)
        }
    }

    fun saveGeneratedPlaylist(playlist: GeneratedPlaylistResult) {
        LibraryRepository.createPlaylist(
            name = playlist.title,
            description = playlist.description,
            coverUrl = playlist.coverUrl,
            initialSongIds = playlist.tracks.map { it.id }
        )
    }

    private fun refreshDjSpeech() {
        scope.launch {
            val song = AuraAudioPlayerManager.state.value.currentSong
            val speech = provider.getDjHostSpeech(song, _uiState.value.activeMood)
            _uiState.value = _uiState.value.copy(djSpeech = speech)
        }
    }
}
