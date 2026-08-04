package com.example.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.components.AuraButton
import com.example.core.components.AuraButtonVariant
import com.example.core.components.AuraFreeBadge
import com.example.core.components.AuraGlassDialog
import com.example.core.designsystem.AuraColors

@Composable
fun OpenSourceLicensesDialog(
    onDismissRequest: () -> Unit
) {
    AuraGlassDialog(
        onDismissRequest = onDismissRequest,
        title = "App Info & Open-Source Licenses"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Aura Music Stream v3.2.0-AURA",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AuraColors.NeonCyan
            )
            Text(
                text = "Build #2026.08.03 • 100% Free Lifetime Music Experience",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AuraFreeBadge()

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            Text(
                text = "Open Source Libraries",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            LicenseRow("Jetpack Compose & Material 3", "Apache License 2.0 • Google LLC")
            LicenseRow("Kotlin Coroutines & Flow", "Apache License 2.0 • JetBrains")
            LicenseRow("AndroidX Room Database", "Apache License 2.0 • Google LLC")
            LicenseRow("ExoPlayer / Media3", "Apache License 2.0 • Google LLC")
            LicenseRow("Coil Image Loader", "Apache License 2.0 • Coil Contributors")
            LicenseRow("Retrofit & OkHttp", "Apache License 2.0 • Square, Inc.")

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            Text(
                text = "Privacy & Data Policy",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Aura Music Stream stores all user preferences, listening history, downloads, and cached audio data strictly on device local storage. No advertising identifiers or personal data are collected or sold.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            AuraButton(
                text = "Close",
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth(),
                variant = AuraButtonVariant.PRIMARY_GRADIENT
            )
        }
    }
}

@Composable
private fun LicenseRow(libraryName: String, details: String) {
    Column {
        Text(
            text = libraryName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = details,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
