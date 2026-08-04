package com.example.feature.social

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ConnectionRepository {

    private val initialConnections = listOf(
        UserConnection(
            userId = "user_alex",
            displayName = "Alex Mercer",
            username = "alexm",
            avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=300",
            bio = "Synthwave enthusiast & producer. Driving into midnight neon.",
            status = ConnectionStatus.CONNECTED,
            mutualFollowsCount = 12,
            favoriteGenre = "Synthwave"
        ),
        UserConnection(
            userId = "user_sara",
            displayName = "Sara Connor",
            username = "sarac",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300",
            bio = "Lofi & Cyberpunk beats curator. Coffee & late night code.",
            status = ConnectionStatus.CONNECTED,
            mutualFollowsCount = 8,
            favoriteGenre = "Lofi Chill"
        ),
        UserConnection(
            userId = "user_david",
            displayName = "David Chen",
            username = "davidc",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
            bio = "Electronic & Ambient music junkie.",
            status = ConnectionStatus.PENDING_RECEIVED,
            mutualFollowsCount = 5,
            favoriteGenre = "Ambient"
        ),
        UserConnection(
            userId = "user_elena",
            displayName = "Elena Rostova",
            username = "elenar",
            avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=300",
            bio = "Pop & EDM party playlist creator.",
            status = ConnectionStatus.PENDING_SENT,
            mutualFollowsCount = 3,
            favoriteGenre = "EDM"
        ),
        UserConnection(
            userId = "user_marcus",
            displayName = "Marcus Vance",
            username = "marcusv",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300",
            bio = "Jazz Fusion & Retrowave listener.",
            status = ConnectionStatus.NOT_CONNECTED,
            mutualFollowsCount = 14,
            favoriteGenre = "Jazz"
        )
    )

    private val _connections = MutableStateFlow<List<UserConnection>>(initialConnections)
    val connections: StateFlow<List<UserConnection>> = _connections.asStateFlow()

    fun searchUsers(query: String): List<UserConnection> {
        if (query.isBlank()) return _connections.value
        return _connections.value.filter {
            it.displayName.contains(query, ignoreCase = true) ||
            it.username.contains(query, ignoreCase = true) ||
            it.favoriteGenre.contains(query, ignoreCase = true)
        }
    }

    fun sendConnectionRequest(userId: String) {
        _connections.value = _connections.value.map {
            if (it.userId == userId) it.copy(status = ConnectionStatus.PENDING_SENT) else it
        }
    }

    fun acceptConnectionRequest(userId: String) {
        _connections.value = _connections.value.map {
            if (it.userId == userId) it.copy(status = ConnectionStatus.CONNECTED) else it
        }
    }

    fun declineConnectionRequest(userId: String) {
        _connections.value = _connections.value.map {
            if (it.userId == userId) it.copy(status = ConnectionStatus.NOT_CONNECTED) else it
        }
    }

    fun removeConnection(userId: String) {
        _connections.value = _connections.value.map {
            if (it.userId == userId) it.copy(status = ConnectionStatus.NOT_CONNECTED) else it
        }
    }
}
