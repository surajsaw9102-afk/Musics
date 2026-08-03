package com.example.core.catalog

import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity

object MusicCatalog {

    val GENRES = listOf(
        "All",
        "Cyberpunk",
        "Synthwave",
        "Ambient",
        "Lofi",
        "Electro",
        "Orchestral",
        "Deep House"
    )

    val FEATURED_ARTISTS = listOf(
        ArtistEntity(
            id = "art_1",
            name = "Lumina Eclipse",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500",
            bio = "Pioneer of futuristic cyberpunk audio synthesis and ethereal vocal landscapes.",
            monthlyListeners = 4250000,
            isVerified = true
        ),
        ArtistEntity(
            id = "art_2",
            name = "CyberPulse",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500",
            bio = "High-octane synthwave duo crafting dark analog basslines and electric hooks.",
            monthlyListeners = 2890000,
            isVerified = true
        ),
        ArtistEntity(
            id = "art_3",
            name = "Aether Horizon",
            avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500",
            bio = "Ambient soundscape designer crafting meditative, atmospheric zero-gravity compositions.",
            monthlyListeners = 1920000,
            isVerified = true
        ),
        ArtistEntity(
            id = "art_4",
            name = "Neon Nexus",
            avatarUrl = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=500",
            bio = "Tokyo-based futuristic synth producer fusing traditional instruments with digital arpeggios.",
            monthlyListeners = 3100000,
            isVerified = true
        )
    )

    val FEATURED_ALBUMS = listOf(
        AlbumEntity(
            id = "alb_1",
            title = "Synthwave Odyssey",
            artistId = "art_1",
            artistName = "Lumina Eclipse",
            coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600",
            releaseYear = 2026,
            totalTracks = 6
        ),
        AlbumEntity(
            id = "alb_2",
            title = "Neon Overdrive",
            artistId = "art_2",
            artistName = "CyberPulse",
            coverUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=600",
            releaseYear = 2025,
            totalTracks = 5
        ),
        AlbumEntity(
            id = "alb_3",
            title = "Celestial Drift",
            artistId = "art_3",
            artistName = "Aether Horizon",
            coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
            releaseYear = 2026,
            totalTracks = 4
        ),
        AlbumEntity(
            id = "alb_4",
            title = "Grid Protocol 2099",
            artistId = "art_4",
            artistName = "Neon Nexus",
            coverUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=600",
            releaseYear = 2026,
            totalTracks = 5
        )
    )

    val ALL_SONGS = listOf(
        SongEntity(
            id = "song_101",
            title = "Midnight Horizon",
            artistId = "art_1",
            artistName = "Lumina Eclipse",
            albumId = "alb_1",
            albumTitle = "Synthwave Odyssey",
            coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600",
            durationMs = 212000, // 3:32
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            isHdAudio = true,
            isExplicit = false,
            genre = "Synthwave",
            releaseYear = 2026,
            audioQuality = "Lossless FLAC 24-bit / 96kHz",
            bitrate = "1411 kbps",
            codec = "FLAC",
            isAvailable = true
        ),
        SongEntity(
            id = "song_102",
            title = "Cybernetic Resonance",
            artistId = "art_2",
            artistName = "CyberPulse",
            albumId = "alb_2",
            albumTitle = "Neon Overdrive",
            coverUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=600",
            durationMs = 245000, // 4:05
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            isHdAudio = true,
            isExplicit = false,
            genre = "Cyberpunk",
            releaseYear = 2025,
            audioQuality = "Hi-Res Masters 32-bit / 192kHz",
            bitrate = "2304 kbps",
            codec = "FLAC",
            isAvailable = true
        ),
        SongEntity(
            id = "song_103",
            title = "Zero Gravity Meditation",
            artistId = "art_3",
            artistName = "Aether Horizon",
            albumId = "alb_3",
            albumTitle = "Celestial Drift",
            coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
            durationMs = 189000, // 3:09
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            isHdAudio = true,
            isExplicit = false,
            genre = "Ambient",
            releaseYear = 2026,
            audioQuality = "Spatial Atmos 24-bit",
            bitrate = "1280 kbps",
            codec = "AAC",
            isAvailable = true
        ),
        SongEntity(
            id = "song_104",
            title = "Tokyo Rain Arpeggios",
            artistId = "art_4",
            artistName = "Neon Nexus",
            albumId = "alb_4",
            albumTitle = "Grid Protocol 2099",
            coverUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=600",
            durationMs = 278000, // 4:38
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            isHdAudio = true,
            isExplicit = false,
            genre = "Lofi",
            releaseYear = 2026,
            audioQuality = "Lossless FLAC 24-bit / 96kHz",
            bitrate = "1411 kbps",
            codec = "FLAC",
            isAvailable = true
        ),
        SongEntity(
            id = "song_105",
            title = "Starlight Continuum",
            artistId = "art_1",
            artistName = "Lumina Eclipse",
            albumId = "alb_1",
            albumTitle = "Synthwave Odyssey",
            coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600",
            durationMs = 230000, // 3:50
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
            isHdAudio = true,
            isExplicit = false,
            genre = "Synthwave",
            releaseYear = 2026,
            audioQuality = "Ultra HD 320 kbps AAC",
            bitrate = "320 kbps",
            codec = "AAC",
            isAvailable = true
        ),
        SongEntity(
            id = "song_106",
            title = "Aetherial Pulse",
            artistId = "art_2",
            artistName = "CyberPulse",
            albumId = "alb_2",
            albumTitle = "Neon Overdrive",
            coverUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600",
            durationMs = 198000, // 3:18
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
            isHdAudio = true,
            isExplicit = false,
            genre = "Electro",
            releaseYear = 2025,
            audioQuality = "Lossless FLAC 24-bit / 96kHz",
            bitrate = "1411 kbps",
            codec = "FLAC",
            isAvailable = true
        ),
        SongEntity(
            id = "song_107",
            title = "Quantum Echoes",
            artistId = "art_3",
            artistName = "Aether Horizon",
            albumId = "alb_3",
            albumTitle = "Celestial Drift",
            coverUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600",
            durationMs = 260000, // 4:20
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
            isHdAudio = true,
            isExplicit = false,
            genre = "Ambient",
            releaseYear = 2026,
            audioQuality = "Lossless FLAC 24-bit / 96kHz",
            bitrate = "1411 kbps",
            codec = "FLAC",
            isAvailable = true
        ),
        SongEntity(
            id = "song_108",
            title = "Orbital Velocity",
            artistId = "art_4",
            artistName = "Neon Nexus",
            albumId = "alb_4",
            albumTitle = "Grid Protocol 2099",
            coverUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=600",
            durationMs = 210000, // 3:30
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            isHdAudio = true,
            isExplicit = false,
            genre = "Deep House",
            releaseYear = 2026,
            audioQuality = "Lossless FLAC 24-bit / 96kHz",
            bitrate = "1411 kbps",
            codec = "FLAC",
            isAvailable = true
        )
    )

    fun getSongById(id: String): SongEntity? {
        return ALL_SONGS.find { it.id == id }
    }

    fun getSongsByGenre(genre: String): List<SongEntity> {
        if (genre == "All") return ALL_SONGS
        return ALL_SONGS.filter { it.genre.equals(genre, ignoreCase = true) }
    }

    fun getSongsByArtist(artistId: String): List<SongEntity> {
        return ALL_SONGS.filter { it.artistId == artistId }
    }

    fun getSongsByAlbum(albumId: String): List<SongEntity> {
        return ALL_SONGS.filter { it.albumId == albumId }
    }

    fun getArtistById(artistId: String): ArtistEntity? {
        return FEATURED_ARTISTS.find { it.id == artistId }
    }

    fun getAlbumById(albumId: String): AlbumEntity? {
        return FEATURED_ALBUMS.find { it.id == albumId }
    }

    fun searchSongs(query: String): List<SongEntity> {
        if (query.isBlank()) return ALL_SONGS
        val q = query.lowercase()
        return ALL_SONGS.filter {
            it.title.lowercase().contains(q) ||
            it.artistName.lowercase().contains(q) ||
            it.albumTitle.lowercase().contains(q) ||
            it.genre.lowercase().contains(q)
        }
    }
}
