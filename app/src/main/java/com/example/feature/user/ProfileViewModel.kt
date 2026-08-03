package com.example.feature.user

import androidx.lifecycle.ViewModel
import com.example.core.state.UserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileUiState(
    val displayName: String = "Alex Vance",
    val username: String = "@alexvance",
    val email: String = "alex@auramusic.io",
    val language: String = "English",
    val country: String = "United States",
    val isEditing: Boolean = false,
    val isSaved: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(displayName = name, isSaved = false)
    }

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username, isSaved = false)
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, isSaved = false)
    }

    fun updateLanguage(lang: String) {
        _uiState.value = _uiState.value.copy(language = lang, isSaved = false)
    }

    fun updateCountry(country: String) {
        _uiState.value = _uiState.value.copy(country = country, isSaved = false)
    }

    fun toggleEdit() {
        _uiState.value = _uiState.value.copy(isEditing = !_uiState.value.isEditing)
    }

    fun saveProfile(userState: UserState) {
        userState.updateProfile(
            displayName = _uiState.value.displayName,
            username = _uiState.value.username,
            email = _uiState.value.email,
            language = _uiState.value.language,
            country = _uiState.value.country
        )
        _uiState.value = _uiState.value.copy(isEditing = false, isSaved = true)
    }
}
