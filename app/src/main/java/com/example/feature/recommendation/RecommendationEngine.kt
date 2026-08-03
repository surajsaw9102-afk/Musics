package com.example.feature.recommendation

import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity

object RecommendationEngine {

    /**
     * Calculates an Affinity Score (0.0f to 1.0f) for a given song based on the user's taste profile.
     */
    fun calculateSongAffinity(song: SongEntity, profile: UserTasteProfile): Float {
        var score = 0.3f // Baseline affinity

        // Genre match boost
        val genreWeight = profile.topGenres[song.genre] ?: 0.1f
        score += genreWeight * 0.35f

        // Artist match boost
        val artistWeight = profile.topArtistIds[song.artistId] ?: 0.1f
        score += artistWeight * 0.25f

        // Liked track boost
        if (profile.likedSongIds.contains(song.id)) {
            score += 0.25f
        }

        // Play frequency boost
        val playCount = profile.playCountMap[song.id] ?: 0
        if (playCount > 0) {
            score += (playCount * 0.05f).coerceAtMost(0.2f)
        }

        // Skipped track penalty
        if (profile.skippedSongIds.contains(song.id)) {
            score -= 0.4f
        }

        return score.coerceIn(0.0f, 1.0f)
    }

    /**
     * Speed Dial: Quick access tiles for top played and recently played songs (4 to 6 items).
     */
    fun getSpeedDialTracks(profile: UserTasteProfile): List<SongEntity> {
        val played = profile.playHistory.distinctBy { it.id }
        if (played.isNotEmpty()) {
            return (played + MusicCatalog.ALL_SONGS)
                .distinctBy { it.id }
                .take(6)
        }
        return MusicCatalog.ALL_SONGS.take(6)
    }

    /**
     * Continue Listening: Resume last played or history songs.
     */
    fun getContinueListeningTracks(profile: UserTasteProfile): List<SongEntity> {
        if (profile.playHistory.isNotEmpty()) {
            return profile.playHistory.distinctBy { it.id }.take(5)
        }
        return MusicCatalog.ALL_SONGS.take(3)
    }

    /**
     * Your Choices / Made For You: Highest affinity scores sorted descending.
     */
    fun getYourChoicesTracks(profile: UserTasteProfile): List<SongEntity> {
        return MusicCatalog.ALL_SONGS
            .sortedByDescending { calculateSongAffinity(it, profile) }
            .take(6)
    }

    /**
     * Because You Listened To: Recommendations based on last played song or top artist.
     */
    fun getBecauseYouListenedTo(profile: UserTasteProfile): Pair<String, List<SongEntity>> {
        val lastSong = profile.lastPlayedSong ?: MusicCatalog.ALL_SONGS.first()
        val rationale = "Because you listened to ${lastSong.title}"

        val matchingGenre = MusicCatalog.getSongsByGenre(lastSong.genre)
            .filter { it.id != lastSong.id }
        val matchingArtist = MusicCatalog.getSongsByArtist(lastSong.artistId)
            .filter { it.id != lastSong.id }

        val combined = (matchingArtist + matchingGenre + MusicCatalog.ALL_SONGS)
            .distinctBy { it.id }
            .take(5)

        return Pair(rationale, combined)
    }

    /**
     * More Like This: Recommendations similar to top artist or genre.
     */
    fun getMoreLikeThisTracks(profile: UserTasteProfile): Pair<String, List<SongEntity>> {
        val topGenre = profile.topGenres.maxByOrNull { it.value }?.key ?: "Synthwave"
        val rationale = "More Like This: $topGenre Vibe"

        val songs = MusicCatalog.getSongsByGenre(topGenre)
        if (songs.size >= 3) {
            return Pair(rationale, songs.take(6))
        }
        return Pair(rationale, MusicCatalog.ALL_SONGS.take(6))
    }

    /**
     * Recommended Artists based on matching top genres.
     */
    fun getRecommendedArtists(profile: UserTasteProfile): List<ArtistEntity> {
        val topArtistIds = profile.topArtistIds.keys
        val featured = MusicCatalog.FEATURED_ARTISTS

        return featured.sortedByDescending { artist ->
            var score = 0.2f
            if (topArtistIds.contains(artist.id)) score += 0.5f
            val artistSongs = MusicCatalog.getSongsByArtist(artist.id)
            if (artistSongs.any { profile.topGenres.containsKey(it.genre) }) score += 0.3f
            score
        }
    }

    /**
     * Recommended Albums matching user taste.
     */
    fun getRecommendedAlbums(profile: UserTasteProfile): List<AlbumEntity> {
        return MusicCatalog.FEATURED_ALBUMS.sortedByDescending { album ->
            val songs = MusicCatalog.getSongsByAlbum(album.id)
            songs.maxOfOrNull { calculateSongAffinity(it, profile) } ?: 0.5f
        }
    }

    /**
     * Discover Something New: Tracks with low play count but high general rating/quality outside top 1 genre.
     */
    fun getDiscoverNewTracks(profile: UserTasteProfile): List<SongEntity> {
        val topGenre = profile.topGenres.maxByOrNull { it.value }?.key
        val unexplored = MusicCatalog.ALL_SONGS.filter { song ->
            val playCount = profile.playCountMap[song.id] ?: 0
            playCount == 0 && song.genre != topGenre
        }

        if (unexplored.isNotEmpty()) {
            return unexplored.shuffled().take(6)
        }
        return MusicCatalog.ALL_SONGS.shuffled().take(6)
    }
}
