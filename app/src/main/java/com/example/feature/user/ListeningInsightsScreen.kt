package com.example.feature.user

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.components.AuraGlassCard
import com.example.core.components.AuraSectionHeader
import com.example.core.designsystem.AuraColors
import com.example.core.repository.DailyListeningData
import com.example.core.repository.GenreListeningData
import com.example.core.repository.ListeningStatistics
import com.example.core.repository.StatisticsRepository

@Composable
fun ListeningInsightsScreen(
    testTag: String = "listening_insights_screen"
) {
    val stats by StatisticsRepository.stats.collectAsState()

    Column(
        modifier = Modifier
            .testTag(testTag)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        AuraSectionHeader(
            title = "Listening Insights",
            subtitle = "Your personalized music trends & stats"
        )

        // Summary Stats Grid
        StatsGrid(stats = stats)

        // Active Listening Hours Card
        ActiveHoursCard(stats = stats)

        // Weekly Activity Chart
        WeeklyActivityCard(weeklyData = stats.weeklyActivity)

        // Top Genres Breakdown
        TopGenresCard(topGenres = stats.topGenres)

        // Top Artists
        TopArtistsSection(stats = stats)

        // Favorite Mood Categories
        FavoriteMoodsCard()
    }
}

@Composable
private fun StatsGrid(stats: ListeningStatistics) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Streak",
            value = "${stats.activeListeningStreakDays} Days",
            icon = Icons.Default.LocalFireDepartment,
            accentColor = AuraColors.AmberGlow
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Listening Time",
            value = "${stats.totalHours} Hrs",
            icon = Icons.Default.Schedule,
            accentColor = AuraColors.NeonCyan
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Songs Played",
            value = "${stats.songsPlayedCount}",
            icon = Icons.Default.MusicNote,
            accentColor = AuraColors.ElectricPurple
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Albums",
            value = "${stats.albumsPlayedCount}",
            icon = Icons.Default.Album,
            accentColor = AuraColors.EmeraldPulse
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color
) {
    AuraGlassCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActiveHoursCard(stats: ListeningStatistics) {
    AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AuraColors.ElectricPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NightsStay,
                    contentDescription = null,
                    tint = AuraColors.ElectricPurple,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Most Active Listening Hours",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stats.peakListeningTime,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AuraColors.NeonCyan
                )
            }
        }
    }
}

@Composable
private fun WeeklyActivityCard(weeklyData: List<DailyListeningData>) {
    val maxHours = weeklyData.maxOfOrNull { it.hours } ?: 8.0f

    AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Weekly Activity (Hours / Day)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            weeklyData.forEach { day ->
                val ratio = (day.hours / maxHours).coerceIn(0.1f, 1.0f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(
                        text = "${day.hours}h",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .fillMaxHeight(ratio)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                if (ratio > 0.7f) AuraColors.NeonCyan else AuraColors.ElectricPurple.copy(alpha = 0.7f)
                            )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = day.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun TopGenresCard(topGenres: List<GenreListeningData>) {
    AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Most Played Genres",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(14.dp))

        topGenres.forEach { genre ->
            val progressAnimated by animateFloatAsState(targetValue = genre.percentage, label = "genre_progress")

            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = genre.genreName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(genre.percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = AuraColors.NeonCyan
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progressAnimated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = when (genre.genreName) {
                        "Synthwave" -> AuraColors.NeonCyan
                        "Cyberpunk" -> AuraColors.ElectricPurple
                        "Ambient & Lofi" -> AuraColors.EmeraldPulse
                        else -> AuraColors.AmberGlow
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TopArtistsSection(stats: ListeningStatistics) {
    AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Your Top Artists",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        stats.topArtists.forEachIndexed { index, pair ->
            val artist = pair.first
            val streams = pair.second

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AuraColors.AmberGlow,
                    modifier = Modifier.width(32.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = artist.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${artist.monthlyListeners / 1000}k Monthly Listeners",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$streams plays",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AuraColors.NeonCyan
                )
            }
        }
    }
}

@Composable
private fun FavoriteMoodsCard() {
    val moods = listOf(
        Pair("Focus & Code", Icons.Default.Code),
        Pair("Late Night Cyber", Icons.Default.NightsStay),
        Pair("Chill & Lofi", Icons.Default.Spa),
        Pair("High Energy Workout", Icons.Default.FitnessCenter),
        Pair("Deep Meditation", Icons.Default.SelfImprovement)
    )

    AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Favorite Mood Categories",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(moods) { mood ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AuraColors.ElectricPurple.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraColors.NeonCyan.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = mood.second,
                            contentDescription = null,
                            tint = AuraColors.NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = mood.first,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
