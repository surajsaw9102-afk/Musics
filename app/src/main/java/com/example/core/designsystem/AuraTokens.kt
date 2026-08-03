package com.example.core.designsystem

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AuraSpacing {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
    val xxl: Dp = 48.dp
    val xxxl: Dp = 64.dp
}

object AuraRadius {
    val Small: Dp = 8.dp
    val Medium: Dp = 16.dp
    val Large: Dp = 24.dp
    val ExtraLarge: Dp = 32.dp
    val Pill: Dp = 100.dp
}

object AuraElevation {
    val None: Dp = 0.dp
    val Low: Dp = 2.dp
    val Medium: Dp = 6.dp
    val High: Dp = 12.dp
    val Glass: Dp = 16.dp
}

object AuraMotion {
    const val FastMillis = 150
    const val MediumMillis = 250
    const val SlowMillis = 400

    fun <T> auraSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
}

enum class WindowSizeClass {
    COMPACT, // Phone portrait (< 600dp)
    MEDIUM,  // Tablet portrait / Foldable (600dp - 840dp)
    EXPANDED // Tablet landscape / Desktop (> 840dp)
}
