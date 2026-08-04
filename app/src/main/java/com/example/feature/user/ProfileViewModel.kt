package com.example.feature.user

import androidx.lifecycle.ViewModel
import com.example.core.state.AuraAccentColor
import com.example.core.state.PreferencesManager
import com.example.core.state.UserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

val AVATAR_PRESETS = listOf(
    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300",
    "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=300",
    "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=300",
    "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=300"
)

val ALL_GENRE_OPTIONS = listOf(
    "Synthwave", "Cyberpunk", "Lofi Beats", "Ambient", "Indie Rock",
    "Electronic", "EDM", "Pop", "Hip Hop", "Jazz", "Classical", "Chillout"
)

val ALL_ARTIST_OPTIONS = listOf(
    "Aura Synthetics", "Starlight Protocol", "Lofi Spheres",
    "The Weeknd", "Daft Punk", "Kavinsky", "Lorn", "Tycho"
)

data class ProfileUiState(
    val displayName: String = "Alex Vance",
    val username: String = "@alexvance",
    val email: String = "alex@auramusic.io",
    val photoUrl: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
    val bio: String = "Audiophile, Synthwave enthusiast & digital music collector.",
    val language: String = "English",
    val country: String = "United States",
    val favoriteGenres: List<String> = listOf("Synthwave", "Cyberpunk", "Lofi Beats", "Ambient"),
    val favoriteArtists: List<String> = listOf("Aura Synthetics", "Starlight Protocol", "Lofi Spheres"),
    val accentColor: AuraAccentColor = AuraAccentColor.NEON_CYAN,
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
    val selectedTab: Int = 0
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(displayName = name, isSaved = false)
    }

    fun updateUsername(username: String) {
        val formatted = if (username.startsWith("@")) username else "@$username"
        _uiState.value = _uiState.value.copy(username = formatted, isSaved = false)
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, isSaved = false)
    }

    fun updatePhotoUrl(url: String) {
        _uiState.value = _uiState.value.copy(photoUrl = url, isSaved = false)
    }

    fun updateBio(bio: String) {
        _uiState.value = _uiState.value.copy(bio = bio, isSaved = false)
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun toggleGenreSelection(genre: String) {
        val current = _uiState.value.favoriteGenres.toMutableList()
        if (genre in current) {
            current.remove(genre)
        } else {
            current.add(genre)
        }
        _uiState.value = _uiState.value.copy(favoriteGenres = current, isSaved = false)
    }

    fun toggleArtistSelection(artist: String) {
        val current = _uiState.value.favoriteArtists.toMutableList()
        if (artist in current) {
            current.remove(artist)
        } else {
            current.add(artist)
        }
        _uiState.value = _uiState.value.copy(favoriteArtists = current, isSaved = false)
    }

    fun updateAccentColor(accent: AuraAccentColor) {
        _uiState.value = _uiState.value.copy(accentColor = accent, isSaved = false)
    }

    fun toggleEdit() {
        _uiState.value = _uiState.value.copy(isEditing = !_uiState.value.isEditing)
    }

    fun saveProfile(userState: UserState) {
        userState.updateProfile(
            displayName = _uiState.value.displayName,
            username = _uiState.value.username,
            email = _uiState.value.email,
            photoUrl = _uiState.value.photoUrl,
            bio = _uiState.value.bio,
            language = _uiState.value.language,
            country = _uiState.value.country
        )
        PreferencesManager.updateFavoriteGenres(_uiState.value.favoriteGenres)
        PreferencesManager.updateFavoriteArtists(_uiState.value.favoriteArtists)
        _uiState.value = _uiState.value.copy(isEditing = false, isSaved = true)
    }
}
