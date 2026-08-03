package com.example.core.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.entities.SongEntity
import com.example.core.player.AuraAudioPlayerManager
import com.example.core.player.PlayerStateData
import com.example.core.player.RepeatModeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

import com.example.core.player.AudioSettingsManager
import com.example.core.player.AudioSettingsState
import com.example.core.player.PlaybackSpeedManager
import com.example.core.player.SleepTimerManager
import com.example.core.player.SleepTimerOption
import com.example.core.player.lyrics.LyricsData
import com.example.core.player.lyrics.LyricsRepository

data class PlayerData(
    val currentSong: SongEntity? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val isLiked: Boolean = true,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatModeState = RepeatModeState.OFF,
    val queue: List<SongEntity> = emptyList(),
    val currentIndex: Int = -1,
    val errorMessage: String? = null,
    val audioQuality: String = "Lossless FLAC 24-bit / 96kHz",
    val volume: Float = 1.0f,
    val playbackSpeed: Float = 1.0f,
    val lyrics: LyricsData = LyricsData.None,
    val sleepTimerOption: SleepTimerOption = SleepTimerOption.OFF,
    val sleepTimerRemainingSeconds: Long? = null,
    val audioSettings: AudioSettingsState = AudioSettingsState()
)

class PlayerState : ViewModel() {

    private val _likedSongs = MutableStateFlow<Set<String>>(
        setOf("song_101", "song_103") // Default favorites
    )

    val playerData: StateFlow<PlayerData> = combine(
        AuraAudioPlayerManager.state,
        _likedSongs,
        LyricsRepository.currentLyricsState,
        PlaybackSpeedManager.currentSpeed,
        SleepTimerManager.activeOption,
        SleepTimerManager.remainingSeconds
    ) { flows: Array<Any?> ->
        val managerState = flows[0] as PlayerStateData
        @Suppress("UNCHECKED_CAST")
        val likedSet = flows[1] as Set<String>
        val lyrics = flows[2] as LyricsData
        val speed = flows[3] as Float
        val timerOpt = flows[4] as SleepTimerOption
        val timerRemaining = flows[5] as Long?

        val currentId = managerState.currentSong?.id
        PlayerData(
            currentSong = managerState.currentSong,
            isPlaying = managerState.isPlaying,
            isBuffering = managerState.isBuffering,
            progressMs = managerState.progressMs,
            durationMs = managerState.durationMs,
            bufferedMs = managerState.bufferedMs,
            isLiked = currentId != null && likedSet.contains(currentId),
            isShuffle = managerState.isShuffle,
            repeatMode = managerState.repeatMode,
            queue = managerState.queue,
            currentIndex = managerState.currentIndex,
            errorMessage = managerState.errorMessage,
            audioQuality = managerState.audioQuality,
            volume = managerState.volume,
            playbackSpeed = speed,
            lyrics = lyrics,
            sleepTimerOption = timerOpt,
            sleepTimerRemainingSeconds = timerRemaining,
            audioSettings = AudioSettingsManager.settings.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerData()
    )

    fun playSong(song: SongEntity, newQueue: List<SongEntity>? = null) {
        com.example.core.cache.SmartCacheManager.recordSongPlay(song)
        AuraAudioPlayerManager.playSong(song, newQueue)
    }

    fun togglePlayPause() {
        AuraAudioPlayerManager.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        AuraAudioPlayerManager.seekTo(positionMs)
    }

    fun fastForward(deltaMs: Long = 10000L) {
        AuraAudioPlayerManager.fastForward(deltaMs)
    }

    fun rewind(deltaMs: Long = 10000L) {
        AuraAudioPlayerManager.rewind(deltaMs)
    }

    fun skipNext() {
        AuraAudioPlayerManager.skipNext()
    }

    fun skipPrevious() {
        AuraAudioPlayerManager.skipPrevious()
    }

    fun toggleShuffle() {
        AuraAudioPlayerManager.toggleShuffle()
    }

    fun toggleRepeat() {
        AuraAudioPlayerManager.toggleRepeat()
    }

    fun toggleLike() {
        val currentSongId = playerData.value.currentSong?.id ?: return
        val currentSet = _likedSongs.value.toMutableSet()
        if (currentSet.contains(currentSongId)) {
            currentSet.remove(currentSongId)
        } else {
            currentSet.add(currentSongId)
        }
        _likedSongs.value = currentSet
    }

    fun addToQueue(song: SongEntity) {
        AuraAudioPlayerManager.addToQueue(song)
    }

    fun addNext(song: SongEntity) {
        AuraAudioPlayerManager.addNext(song)
    }

    fun removeFromQueue(index: Int) {
        AuraAudioPlayerManager.removeFromQueue(index)
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        AuraAudioPlayerManager.reorderQueue(fromIndex, toIndex)
    }

    fun clearQueue() {
        AuraAudioPlayerManager.clearQueue()
    }

    fun retryPlayback() {
        AuraAudioPlayerManager.retryPlayback()
    }

    fun setVolume(volume: Float) {
        AuraAudioPlayerManager.setVolume(volume)
    }

    fun playQueueIndex(index: Int) {
        AuraAudioPlayerManager.playQueueIndex(index)
    }

    fun setPlaybackSpeed(speed: Float) {
        PlaybackSpeedManager.setSpeed(speed)
    }

    fun startSleepTimer(option: SleepTimerOption) {
        SleepTimerManager.startTimer(option) {
            AuraAudioPlayerManager.togglePlayPause()
        }
    }

    fun cancelSleepTimer() {
        SleepTimerManager.cancelTimer()
    }

    fun updateCrossfade(seconds: Int) {
        AudioSettingsManager.setCrossfade(seconds)
    }

    fun toggleMonoAudio(enabled: Boolean) {
        AudioSettingsManager.setMonoAudio(enabled)
    }

    fun toggleGapless(enabled: Boolean) {
        AudioSettingsManager.setGapless(enabled)
    }

    fun togglePauseOnHeadsetDisconnect(enabled: Boolean) {
        AudioSettingsManager.setPauseOnHeadsetDisconnect(enabled)
    }

    fun toggleResumeOnHeadsetConnect(enabled: Boolean) {
        AudioSettingsManager.setResumeOnHeadsetConnect(enabled)
    }

    fun saveQueueAsPlaylist(playlistTitle: String) {
        // Mock save queue to user playlists
    }
}
