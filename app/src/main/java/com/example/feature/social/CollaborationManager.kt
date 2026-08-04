package com.example.feature.social

import com.example.feature.library.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object CollaborationManager {

    private val _collaborativePlaylists = MutableStateFlow<List<CollaborativePlaylist>>(
        listOf(
            CollaborativePlaylist(
                playlistId = "pl_collab_synth",
                title = "Cyberpunk Night Drive 🚗⚡",
                description = "Shared playlist for midnight highway cruising. Add your favorite synthwave and retrowave hits!",
                coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
                isCollaborative = true,
                inviteCode = "CYBER2026",
                ownerId = "user_me",
                ownerName = "You",
                collaborators = listOf(
                    CollaboratorInfo(
                        userId = "user_me",
                        name = "You",
                        handle = "@you",
                        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                        role = CollaboratorRole.OWNER,
                        tracksAddedCount = 8
                    ),
                    CollaboratorInfo(
                        userId = "user_alex",
                        name = "Alex Mercer",
                        handle = "@alexm",
                        avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=300",
                        role = CollaboratorRole.CONTRIBUTOR,
                        tracksAddedCount = 5
                    ),
                    CollaboratorInfo(
                        userId = "user_sara",
                        name = "Sara Connor",
                        handle = "@sarac",
                        avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300",
                        role = CollaboratorRole.CONTRIBUTOR,
                        tracksAddedCount = 3
                    )
                ),
                activityLogs = listOf(
                    PlaylistActivityLog(id = "log_1", timestampMs = System.currentTimeMillis() - 600000, userId = "user_alex", userName = "Alex Mercer", action = "Added 'Stellar Drift'", songTitle = "Stellar Drift"),
                    PlaylistActivityLog(id = "log_2", timestampMs = System.currentTimeMillis() - 3600000, userId = "user_sara", userName = "Sara Connor", action = "Added 'Neon Sunset'", songTitle = "Neon Sunset"),
                    PlaylistActivityLog(id = "log_3", timestampMs = System.currentTimeMillis() - 86400000, userId = "user_me", userName = "You", action = "Created collaborative playlist")
                ),
                songIds = listOf("s1", "s2", "s3", "s4", "s5")
            )
        )
    )
    val collaborativePlaylists: StateFlow<List<CollaborativePlaylist>> = _collaborativePlaylists.asStateFlow()

    fun createCollaborativePlaylist(title: String, description: String, coverUrl: String? = null): CollaborativePlaylist {
        val newInviteCode = UUID.randomUUID().toString().take(6).uppercase()
        val playlistId = "pl_collab_${UUID.randomUUID().toString().take(8)}"
        val newCollabPlaylist = CollaborativePlaylist(
            playlistId = playlistId,
            title = title,
            description = description,
            coverUrl = coverUrl ?: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
            isCollaborative = true,
            inviteCode = newInviteCode,
            ownerId = "user_me",
            ownerName = "You",
            collaborators = listOf(
                CollaboratorInfo(
                    userId = "user_me",
                    name = "You",
                    handle = "@you",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                    role = CollaboratorRole.OWNER,
                    tracksAddedCount = 0
                )
            ),
            activityLogs = listOf(
                PlaylistActivityLog(
                    id = UUID.randomUUID().toString(),
                    timestampMs = System.currentTimeMillis(),
                    userId = "user_me",
                    userName = "You",
                    action = "Created collaborative playlist"
                )
            )
        )
        val updated = _collaborativePlaylists.value + newCollabPlaylist
        _collaborativePlaylists.value = updated

        // Sync with LibraryRepository playlist creation
        LibraryRepository.createPlaylist(title, description)
        return newCollabPlaylist
    }

    fun toggleCollaborativeMode(playlistId: String): Boolean {
        val list = _collaborativePlaylists.value.toMutableList()
        val index = list.indexOfFirst { it.playlistId == playlistId }
        if (index != -1) {
            val item = list[index]
            val updatedItem = item.copy(isCollaborative = !item.isCollaborative)
            list[index] = updatedItem
            _collaborativePlaylists.value = list
            return updatedItem.isCollaborative
        } else {
            // Enable collab mode for existing standard playlist
            val existingLocalPlaylist = LibraryRepository.state.value.playlists.find { it.id == playlistId }
            val newInviteCode = UUID.randomUUID().toString().take(6).uppercase()
            val newCollabPlaylist = CollaborativePlaylist(
                playlistId = playlistId,
                title = existingLocalPlaylist?.name ?: "Collaborative Playlist",
                description = existingLocalPlaylist?.description ?: "Shared music collection",
                coverUrl = existingLocalPlaylist?.coverUrl ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
                isCollaborative = true,
                inviteCode = newInviteCode,
                ownerId = "user_me",
                ownerName = "You",
                collaborators = listOf(
                    CollaboratorInfo(
                        userId = "user_me",
                        name = "You",
                        handle = "@you",
                        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                        role = CollaboratorRole.OWNER
                    )
                ),
                activityLogs = listOf(
                    PlaylistActivityLog(
                        id = UUID.randomUUID().toString(),
                        timestampMs = System.currentTimeMillis(),
                        userId = "user_me",
                        userName = "You",
                        action = "Enabled collaborative mode"
                    )
                )
            )
            _collaborativePlaylists.value = list + newCollabPlaylist
            return true
        }
    }

    fun joinByInviteCode(inviteCode: String): CollaborativePlaylist? {
        val found = _collaborativePlaylists.value.find { it.inviteCode.equals(inviteCode, ignoreCase = true) }
        if (found != null) {
            // Add user if not already present
            val isAlreadyMember = found.collaborators.any { it.userId == "user_me" }
            if (!isAlreadyMember) {
                val newCollabs = found.collaborators + CollaboratorInfo(
                    userId = "user_me",
                    name = "You",
                    handle = "@you",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                    role = CollaboratorRole.CONTRIBUTOR
                )
                val newLog = PlaylistActivityLog(
                    id = UUID.randomUUID().toString(),
                    timestampMs = System.currentTimeMillis(),
                    userId = "user_me",
                    userName = "You",
                    action = "Joined playlist via invite code"
                )
                val updated = found.copy(
                    collaborators = newCollabs,
                    activityLogs = listOf(newLog) + found.activityLogs
                )
                _collaborativePlaylists.value = _collaborativePlaylists.value.map {
                    if (it.playlistId == found.playlistId) updated else it
                }
                return updated
            }
            return found
        }
        return null
    }

    fun addTrackToCollaborativePlaylist(playlistId: String, songId: String, songTitle: String) {
        val list = _collaborativePlaylists.value.toMutableList()
        val index = list.indexOfFirst { it.playlistId == playlistId }
        if (index != -1) {
            val target = list[index]
            val updatedSongs = target.songIds + songId
            val newLog = PlaylistActivityLog(
                id = UUID.randomUUID().toString(),
                timestampMs = System.currentTimeMillis(),
                userId = "user_me",
                userName = "You",
                action = "Added '$songTitle'",
                songTitle = songTitle
            )
            // Update contributor added tracks count
            val updatedCollabs = target.collaborators.map { collab ->
                if (collab.userId == "user_me") {
                    collab.copy(tracksAddedCount = collab.tracksAddedCount + 1)
                } else collab
            }
            list[index] = target.copy(
                songIds = updatedSongs,
                collaborators = updatedCollabs,
                activityLogs = listOf(newLog) + target.activityLogs
            )
            _collaborativePlaylists.value = list

            // Also add to local library database
            LibraryRepository.addSongsToPlaylist(playlistId, listOf(songId))
        }
    }

    fun removeTrackFromCollaborativePlaylist(playlistId: String, songId: String, songTitle: String) {
        val list = _collaborativePlaylists.value.toMutableList()
        val index = list.indexOfFirst { it.playlistId == playlistId }
        if (index != -1) {
            val target = list[index]
            val updatedSongs = target.songIds - songId
            val newLog = PlaylistActivityLog(
                id = UUID.randomUUID().toString(),
                timestampMs = System.currentTimeMillis(),
                userId = "user_me",
                userName = "You",
                action = "Removed '$songTitle'",
                songTitle = songTitle
            )
            list[index] = target.copy(
                songIds = updatedSongs,
                activityLogs = listOf(newLog) + target.activityLogs
            )
            _collaborativePlaylists.value = list
        }
    }
}
