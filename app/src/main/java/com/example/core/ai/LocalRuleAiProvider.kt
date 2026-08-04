package com.example.core.ai

import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class LocalRuleAiProvider : AiProvider {

    override suspend fun generateAssistantResponse(prompt: String, context: AiContext): AiChatMessage = withContext(Dispatchers.Default) {
        val lowerPrompt = prompt.lowercase()
        val allSongs = MusicCatalog.ALL_SONGS

        // Detect intent
        return@withContext when {
            lowerPrompt.contains("relax") || lowerPrompt.contains("calm") || lowerPrompt.contains("chill") || lowerPrompt.contains("peaceful") -> {
                val songs = allSongs.filter { it.genre.contains("Ambient", true) || it.genre.contains("Lofi", true) || it.title.contains("Drift", true) || it.title.contains("Horizon", true) }
                    .ifEmpty { allSongs.take(4) }
                AiChatMessage(
                    sender = "ASSISTANT",
                    text = "Here is a relaxing selection of soothing soundscapes and calm beats for you to unwind.",
                    tracks = songs,
                    actionChips = listOf(
                        AiActionChip("Play All Relaxing", "play_relaxing"),
                        AiActionChip("Create Calm Playlist", "create_calm_playlist"),
                        AiActionChip("More Ambient Tracks", "more_ambient")
                    ),
                    intent = IntentType.PLAY_SONGS
                )
            }
            lowerPrompt.contains("favorite") || lowerPrompt.contains("liked") || lowerPrompt.contains("top songs") -> {
                val songs = context.likedSongs.ifEmpty { allSongs.take(5) }
                AiChatMessage(
                    sender = "ASSISTANT",
                    text = if (context.likedSongs.isNotEmpty()) "Here are your favorite tracks from your library!" else "You haven't liked songs yet, but here are top trending picks for you!",
                    tracks = songs,
                    actionChips = listOf(
                        AiActionChip("Play Favorites", "play_favorites"),
                        AiActionChip("Create Favorite Mix", "create_fav_mix")
                    ),
                    intent = IntentType.PLAY_SONGS
                )
            }
            lowerPrompt.contains("workout") || lowerPrompt.contains("gym") || lowerPrompt.contains("pump") || lowerPrompt.contains("energy") -> {
                val songs = allSongs.filter { it.genre.contains("Cyberpunk", true) || it.genre.contains("Electro", true) || it.genre.contains("Synthwave", true) }
                    .ifEmpty { allSongs.take(4) }
                AiChatMessage(
                    sender = "ASSISTANT",
                    text = "High-energy beats ready to power your workout session!",
                    tracks = songs,
                    actionChips = listOf(
                        AiActionChip("Start Workout Flow", "start_workout"),
                        AiActionChip("Save Gym Playlist", "save_gym_pl")
                    ),
                    intent = IntentType.PLAY_SONGS
                )
            }
            lowerPrompt.contains("hindi") || lowerPrompt.contains("romantic") || lowerPrompt.contains("sad") -> {
                val matched = allSongs.filter { it.title.contains("Cyber", true) || it.genre.contains("Ambient", true) || it.genre.contains("Lofi", true) }.take(4)
                AiChatMessage(
                    sender = "ASSISTANT",
                    text = "Found atmospheric and soulful tracks matching your emotional tone and preference.",
                    tracks = matched,
                    actionChips = listOf(
                        AiActionChip("Play Selection", "play_selection"),
                        AiActionChip("Explore More Like This", "explore_similar")
                    ),
                    intent = IntentType.SEARCH_FILTER
                )
            }
            lowerPrompt.contains("playlist") || lowerPrompt.contains("drive") || lowerPrompt.contains("trip") -> {
                val pl = generatePlaylist(prompt, MoodType.NIGHT_DRIVE, context, allSongs)
                AiChatMessage(
                    sender = "ASSISTANT",
                    text = "I've drafted a smart playlist '${pl.title}' tailored for your journey!",
                    generatedPlaylist = pl,
                    tracks = pl.tracks,
                    actionChips = listOf(
                        AiActionChip("Save to Library", "save_playlist_${pl.id}"),
                        AiActionChip("Play Playlist", "play_playlist_${pl.id}")
                    ),
                    intent = IntentType.CREATE_PLAYLIST
                )
            }
            lowerPrompt.contains("listening") || lowerPrompt.contains("stats") || lowerPrompt.contains("most") -> {
                val insights = generateInsights(context.recentHistory, context.likedSongs)
                AiChatMessage(
                    sender = "ASSISTANT",
                    text = "Your music personality is '${insights.personalityTitle}'! You've listened to ${insights.totalMinutesListened} minutes with a ${insights.discoveryScore}% discovery score.",
                    actionChips = listOf(
                        AiActionChip("View Detailed Insights", "nav_insights"),
                        AiActionChip("Weekly Report", "weekly_report")
                    ),
                    intent = IntentType.GET_INSIGHTS
                )
            }
            lowerPrompt.contains("study") || lowerPrompt.contains("focus") -> {
                val songs = allSongs.filter { it.genre.contains("Lofi", true) || it.genre.contains("Ambient", true) }
                AiChatMessage(
                    sender = "ASSISTANT",
                    text = "Deep focus and study tracks ready to keep you in the flow zone.",
                    tracks = songs,
                    actionChips = listOf(
                        AiActionChip("Play Study Focus", "play_focus"),
                        AiActionChip("Lo-Fi Radio", "lofi_radio")
                    ),
                    intent = IntentType.PLAY_SONGS
                )
            }
            lowerPrompt.contains("like this") && context.currentSong != null -> {
                val curr = context.currentSong
                val similar = allSongs.filter { it.id != curr.id && (it.artistId == curr.artistId || it.genre == curr.genre) }
                AiChatMessage(
                    sender = "ASSISTANT",
                    text = "Here are songs with a similar sound profile to '${curr.title}' by ${curr.artistName}:",
                    tracks = similar,
                    actionChips = listOf(
                        AiActionChip("Queue Similar", "queue_similar"),
                        AiActionChip("Start Radio", "start_radio")
                    ),
                    intent = IntentType.PLAY_SONGS
                )
            }
            else -> {
                // Fallback smart general music assistant
                val songs = allSongs.shuffled().take(3)
                AiChatMessage(
                    sender = "ASSISTANT",
                    text = "I'm your AI Music Companion! I can create playlists, suggest songs based on mood, filter music by activity, or analyze your listening stats.",
                    tracks = songs,
                    actionChips = listOf(
                        AiActionChip("Relaxing Music", "play relaxing music"),
                        AiActionChip("Workout Mix", "workout mix"),
                        AiActionChip("Generate Road Trip Playlist", "make a road trip playlist"),
                        AiActionChip("My Listening Stats", "what am I listening to most")
                    ),
                    intent = IntentType.GENERAL_QNA
                )
            }
        }
    }

    override suspend fun interpretSearchQuery(query: String, catalog: List<SongEntity>): SearchFilterResult = withContext(Dispatchers.Default) {
        val q = query.lowercase().trim()

        var detectedMood: MoodType? = null
        when {
            q.contains("chill") || q.contains("relax") -> detectedMood = MoodType.CALM
            q.contains("sad") || q.contains("heartbreak") -> detectedMood = MoodType.SAD
            q.contains("happy") || q.contains("upbeat") -> detectedMood = MoodType.HAPPY
            q.contains("gym") || q.contains("workout") -> detectedMood = MoodType.WORKOUT
            q.contains("study") || q.contains("focus") -> detectedMood = MoodType.STUDY
            q.contains("night") || q.contains("drive") -> detectedMood = MoodType.NIGHT_DRIVE
            q.contains("party") || q.contains("dance") -> detectedMood = MoodType.PARTY
            q.contains("rain") -> detectedMood = MoodType.RAINY
        }

        var detectedGenre: String? = null
        val genres = listOf("Cyberpunk", "Synthwave", "Ambient", "Lofi", "Electro", "Orchestral", "Deep House")
        for (g in genres) {
            if (q.contains(g.lowercase())) {
                detectedGenre = g
                break
            }
        }

        val filteredSongs = catalog.filter { song ->
            val matchTitle = song.title.lowercase().contains(q)
            val matchArtist = song.artistName.lowercase().contains(q)
            val matchAlbum = song.albumTitle.lowercase().contains(q)
            val matchGenre = song.genre.lowercase().contains(q)
            val matchMood = detectedMood != null && (song.genre.contains("Ambient", true) || song.genre.contains("Lofi", true) || song.genre.contains("Synthwave", true))
            matchTitle || matchArtist || matchAlbum || matchGenre || matchMood
        }.ifEmpty {
            if (detectedMood != null || detectedGenre != null) {
                catalog.take(5)
            } else {
                catalog.filter { it.title.contains(q, ignoreCase = true) || it.artistName.contains(q, ignoreCase = true) }
            }
        }

        val matchedArtists = MusicCatalog.FEATURED_ARTISTS.filter { it.name.lowercase().contains(q) }
        val matchedAlbums = MusicCatalog.FEATURED_ALBUMS.filter { it.title.lowercase().contains(q) }

        return@withContext SearchFilterResult(
            rawQuery = query,
            matchedSongs = filteredSongs,
            matchedArtists = matchedArtists,
            matchedAlbums = matchedAlbums,
            detectedMood = detectedMood,
            detectedGenre = detectedGenre,
            detectedTempo = if (q.contains("fast") || q.contains("upbeat")) "Fast" else if (q.contains("slow")) "Slow" else "Mid-tempo",
            detectedLanguage = if (q.contains("hindi")) "Hindi" else "English / Instrumental",
            detectedEnergy = if (q.contains("gym") || q.contains("party")) "High Energy" else "Chill Flow"
        )
    }

    override suspend fun generatePlaylist(
        prompt: String,
        mood: MoodType?,
        context: AiContext,
        catalog: List<SongEntity>
    ): GeneratedPlaylistResult = withContext(Dispatchers.Default) {
        val lower = prompt.lowercase()
        val title = when {
            lower.contains("workout") || lower.contains("gym") -> "Neon Power Workout"
            lower.contains("drive") || lower.contains("road") -> "Late Night Cyber Cruise"
            lower.contains("rain") -> "Rainy Windows & Lo-Fi"
            lower.contains("study") || lower.contains("focus") -> "Deep Focus Protocol"
            lower.contains("chill") || lower.contains("relax") -> "Ethereal Ambient Sanctuary"
            else -> "AI Curated: " + prompt.take(20).replaceFirstChar { it.uppercase() }
        }

        val description = "Generated by Aura AI based on '${prompt}' and your active music taste profile."
        val selectedTracks = catalog.shuffled().take(6)
        val coverUrl = selectedTracks.firstOrNull()?.coverUrl ?: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600"

        return@withContext GeneratedPlaylistResult(
            title = title,
            description = description,
            coverUrl = coverUrl,
            mood = mood ?: MoodType.NIGHT_DRIVE,
            tracks = selectedTracks
        )
    }

    override suspend fun getRecommendations(
        context: AiContext,
        catalog: List<SongEntity>
    ): List<RecommendationSection> = withContext(Dispatchers.Default) {
        val forYou = catalog.take(5)
        val moodPicks = catalog.filter { it.genre == "Ambient" || it.genre == "Lofi" }.ifEmpty { catalog.take(4) }
        val energyBoost = catalog.filter { it.genre == "Cyberpunk" || it.genre == "Electro" || it.genre == "Synthwave" }.ifEmpty { catalog.take(4) }
        val discoveryPicks = catalog.shuffled().take(5)

        return@withContext listOf(
            RecommendationSection("sec_for_you", "For You", "Personalized mix based on your recent activity", forYou, "FOR_YOU"),
            RecommendationSection("sec_mood", "Mood Picks", "Soothing sounds tuned to calm focus", moodPicks, "MOOD_PICKS"),
            RecommendationSection("sec_energy", "High Energy Flow", "Futuristic synth & driving electronic rhythms", energyBoost, "ENERGY_PICKS"),
            RecommendationSection("sec_discovery", "Fresh Discoveries", "Hidden gems and new artists you might love", discoveryPicks, "DISCOVERY_PICKS")
        )
    }

    override suspend fun generateInsights(
        history: List<SongEntity>,
        likes: List<SongEntity>
    ): MusicInsights = withContext(Dispatchers.Default) {
        val topArtists = MusicCatalog.FEATURED_ARTISTS.take(3)
        val topGenres = listOf(
            "Synthwave" to 0.42f,
            "Ambient" to 0.28f,
            "Cyberpunk" to 0.18f,
            "Lofi" to 0.12f
        )

        val totalMins = (history.size * 3.5 + 140).toInt()
        val cards = listOf(
            InsightCard("c1", "Peak Listening Time", "You listen to calm music most between 10 PM and 1 AM", "NIGHT_DRIVE", "#7C4DFF"),
            InsightCard("c2", "Replay Habit", "You replayed synthwave tracks 4x more than average this week", "REPEAT", "#00E5FF"),
            InsightCard("c3", "Discovery Explorer", "Your top discovery genre this month is Ambient Drone", "EXPLORE", "#FF4081")
        )

        return@withContext MusicInsights(
            topArtists = topArtists,
            topGenres = topGenres,
            peakListeningTime = "Late Night (10 PM - 2 AM)",
            totalMinutesListened = totalMins,
            skipRatePercent = 14,
            repeatRatePercent = 38,
            discoveryScore = 86,
            personalitySummary = "You are an exploratory futuristic listener who thrives on dark analog synths and ethereal ambient atmospheres.",
            personalityTitle = "The Cyberpunk Visionary",
            insightCards = cards
        )
    }

    override suspend fun getSmartQueue(
        currentSong: SongEntity?,
        mode: SmartQueueMode,
        catalog: List<SongEntity>
    ): List<SongEntity> = withContext(Dispatchers.Default) {
        if (currentSong == null) return@withContext catalog.shuffled().take(10)

        return@withContext when (mode) {
            SmartQueueMode.SMART_SIMILAR -> catalog.filter { it.id != currentSong.id && (it.genre == currentSong.genre || it.artistId == currentSong.artistId) }
                .ifEmpty { catalog.shuffled() }.take(10)
            SmartQueueMode.MOOD_FLOW -> catalog.filter { it.genre == currentSong.genre }.shuffled().take(10)
            SmartQueueMode.ENERGY_FLOW -> catalog.filter { it.isHdAudio }.shuffled().take(10)
            SmartQueueMode.ARTIST_FLOW -> catalog.filter { it.artistId == currentSong.artistId }.ifEmpty { catalog.shuffled() }.take(10)
            SmartQueueMode.DISCOVERY -> catalog.filter { it.id != currentSong.id }.shuffled().take(10)
            SmartQueueMode.COMFORT -> catalog.take(8)
            SmartQueueMode.SURPRISE -> catalog.shuffled().take(10)
        }
    }

    override suspend fun getMoodMusic(mood: MoodType, catalog: List<SongEntity>): List<SongEntity> = withContext(Dispatchers.Default) {
        return@withContext when (mood) {
            MoodType.CALM, MoodType.RAINY, MoodType.STUDY, MoodType.MEDITATION, MoodType.SLEEP, MoodType.FOCUS ->
                catalog.filter { it.genre == "Ambient" || it.genre == "Lofi" }.ifEmpty { catalog.take(5) }
            MoodType.ENERGETIC, MoodType.WORKOUT, MoodType.PARTY ->
                catalog.filter { it.genre == "Cyberpunk" || it.genre == "Electro" || it.genre == "Synthwave" }.ifEmpty { catalog.take(5) }
            else -> catalog.shuffled().take(5)
        }
    }

    override suspend fun getDjHostSpeech(song: SongEntity?, mood: MoodType?): DjHostSpeech = withContext(Dispatchers.Default) {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val timeGreeting = when {
            hour in 5..11 -> "Good morning"
            hour in 12..17 -> "Good afternoon"
            hour in 18..22 -> "Good evening"
            else -> "Late night vibe checking in"
        }

        val trackText = if (song != null) {
            "Currently playing '${song.title}' by ${song.artistName}. Notice how the atmospheric synth layers blend seamlessly."
        } else {
            "Welcome to Aura AI DJ! Let's get your music flow started."
        }

        val transition = "Transitioning smoothly into high-fidelity beats tailored for your ${mood?.displayName ?: "current vibe"}."

        return@withContext DjHostSpeech(
            greeting = "$timeGreeting! I'm Aura DJ Host, keeping your audio flow continuous and personalized.",
            trackComment = trackText,
            moodTransitionText = transition,
            nextTrackRecommendation = "Up next: an ambient synthwave masterpiece carefully selected based on your recent listening tempo."
        )
    }
}
