package com.example.core.state

import androidx.lifecycle.ViewModel
import com.example.core.storage.StorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsData(
    val selectedLanguage: String = "English",
    val selectedCountry: String = "United States",
    // Appearance
    val accentColor: AuraAccentColor = AuraAccentColor.NEON_CYAN,
    val fontScale: String = "Normal",
    val isReducedMotionEnabled: Boolean = false,
    // Playback
    val isCrossfadeEnabled: Boolean = true,
    val crossfadeSeconds: Int = 3,
    val isGaplessEnabled: Boolean = true,
    val isNormalizeVolumeEnabled: Boolean = true,
    val isAutoplayEnabled: Boolean = true,
    val isRememberPositionEnabled: Boolean = true,
    val isResumePlaybackEnabled: Boolean = false,
    val isExplicitFilterEnabled: Boolean = false,
    // Audio Quality
    val streamingQuality: String = "Lossless FLAC (24-bit/96kHz)",
    val downloadQuality: String = "Lossless FLAC (Hi-Res)",
    val isDownloadOverWifiOnly: Boolean = true,
    val isDataSaverEnabled: Boolean = false,
    val audioOutputMode: AudioOutputMode = AudioOutputMode.SPATIAL_3D,
    // Privacy & History
    val historyVisibility: HistoryVisibility = HistoryVisibility.PUBLIC,
    val dataRetentionPeriod: String = "90 Days",
    val isPushNotificationsEnabled: Boolean = true,
    // Cache stats
    val cacheSizeMb: Double = 142.5,
    val downloadSizeMb: Double = 320.8
)

class SettingsState : ViewModel() {
    private val _settings = MutableStateFlow(SettingsData())
    val settings: StateFlow<SettingsData> = _settings.asStateFlow()

    fun updateLanguage(lang: String) {
        _settings.value = _settings.value.copy(selectedLanguage = lang)
    }

    fun updateCountry(country: String) {
        _settings.value = _settings.value.copy(selectedCountry = country)
    }

    fun setAccentColor(color: AuraAccentColor) {
        _settings.value = _settings.value.copy(accentColor = color)
    }

    fun setFontScale(scale: String) {
        _settings.value = _settings.value.copy(fontScale = scale)
    }

    fun toggleReducedMotion(enabled: Boolean) {
        _settings.value = _settings.value.copy(isReducedMotionEnabled = enabled)
    }

    fun toggleCrossfade(enabled: Boolean) {
        _settings.value = _settings.value.copy(isCrossfadeEnabled = enabled)
    }

    fun setCrossfadeDuration(seconds: Int) {
        _settings.value = _settings.value.copy(crossfadeSeconds = seconds)
    }

    fun toggleGapless(enabled: Boolean) {
        _settings.value = _settings.value.copy(isGaplessEnabled = enabled)
    }

    fun toggleNormalizeVolume(enabled: Boolean) {
        _settings.value = _settings.value.copy(isNormalizeVolumeEnabled = enabled)
    }

    fun toggleAutoplay(enabled: Boolean) {
        _settings.value = _settings.value.copy(isAutoplayEnabled = enabled)
    }

    fun toggleRememberPosition(enabled: Boolean) {
        _settings.value = _settings.value.copy(isRememberPositionEnabled = enabled)
    }

    fun toggleResumePlayback(enabled: Boolean) {
        _settings.value = _settings.value.copy(isResumePlaybackEnabled = enabled)
    }

    fun toggleExplicitFilter(enabled: Boolean) {
        _settings.value = _settings.value.copy(isExplicitFilterEnabled = enabled)
    }

    fun updateStreamingQuality(quality: String) {
        _settings.value = _settings.value.copy(streamingQuality = quality)
    }

    fun updateDownloadQuality(quality: String) {
        _settings.value = _settings.value.copy(downloadQuality = quality)
    }

    fun toggleWifiOnlyDownloads(enabled: Boolean) {
        _settings.value = _settings.value.copy(isDownloadOverWifiOnly = enabled)
    }

    fun toggleDataSaver(enabled: Boolean) {
        _settings.value = _settings.value.copy(isDataSaverEnabled = enabled)
    }

    fun setAudioOutputMode(mode: AudioOutputMode) {
        _settings.value = _settings.value.copy(audioOutputMode = mode)
    }

    fun setHistoryVisibility(visibility: HistoryVisibility) {
        _settings.value = _settings.value.copy(historyVisibility = visibility)
    }

    fun setDataRetentionPeriod(period: String) {
        _settings.value = _settings.value.copy(dataRetentionPeriod = period)
    }

    fun toggleNotifications(enabled: Boolean) {
        _settings.value = _settings.value.copy(isPushNotificationsEnabled = enabled)
    }

    fun clearCache() {
        StorageManager.clearCache()
        _settings.value = _settings.value.copy(cacheSizeMb = 0.0)
    }

    fun deleteAllDownloads() {
        StorageManager.deleteAllDownloads()
        _settings.value = _settings.value.copy(downloadSizeMb = 0.0)
    }
}
