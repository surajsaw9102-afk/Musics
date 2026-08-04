package com.example.feature.social

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ProfileVisibilityManager {

    private val _privacySettings = MutableStateFlow(
        PrivacySettings(
            showListeningActivity = true,
            showPlayedSongs = true,
            showPublicPlaylists = true,
            isPrivateProfile = false,
            friendsOnlyVisibility = false
        )
    )
    val privacySettings: StateFlow<PrivacySettings> = _privacySettings.asStateFlow()

    fun updateListeningActivityVisibility(enabled: Boolean) {
        _privacySettings.value = _privacySettings.value.copy(showListeningActivity = enabled)
    }

    fun updatePlayedSongsVisibility(enabled: Boolean) {
        _privacySettings.value = _privacySettings.value.copy(showPlayedSongs = enabled)
    }

    fun updatePublicPlaylistsVisibility(enabled: Boolean) {
        _privacySettings.value = _privacySettings.value.copy(showPublicPlaylists = enabled)
    }

    fun updatePrivateProfile(enabled: Boolean) {
        _privacySettings.value = _privacySettings.value.copy(isPrivateProfile = enabled)
    }

    fun updateFriendsOnlyVisibility(enabled: Boolean) {
        _privacySettings.value = _privacySettings.value.copy(friendsOnlyVisibility = enabled)
    }
}
