package com.example.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.auraGlass(
    shape: Shape = RoundedCornerShape(AuraRadius.Medium),
    borderWidth: Dp = 1.dp,
    alpha: Float = 0.15f
): Modifier {
    val isDark = MaterialTheme.colorScheme.background == AuraColors.DarkBackground
    val glassBg = if (isDark) {
        AuraColors.DarkSurfaceVariant.copy(alpha = alpha)
    } else {
        AuraColors.LightSurface.copy(alpha = 0.7f)
    }

    val borderBrush = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                AuraColors.ElectricPurple.copy(alpha = 0.5f),
                AuraColors.NeonCyan.copy(alpha = 0.2f),
                Color.Transparent
            )
        } else {
            listOf(
                AuraColors.ElectricPurple.copy(alpha = 0.3f),
                AuraColors.LightSurfaceVariant,
                Color.Transparent
            )
        }
    )

    return this
        .clip(shape)
        .background(glassBg, shape)
        .border(borderWidth, borderBrush, shape)
}

fun Modifier.auraAmbientGlow(
    color: Color = AuraColors.ElectricPurple,
    alpha: Float = 0.25f
): Modifier = this.drawBehind {
    drawRadialGlow(color = color, alpha = alpha)
}

private fun DrawScope.drawRadialGlow(color: Color, alpha: Float) {
    val radius = size.maxDimension / 1.5f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha),
                color.copy(alpha = alpha * 0.4f),
                Color.Transparent
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}
