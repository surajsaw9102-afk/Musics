package com.example.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun AuraMiniPlayerBar(
    songTitle: String,
    artistName: String,
    coverUrl: String,
    isPlaying: Boolean,
    progressFraction: Float = 0f,
    onPlayPauseClick: () -> Unit,
    onSkipClick: () -> Unit,
    onBarClick: () -> Unit,
    onPreviousClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    testTag: String = "aura_mini_player_bar"
) {
    val shape = RoundedCornerShape(topStart = AuraRadius.Large, topEnd = AuraRadius.Large)

    // Animated artwork rotation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "mini_artwork")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mini_rotation"
    )

    Column(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .auraGlass(shape = shape)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -30) {
                        onSkipClick()
                    } else if (dragAmount > 30) {
                        onPreviousClick?.invoke()
                    }
                }
            }
            .clickable(onClick = onBarClick)
    ) {
        // Real Progress indicator top line
        LinearProgressIndicator(
            progress = { progressFraction.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp),
            color = AuraColors.NeonCyan,
            trackColor = AuraColors.DarkSurface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(AuraRadius.Small))
                    .background(AuraColors.DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = songTitle,
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(if (isPlaying) rotation else 0f),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = songTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (isPlaying) {
                        AuraEqualizerWave(isPlaying = true, barCount = 4)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    AuraHdBadge()
                }
                Text(
                    text = artistName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            AuraIconButton(
                icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                onClick = onPlayPauseClick,
                size = 38.dp,
                tint = AuraColors.ElectricPurple
            )

            Spacer(modifier = Modifier.width(6.dp))

            AuraIconButton(
                icon = Icons.Default.SkipNext,
                contentDescription = "Next Track",
                onClick = onSkipClick,
                size = 38.dp,
                isGlass = false
            )
        }
    }
}
