package com.example.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass

enum class AuraButtonVariant {
    PRIMARY_GRADIENT,
    GLASS,
    SECONDARY,
    OUTLINED,
    GHOST
}

@Composable
fun AuraButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AuraButtonVariant = AuraButtonVariant.PRIMARY_GRADIENT,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "aura_button"
) {
    val shape: Shape = RoundedCornerShape(AuraRadius.Pill)
    val interactionSource = remember { MutableInteractionSource() }

    val bgModifier = when (variant) {
        AuraButtonVariant.PRIMARY_GRADIENT -> {
            Modifier.background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        AuraColors.ElectricPurple,
                        AuraColors.NeonCyan
                    )
                ),
                shape = shape
            )
        }
        AuraButtonVariant.GLASS -> {
            Modifier.auraGlass(shape = shape)
        }
        AuraButtonVariant.SECONDARY -> {
            Modifier.background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = shape
            )
        }
        AuraButtonVariant.OUTLINED -> {
            Modifier
                .background(Color.Transparent, shape)
                .border(1.5.dp, AuraColors.ElectricPurple, shape)
        }
        AuraButtonVariant.GHOST -> {
            Modifier.background(Color.Transparent, shape)
        }
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .height(48.dp)
            .clip(shape)
            .then(bgModifier)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                onClick = onClick
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (variant == AuraButtonVariant.PRIMARY_GRADIENT) Color.Black else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 6.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (variant == AuraButtonVariant.PRIMARY_GRADIENT) Color.Black else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AuraIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    isGlass: Boolean = true,
    testTag: String = "aura_icon_button"
) {
    val shape = RoundedCornerShape(AuraRadius.Pill)
    val glassMod = if (isGlass) Modifier.auraGlass(shape = shape) else Modifier

    Box(
        modifier = modifier
            .testTag(testTag)
            .size(size)
            .clip(shape)
            .then(glassMod)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = AuraColors.NeonCyan),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}
