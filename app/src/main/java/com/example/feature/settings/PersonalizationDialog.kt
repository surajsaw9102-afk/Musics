package com.example.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.components.AuraButton
import com.example.core.components.AuraButtonVariant
import com.example.core.components.AuraChip
import com.example.core.components.AuraGlassDialog
import com.example.core.designsystem.AuraColors
import com.example.core.state.*

@Composable
fun PersonalizationDialog(
    onDismissRequest: () -> Unit
) {
    val prefs by PreferencesManager.personalization.collectAsState()

    AuraGlassDialog(
        onDismissRequest = onDismissRequest,
        title = "Personalization Controls"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Default Startup Screen
            Text(
                text = "Default Startup Screen",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AuraColors.NeonCyan
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                StartupScreen.values().forEach { screen ->
                    AuraChip(
                        label = screen.routeTitle,
                        isSelected = prefs.startupScreen == screen,
                        onClick = { PreferencesManager.setStartupScreen(screen) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Preferred Discovery Style
            Text(
                text = "Discovery Recommendation Style",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AuraColors.NeonCyan
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DiscoveryStyle.values().forEach { style ->
                    AuraChip(
                        label = "${style.title} — ${style.description}",
                        isSelected = prefs.discoveryStyle == style,
                        onClick = { PreferencesManager.setDiscoveryStyle(style) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Explicit Content Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Allow Explicit Content",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Filter or allow tracks marked with explicit lyrics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = prefs.explicitContentAllowed,
                    onCheckedChange = PreferencesManager::toggleExplicitContent,
                    colors = SwitchDefaults.colors(checkedTrackColor = AuraColors.ElectricPurple)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AuraButton(
                text = "Save Preferences",
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth(),
                variant = AuraButtonVariant.PRIMARY_GRADIENT
            )
        }
    }
}
