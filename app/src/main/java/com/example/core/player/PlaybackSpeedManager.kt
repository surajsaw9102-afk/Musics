package com.example.core.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PlaybackSpeedManager {
    val SPEED_OPTIONS = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    private val _currentSpeed = MutableStateFlow(1.0f)
    val currentSpeed: StateFlow<Float> = _currentSpeed.asStateFlow()

    private val _preservePitch = MutableStateFlow(true)
    val preservePitch: StateFlow<Boolean> = _preservePitch.asStateFlow()

    fun setSpeed(speed: Float) {
        if (speed in SPEED_OPTIONS) {
            _currentSpeed.value = speed
            AuraAudioPlayerManager.setPlaybackSpeed(speed)
        }
    }

    fun togglePreservePitch() {
        val newPitchState = !_preservePitch.value
        _preservePitch.value = newPitchState
        AuraAudioPlayerManager.setPitch(if (newPitchState) 1.0f else _currentSpeed.value)
    }
}
