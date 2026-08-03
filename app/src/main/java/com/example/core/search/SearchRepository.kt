package com.example.core.search

import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity

object SearchRepository {

    val ONLINE_PLAYLISTS = listOf(
        OnlinePlaylist(
            id = "plist_1",
            name = "Cyberpunk 2099 Essentials",
            curator = "Aura Editors",
            coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600",
            trackCount = 24,
            description = "High-octane electro basslines and dark analog arpeggios in 24-bit FLAC.",
            moodCategory = "Workout"
        ),
        OnlinePlaylist(
            id = "plist_2",
            name = "Night Drive Lossless Mix",
            curator = "Neon Nexus",
            coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
            trackCount = 18,
            description = "Atmospheric synthwave & ambient beats for late night urban cruising.",
            moodCategory = "Night Drive"
        ),
        OnlinePlaylist(
            id = "plist_3",
            name = "Focus Flow: Zero Gravity",
            curator = "Aether Horizon",
            coverUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=600",
            trackCount = 30,
            description = "Deep ambient soundscapes and generative drone loops for intense deep work.",
            moodCategory = "Focus"
        ),
        OnlinePlaylist(
            id = "plist_4",
            name = "Lo-Fi Rain & Tokyo Coffee",
            curator = "Lofi Vibes",
            coverUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=600",
            trackCount = 15,
            description = "Warm vinyl crackles and gentle Rhodes chords recorded live.",
            moodCategory = "Chill"
        ),
        OnlinePlaylist(
            id = "plist_5",
            name = "Neon Pulse Party Anthem",
            curator = "Aura Club Mix",
            coverUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=600",
            trackCount = 20,
            description = "Mainstage synthwave and cyberpunk dance hits.",
            moodCategory = "Party"
        )
    )

    val MOOD_CATEGORIES = listOf(
        MoodCategory(
            id = "mood_chill",
            name = "Chill & Relax",
            description = "Ethereal ambient & warm lofi soundscapes",
            coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500"
        ),
        MoodCategory(
            id = "mood_focus",
            name = "Focus & Code",
            description = "Subtle generative drones & deep focus beats",
            coverUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=500"
        ),
        MoodCategory(
            id = "mood_workout",
            name = "Cyber Workout",
            description = "Aggressive synthwave basslines and 140+ BPM energy",
            coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500"
        ),
        MoodCategory(
            id = "mood_nightdrive",
            name = "Night Drive",
            description = "Cinematic retro-futuristic driving melodies",
            coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500"
        ),
        MoodCategory(
            id = "mood_party",
            name = "Pulse Party",
            description = "Electric electro-house and high energy drops",
            coverUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=500"
        )
    )

    // Clearly separated Local Files stored on device ("On This Device")
    val LOCAL_DEVICE_TRACKS = listOf(
        LocalDeviceTrack(
            id = "local_001",
            title = "Studio Jam Session 2026",
            artistName = "Local Device File",
            albumTitle = "Internal Recording",
            durationMs = 195000,
            fileName = "Jam_Session_2026.flac",
            filePath = "/storage/emulated/0/Music/Jam_Session_2026.flac",
            format = "FLAC 24-bit",
            fileSizeMb = 32.4f
        ),
        LocalDeviceTrack(
            id = "local_002",
            title = "Acoustic Guitar Voice Memo",
            artistName = "Local Recording",
            albumTitle = "Voice Memos",
            durationMs = 142000,
            fileName = "Voice_Memo_008.wav",
            filePath = "/storage/emulated/0/Recordings/Voice_Memo_008.wav",
            format = "WAV PCM",
            fileSizeMb = 18.2f
        ),
        LocalDeviceTrack(
            id = "local_003",
            title = "Offline Synth Draft",
            artistName = "On Device Audio",
            albumTitle = "Unreleased Demos",
            durationMs = 210000,
            fileName = "Synth_Draft_v2.mp3",
            filePath = "/storage/emulated/0/Music/Synth_Draft_v2.mp3",
            format = "MP3 320kbps",
            fileSizeMb = 5.1f
        )
    )

    val TRENDING_SEARCHES = listOf(
        "Lumina Eclipse",
        "Synthwave Odyssey",
        "CyberPulse",
        "Lossless FLAC",
        "Zero Gravity",
        "Tokyo Rain",
        "Night Drive",
        "Deep Focus"
    )

    // Map common typos to correct search queries
    private val TYPO_DICTIONARY = mapOf(
        "syntwave" to "Synthwave",
        "synwave" to "Synthwave",
        "cyberpunc" to "Cyberpunk",
        "lumina" to "Lumina Eclipse",
        "aether" to "Aether Horizon",
        "nexus" to "Neon Nexus",
        "tokyo" to "Tokyo Rain Arpeggios",
        "midnit" to "Midnight Horizon",
        "lofi" to "Lofi"
    )

    fun performSearch(
        query: String,
        filterOptions: SearchFilterOptions
    ): SearchResultsGroup {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return SearchResultsGroup()
        }

        // Check for typo correction
        val lowerQuery = trimmedQuery.lowercase()
        val correctedQuery = TYPO_DICTIONARY.entries.find { lowerQuery.contains(it.key) }?.value
        val effectiveSearchQuery = (correctedQuery ?: trimmedQuery).lowercase()

        // 1. Search Online Songs
        var matchedSongs = MusicCatalog.ALL_SONGS.filter { song ->
            song.title.lowercase().contains(effectiveSearchQuery) ||
                    song.artistName.lowercase().contains(effectiveSearchQuery) ||
                    song.albumTitle.lowercase().contains(effectiveSearchQuery) ||
                    song.genre.lowercase().contains(effectiveSearchQuery)
        }

        // 2. Search Online Artists
        var matchedArtists = MusicCatalog.FEATURED_ARTISTS.filter { artist ->
            artist.name.lowercase().contains(effectiveSearchQuery) ||
                    artist.bio.lowercase().contains(effectiveSearchQuery)
        }

        // 3. Search Online Albums
        var matchedAlbums = MusicCatalog.FEATURED_ALBUMS.filter { album ->
            album.title.lowercase().contains(effectiveSearchQuery) ||
                    album.artistName.lowercase().contains(effectiveSearchQuery)
        }

        // 4. Search Online Playlists
        var matchedPlaylists = ONLINE_PLAYLISTS.filter { playlist ->
            playlist.name.lowercase().contains(effectiveSearchQuery) ||
                    playlist.curator.lowercase().contains(effectiveSearchQuery) ||
                    playlist.description.lowercase().contains(effectiveSearchQuery) ||
                    playlist.moodCategory.lowercase().contains(effectiveSearchQuery)
        }

        // 5. Search Local Device Tracks (Separated)
        var matchedLocalTracks = LOCAL_DEVICE_TRACKS.filter { localTrack ->
            localTrack.title.lowercase().contains(effectiveSearchQuery) ||
                    localTrack.fileName.lowercase().contains(effectiveSearchQuery) ||
                    localTrack.artistName.lowercase().contains(effectiveSearchQuery)
        }

        // Apply Duration Filter
        if (filterOptions.durationFilter != "All") {
            matchedSongs = matchedSongs.filter { song ->
                when (filterOptions.durationFilter) {
                    "< 3 min" -> song.durationMs < 180000
                    "3-5 min" -> song.durationMs in 180000..300000
                    "> 5 min" -> song.durationMs > 300000
                    else -> true
                }
            }
        }

        // Apply Sorting
        matchedSongs = when (filterOptions.sortBy) {
            SortByOption.TITLE -> matchedSongs.sortedBy { it.title }
            SortByOption.RELEASE_DATE -> matchedSongs.sortedByDescending { it.releaseYear }
            SortByOption.POPULARITY -> matchedSongs // Default catalog order is popular
            SortByOption.RELEVANCE -> matchedSongs
        }

        // Filter by Category
        val finalSongs = if (filterOptions.category == SearchCategory.ALL || filterOptions.category == SearchCategory.SONGS) matchedSongs else emptyList()
        val finalArtists = if (filterOptions.category == SearchCategory.ALL || filterOptions.category == SearchCategory.ARTISTS) matchedArtists else emptyList()
        val finalAlbums = if (filterOptions.category == SearchCategory.ALL || filterOptions.category == SearchCategory.ALBUMS) matchedAlbums else emptyList()
        val finalPlaylists = if (filterOptions.category == SearchCategory.ALL || filterOptions.category == SearchCategory.PLAYLISTS) matchedPlaylists else emptyList()
        val finalLocalTracks = if (filterOptions.category == SearchCategory.ALL || filterOptions.category == SearchCategory.ON_DEVICE) matchedLocalTracks else emptyList()

        // Determine Top Result (e.g. Exact match on artist or song title)
        val topResult = finalArtists.firstOrNull { it.name.equals(trimmedQuery, ignoreCase = true) }
            ?: finalSongs.firstOrNull { it.title.equals(trimmedQuery, ignoreCase = true) }
            ?: finalSongs.firstOrNull()
            ?: finalArtists.firstOrNull()

        val totalCount = finalSongs.size + finalArtists.size + finalAlbums.size + finalPlaylists.size + finalLocalTracks.size

        val suggestions = if (totalCount == 0) {
            listOf("Lumina Eclipse", "CyberPulse", "Midnight Horizon", "Zero Gravity Meditation")
        } else emptyList()

        return SearchResultsGroup(
            topResult = topResult,
            songs = finalSongs,
            artists = finalArtists,
            albums = finalAlbums,
            playlists = finalPlaylists,
            localDeviceTracks = finalLocalTracks,
            autocorrectedQuery = if (correctedQuery != null && !correctedQuery.equals(trimmedQuery, ignoreCase = true)) correctedQuery else null,
            suggestions = suggestions,
            totalResultCount = totalCount
        )
    }

    fun getAutocompleteSuggestions(query: String): List<String> {
        if (query.trim().isEmpty()) return emptyList()
        val q = query.trim().lowercase()

        val songTitles = MusicCatalog.ALL_SONGS.map { it.title }.filter { it.lowercase().contains(q) }
        val artistNames = MusicCatalog.FEATURED_ARTISTS.map { it.name }.filter { it.lowercase().contains(q) }
        val albumTitles = MusicCatalog.FEATURED_ALBUMS.map { it.title }.filter { it.lowercase().contains(q) }
        val genres = MusicCatalog.GENRES.filter { it.lowercase().contains(q) }

        return (artistNames + songTitles + albumTitles + genres).distinct().take(5)
    }

    fun convertLocalTrackToSongEntity(localTrack: LocalDeviceTrack): SongEntity {
        return SongEntity(
            id = localTrack.id,
            title = localTrack.title,
            artistId = "local_art",
            artistName = localTrack.artistName,
            albumId = "local_alb",
            albumTitle = localTrack.albumTitle,
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500",
            durationMs = localTrack.durationMs,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            isHdAudio = true,
            isExplicit = false,
            genre = "Local Audio",
            releaseYear = 2026,
            audioQuality = "Local ${localTrack.format} • ${localTrack.fileSizeMb}MB",
            bitrate = "1411 kbps",
            codec = localTrack.format,
            isAvailable = true
        )
    }
}
