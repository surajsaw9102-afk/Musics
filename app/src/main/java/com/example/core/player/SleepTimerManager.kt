package com.example.core.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class SleepTimerOption(val label: String, val minutes: Int) {
    OFF("Off", 0),
    MIN_10("10 Minutes", 10),
    MIN_15("15 Minutes", 15),
    MIN_30("30 Minutes", 30),
    MIN_45("45 Minutes", 45),
    MIN_60("60 Minutes", 60),
    END_OF_SONG("End of Song", -1)
}

object SleepTimerManager {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    private val _remainingSeconds = MutableStateFlow<Long?>(null)
    val remainingSeconds: StateFlow<Long?> = _remainingSeconds.asStateFlow()

    private val _activeOption = MutableStateFlow(SleepTimerOption.OFF)
    val activeOption: StateFlow<SleepTimerOption> = _activeOption.asStateFlow()

    private var initialVolume: Float = 1.0f

    fun startTimer(option: SleepTimerOption, onTimerComplete: () -> Unit) {
        cancelTimer()
        _activeOption.value = option

        if (option == SleepTimerOption.OFF) return

        if (option == SleepTimerOption.END_OF_SONG) {
            _remainingSeconds.value = -1L // Marker for End of Song
            return
        }

        val totalMs = option.minutes * 60 * 1000L
        initialVolume = AuraAudioPlayerManager.state.value.volume

        timerJob = scope.launch {
            var remaining = totalMs
            while (isActive && remaining > 0) {
                _remainingSeconds.value = remaining / 1000L

                // Fade out audio during last 10 seconds
                if (remaining <= 10000L && remaining > 0) {
                    val fadeRatio = (remaining.toFloat() / 10000L).coerceIn(0f, 1f)
                    AuraAudioPlayerManager.setVolume(initialVolume * fadeRatio)
                }

                delay(1000L)
                remaining -= 1000L
            }

            // Timer completed
            if (isActive) {
                AuraAudioPlayerManager.setVolume(initialVolume) // Reset volume setting
                onTimerComplete()
                cancelTimer()
            }
        }
    }

    fun onSongEnded(onTimerComplete: () -> Unit) {
        if (_activeOption.value == SleepTimerOption.END_OF_SONG) {
            onTimerComplete()
            cancelTimer()
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _activeOption.value = SleepTimerOption.OFF
        _remainingSeconds.value = null
    }

    fun formatRemainingTime(seconds: Long?): String {
        if (seconds == null || seconds <= 0) return ""
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }
}
