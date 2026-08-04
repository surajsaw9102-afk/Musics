package com.example.core.ai

import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.SongEntity
import com.example.core.player.AuraAudioPlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object AiQueueManager {

    private val provider: AiProvider = GeminiAiProvider()
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _activeMode = MutableStateFlow(SmartQueueMode.SMART_SIMILAR)
    val activeMode: StateFlow<SmartQueueMode> = _activeMode.asStateFlow()

    fun setQueueMode(mode: SmartQueueMode) {
        _activeMode.value = mode
        refreshQueue()
    }

    fun refreshQueue() {
        scope.launch {
            val currSong = AuraAudioPlayerManager.state.value.currentSong
            val smartTracks = provider.getSmartQueue(currSong, _activeMode.value, MusicCatalog.ALL_SONGS)
            if (smartTracks.isNotEmpty()) {
                AuraAudioPlayerManager.updateUpcomingQueue(smartTracks)
            }
        }
    }
}
