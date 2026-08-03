package com.example.core.state

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsData(
    val selectedLanguage: String = "English",
    val selectedCountry: String = "United States",
    val audioQuality: String = "Lossless FLAC (Hi-Res)",
    val crossfadeSeconds: Int = 3,
    val isDataSaverEnabled: Boolean = false,
    val isPushNotificationsEnabled: Boolean = true,
    val isDownloadOverWifiOnly: Boolean = true,
    val cacheSizeMb: Double = 142.5
)

class SettingsState : ViewModel() {
    private val _settings = MutableStateFlow(SettingsData())
    val settings: StateFlow<SettingsData> = _settings.asStateFlow()

    fun updateLanguage(lang: String) {
        _settings.value = _settings.value.copy(selectedLanguage = lang)
    }

    fun updateAudioQuality(quality: String) {
        _settings.value = _settings.value.copy(audioQuality = quality)
    }

    fun toggleNotifications(enabled: Boolean) {
        _settings.value = _settings.value.copy(isPushNotificationsEnabled = enabled)
    }

    fun toggleDataSaver(enabled: Boolean) {
        _settings.value = _settings.value.copy(isDataSaverEnabled = enabled)
    }

    fun clearCache() {
        _settings.value = _settings.value.copy(cacheSizeMb = 0.0)
    }
}
