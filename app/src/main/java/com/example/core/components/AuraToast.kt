package com.example.core.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass

enum class AuraToastType {
    INFO,
    SUCCESS,
    WARNING
}

@Composable
fun AuraToastBanner(
    message: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
    type: AuraToastType = AuraToastType.INFO,
    testTag: String = "aura_toast_banner"
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        val (icon, tint) = when (type) {
            AuraToastType.SUCCESS -> Icons.Default.CheckCircle to AuraColors.SuccessGreen
            AuraToastType.WARNING -> Icons.Default.Warning to AuraColors.WarningOrange
            AuraToastType.INFO -> Icons.Default.Info to AuraColors.NeonCyan
        }

        Row(
            modifier = Modifier
                .testTag(testTag)
                .fillMaxWidth()
                .auraGlass(shape = RoundedCornerShape(AuraRadius.Medium))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
