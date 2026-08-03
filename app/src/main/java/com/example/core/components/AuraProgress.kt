package com.example.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.AuraColors

@Composable
fun AuraCircularProgress(
    modifier: Modifier = Modifier,
    color: Color = AuraColors.ElectricPurple,
    testTag: String = "aura_circular_progress"
) {
    CircularProgressIndicator(
        modifier = modifier.testTag(testTag),
        color = color,
        trackColor = color.copy(alpha = 0.2f)
    )
}

@Composable
fun AuraLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = AuraColors.NeonCyan,
    testTag: String = "aura_linear_progress"
) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .testTag(testTag)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp)),
        color = color,
        trackColor = color.copy(alpha = 0.2f)
    )
}

@Composable
fun AuraEqualizerWave(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 4,
    color: Color = AuraColors.NeonCyan,
    testTag: String = "aura_equalizer_wave"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    Row(
        modifier = modifier.testTag(testTag),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(barCount) { index ->
            val scale by if (isPlaying) {
                infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 300 + index * 120, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bar_scale_$index"
                )
            } else {
                remember { mutableStateOf(0.3f) }
            }

            val height = (8 + (16 * scale)).dp

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(color)
            )
        }
    }
}
