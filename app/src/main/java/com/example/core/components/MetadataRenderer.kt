package com.example.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass

object MetadataRenderer {

    fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "--:--"
        val totalSec = durationMs / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format("%d:%02d", min, sec)
    }

    fun formatMonthlyListeners(count: Long): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM Monthly Listeners", count / 1_000_000f)
            count >= 1_000 -> String.format("%dK Monthly Listeners", count / 1000)
            count > 0 -> "$count Monthly Listeners"
            else -> "Growing Artist"
        }
    }

    fun getFallbackBio(artistName: String): String {
        return "$artistName is an featured artist on Aura. Explore their top tracks, singles, and full albums below."
    }
}

@Composable
fun AudioQualityBadge(
    qualityText: String?,
    modifier: Modifier = Modifier
) {
    val text = qualityText ?: "Lossless FLAC 24-bit"
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AuraRadius.Pill))
            .background(AuraColors.NeonCyan.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = "Audio Quality",
                tint = AuraColors.NeonCyan,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = AuraColors.NeonCyan,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ExplicitBadge(
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Default.Explicit,
        contentDescription = "Explicit Content",
        tint = AuraColors.MagentaPulse,
        modifier = modifier.size(16.dp)
    )
}

@Composable
fun LyricsAvailableBadge(
    isAvailable: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (!isAvailable) return
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AuraRadius.Pill))
            .background(AuraColors.ElectricPurple.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lyrics,
                contentDescription = "Lyrics Available",
                tint = AuraColors.ElectricPurple,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "LYRICS",
                style = MaterialTheme.typography.labelSmall,
                color = AuraColors.ElectricPurple,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PlaceholderArtworkCard(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AuraRadius.Medium))
            .auraGlass(shape = RoundedCornerShape(AuraRadius.Medium))
            .background(AuraColors.DarkSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = AuraColors.NeonCyan,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
