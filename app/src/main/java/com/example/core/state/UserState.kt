package com.example.core.state

import androidx.lifecycle.ViewModel
import com.example.core.database.entities.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSession(
    val isAuthenticated: Boolean = false,
    val user: UserEntity? = null
)

class UserState : ViewModel() {
    private val _session = MutableStateFlow(
        UserSession(
            isAuthenticated = true,
            user = UserEntity(
                id = "usr_9981",
                displayName = "Alex Vance",
                username = "@alexvance",
                email = "alex@auramusic.io",
                photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                bio = "Audiophile, Synthwave enthusiast & digital music collector.",
                language = "English",
                country = "United States",
                themeMode = "DARK",
                isFreeMember = true
            )
        )
    )
    val session: StateFlow<UserSession> = _session.asStateFlow()

    fun updateProfile(
        displayName: String,
        username: String,
        email: String,
        photoUrl: String?,
        bio: String,
        language: String,
        country: String
    ) {
        val current = _session.value.user ?: return
        _session.value = _session.value.copy(
            user = current.copy(
                displayName = displayName,
                username = username,
                email = email,
                photoUrl = photoUrl,
                bio = bio,
                language = language,
                country = country
            )
        )
    }

    fun logout() {
        _session.value = UserSession(isAuthenticated = false, user = null)
    }

    fun loginGuest() {
        _session.value = UserSession(
            isAuthenticated = true,
            user = UserEntity(
                id = "usr_guest",
                displayName = "Music Lover",
                username = "@guest_listener",
                email = "guest@auramusic.io",
                bio = "Free listener enjoying unlimited music streaming.",
                isFreeMember = true
            )
        )
    }
}
