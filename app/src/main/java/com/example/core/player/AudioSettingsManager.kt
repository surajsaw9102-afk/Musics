package com.example.core.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioSettingsState(
    val crossfadeSeconds: Int = 3,
    val gaplessPlayback: Boolean = true,
    val monoAudio: Boolean = false,
    val pauseOnHeadsetDisconnect: Boolean = true,
    val resumeOnHeadsetConnect: Boolean = false,
    val audioNormalization: Boolean = true,
    val targetLoudnessDb: Int = -14
)

object AudioSettingsManager {
    private val _settings = MutableStateFlow(AudioSettingsState())
    val settings: StateFlow<AudioSettingsState> = _settings.asStateFlow()

    fun setCrossfade(seconds: Int) {
        _settings.value = _settings.value.copy(crossfadeSeconds = seconds.coerceIn(0, 12))
    }

    fun setGapless(enabled: Boolean) {
        _settings.value = _settings.value.copy(gaplessPlayback = enabled)
    }

    fun setMonoAudio(enabled: Boolean) {
        _settings.value = _settings.value.copy(monoAudio = enabled)
    }

    fun setPauseOnHeadsetDisconnect(enabled: Boolean) {
        _settings.value = _settings.value.copy(pauseOnHeadsetDisconnect = enabled)
    }

    fun setResumeOnHeadsetConnect(enabled: Boolean) {
        _settings.value = _settings.value.copy(resumeOnHeadsetConnect = enabled)
    }

    fun setAudioNormalization(enabled: Boolean) {
        _settings.value = _settings.value.copy(audioNormalization = enabled)
    }
}
