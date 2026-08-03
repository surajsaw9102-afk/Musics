package com.example.feature.home

import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.SongEntity
import com.example.feature.recommendation.RecommendationEngine
import com.example.feature.recommendation.TimeOfDay
import com.example.feature.recommendation.UserTasteProfile

object HomeFeedGenerator {

    fun generateFeed(
        profile: UserTasteProfile,
        categoryFilter: String = "All"
    ): HomeFeedData {
        val timeOfDay = profile.currentTimeOfDay
        val isFresh = profile.totalPlays == 0 && profile.playHistory.isEmpty()

        val rawSections = mutableListOf<HomeSection>()

        // 1. Speed Dial Section (Quick 1-touch tiles)
        val speedDialTracks = RecommendationEngine.getSpeedDialTracks(profile)
        if (speedDialTracks.isNotEmpty()) {
            rawSections.add(
                HomeSection(
                    id = "sec_speed_dial",
                    title = "Speed Dial",
                    subtitle = "Instant 1-touch jump into your frequent loops",
                    type = HomeSectionType.SPEED_DIAL,
                    priorityScore = if (isFresh) 0.3f else 0.95f,
                    items = speedDialTracks
                )
            )
        }

        // 2. Continue Listening Section
        val continueListening = RecommendationEngine.getContinueListeningTracks(profile)
        if (continueListening.isNotEmpty() && !isFresh) {
            val lastSong = profile.lastPlayedSong ?: continueListening.first()
            rawSections.add(
                HomeSection(
                    id = "sec_continue",
                    title = "Continue Listening",
                    subtitle = "Pick up right where you left off",
                    rationaleBadge = "Resume Track",
                    type = HomeSectionType.CONTINUE_LISTENING,
                    priorityScore = 0.90f,
                    items = continueListening
                )
            )
        }

        // 3. Your Choices / Made For You
        val yourChoices = RecommendationEngine.getYourChoicesTracks(profile)
        rawSections.add(
            HomeSection(
                id = "sec_your_choices",
                title = "Your Choices (Made For You)",
                subtitle = "Algorithmic match based on your listening signals",
                rationaleBadge = "${((profile.topGenres.values.firstOrNull() ?: 0.8f) * 100).toInt()}% Affinity Match",
                type = HomeSectionType.YOUR_CHOICES,
                priorityScore = 0.85f,
                items = yourChoices
            )
        )

        // 4. Because You Listened To...
        if (profile.lastPlayedSong != null) {
            val (rationale, songList) = RecommendationEngine.getBecauseYouListenedTo(profile)
            if (songList.isNotEmpty()) {
                rawSections.add(
                    HomeSection(
                        id = "sec_because_you_listened",
                        title = rationale,
                        subtitle = "Related tracks from connected genres and artists",
                        rationaleBadge = "Taste Signal",
                        type = HomeSectionType.BECAUSE_YOU_LISTENED_TO,
                        priorityScore = 0.80f,
                        items = songList
                    )
                )
            }
        }

        // 5. Time of Day / Mood Picks
        val moodItems = generateTimeOfDayMoodPicks(timeOfDay)
        rawSections.add(
            HomeSection(
                id = "sec_mood_picks",
                title = "${timeOfDay.greeting} Mood Picks",
                subtitle = "Curated soundscapes suited for ${timeOfDay.displayName.lowercase()}",
                rationaleBadge = timeOfDay.displayName,
                type = HomeSectionType.MOOD_PICKS,
                priorityScore = 0.75f,
                items = moodItems
            )
        )

        // 6. More Like This
        val (moreRationale, moreTracks) = RecommendationEngine.getMoreLikeThisTracks(profile)
        rawSections.add(
            HomeSection(
                id = "sec_more_like_this",
                title = moreRationale,
                subtitle = "Deep cut recommendations in your favorite genre",
                type = HomeSectionType.MORE_LIKE_THIS,
                priorityScore = 0.70f,
                items = moreTracks
            )
        )

        // 7. Recommended Artists
        val recArtists = RecommendationEngine.getRecommendedArtists(profile)
        rawSections.add(
            HomeSection(
                id = "sec_recommended_artists",
                title = "Recommended Artists",
                subtitle = "Creators matching your audio preferences",
                type = HomeSectionType.RECOMMENDED_ARTISTS,
                priorityScore = 0.65f,
                items = recArtists
            )
        )

        // 8. Recommended Albums
        val recAlbums = RecommendationEngine.getRecommendedAlbums(profile)
        rawSections.add(
            HomeSection(
                id = "sec_recommended_albums",
                title = "Recommended Albums",
                subtitle = "Full high-fidelity records handpicked for you",
                type = HomeSectionType.RECOMMENDED_ALBUMS,
                priorityScore = 0.60f,
                items = recAlbums
            )
        )

        // 9. Discover Something New
        val discoverNew = RecommendationEngine.getDiscoverNewTracks(profile)
        rawSections.add(
            HomeSection(
                id = "sec_discover_new",
                title = "Discover Something New ⚡",
                subtitle = "Expand your horizon with fresh lossless discoveries",
                rationaleBadge = "Fresh Discovery",
                type = HomeSectionType.DISCOVER_NEW,
                priorityScore = 0.55f,
                items = discoverNew
            )
        )

        // 10. Trending Now
        rawSections.add(
            HomeSection(
                id = "sec_trending",
                title = "Trending Now",
                subtitle = "Top global streams across the 100% free catalog",
                type = HomeSectionType.TRENDING_NOW,
                priorityScore = 0.50f,
                items = MusicCatalog.ALL_SONGS.reversed()
            )
        )

        // Dynamic Section Ranking Strategy based on User History and Category Filter
        val sortedSections = rawSections
            .sortedByDescending { it.priorityScore }
            .filter { section ->
                when (categoryFilter) {
                    "All" -> true
                    "Made For You" -> section.type in listOf(HomeSectionType.SPEED_DIAL, HomeSectionType.YOUR_CHOICES, HomeSectionType.BECAUSE_YOU_LISTENED_TO, HomeSectionType.CONTINUE_LISTENING)
                    "Chill" -> section.type in listOf(HomeSectionType.MOOD_PICKS, HomeSectionType.MORE_LIKE_THIS, HomeSectionType.DISCOVER_NEW)
                    "Energy" -> section.type in listOf(HomeSectionType.SPEED_DIAL, HomeSectionType.TRENDING_NOW, HomeSectionType.YOUR_CHOICES)
                    "Focus" -> section.type in listOf(HomeSectionType.MOOD_PICKS, HomeSectionType.BECAUSE_YOU_LISTENED_TO, HomeSectionType.RECOMMENDED_ALBUMS)
                    else -> true
                }
            }

        // Top Vibe summary formatted text
        val topGenre = profile.topGenres.entries.firstOrNull()?.key ?: "Synthwave"
        val topGenreScore = ((profile.topGenres.entries.firstOrNull() ?: mapOf("A" to 0.8f).entries.first()).value * 100).toInt()
        val secGenre = profile.topGenres.entries.drop(1).firstOrNull()?.key ?: "Ambient"
        val topVibeText = "$topGenreScore% $topGenre • $secGenre Flow"

        return HomeFeedData(
            sections = sortedSections,
            activeCategoryFilter = categoryFilter,
            isLoading = false,
            isFreshUser = isFresh,
            topVibeSummary = topVibeText,
            timeOfDayGreeting = "${timeOfDay.greeting}, Cyber Listener",
            timeOfDaySubtext = "${timeOfDay.defaultMood} • 100% Free"
        )
    }

    private fun generateTimeOfDayMoodPicks(timeOfDay: TimeOfDay): List<MoodPickItem> {
        return when (timeOfDay) {
            TimeOfDay.MORNING -> listOf(
                MoodPickItem("m_1", "Sunrise Focus Drone", "Soothing generative arpeggios", "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=500", "Ambient"),
                MoodPickItem("m_2", "Acoustic Morning Zen", "Warm organic guitar & vinyl", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500", "Chill"),
                MoodPickItem("m_3", "Clean Code Coffee", "Minimalist electronic beats", "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=500", "Lo-Fi")
            )
            TimeOfDay.AFTERNOON -> listOf(
                MoodPickItem("m_4", "Cyber Workout 140+ BPM", "High-energy synthwave drops", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500", "Synthwave"),
                MoodPickItem("m_5", "Deep Focus Flow", "Subtle analog bass loops for productivity", "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=500", "Ambient"),
                MoodPickItem("m_6", "Neon City Pulse", "Upbeat electro groove", "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=500", "Electro House")
            )
            TimeOfDay.EVENING -> listOf(
                MoodPickItem("m_7", "Night Drive Lossless", "Atmospheric synthwave & ambient beats", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500", "Synthwave"),
                MoodPickItem("m_8", "Neon Rain Tokyo", "Ethereal synth chords & rain crackles", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500", "Ambient"),
                MoodPickItem("m_9", "Sunset Lounge", "Smooth downtempo electronic", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500", "Chill")
            )
            TimeOfDay.LATE_NIGHT -> listOf(
                MoodPickItem("m_10", "Zero Gravity Meditation", "Deep space drones for rest & focus", "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=500", "Ambient"),
                MoodPickItem("m_11", "Midnight Cyberpunk Drive", "Dark analog basslines under city neon", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500", "Cyberpunk"),
                MoodPickItem("m_12", "Late Night Vinyl Lofi", "Dusty Rhodes & rain sounds", "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=500", "Lo-Fi")
            )
        }
    }
}
