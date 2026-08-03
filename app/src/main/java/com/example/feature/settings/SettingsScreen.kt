package com.example.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.components.*
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraThemeMode
import com.example.core.state.SettingsState
import com.example.core.state.ThemeState

@Composable
fun SettingsScreen(
    themeState: ThemeState,
    settingsState: SettingsState = viewModel(),
    modifier: Modifier = Modifier,
    testTag: String = "settings_screen"
) {
    val themeMode by themeState.themeMode.collectAsState()
    val settings by settingsState.settings.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .testTag(testTag)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        AuraSectionHeader(
            title = "App Settings",
            subtitle = "Customize appearance, audio & notifications"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Appearance Section
        SettingsGroupTitle("Appearance & Theme")

        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingsClickableRow(
                icon = Icons.Default.Palette,
                title = "Theme Preference",
                subtitle = when (themeMode) {
                    AuraThemeMode.DARK -> "Dark Theme (Default)"
                    AuraThemeMode.LIGHT -> "Light Theme"
                    AuraThemeMode.SYSTEM -> "System Theme"
                },
                onClick = { showThemeDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Language & Region
        SettingsGroupTitle("Language & Region")

        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingsClickableRow(
                icon = Icons.Default.Language,
                title = "App Language",
                subtitle = settings.selectedLanguage,
                onClick = {}
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsClickableRow(
                icon = Icons.Default.Public,
                title = "Country / Region",
                subtitle = settings.selectedCountry,
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Audio & Playback
        SettingsGroupTitle("Audio Quality & Playback")

        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingsClickableRow(
                icon = Icons.Default.GraphicEq,
                title = "Streaming Audio Quality",
                subtitle = "${settings.audioQuality} • 100% Free",
                onClick = { showAudioDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsClickableRow(
                icon = Icons.Default.Equalizer,
                title = "Equalizer Preset",
                subtitle = "Bass Boosted & Crystal Clear",
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Notifications & Data
        SettingsGroupTitle("Notifications & Privacy")

        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingsToggleRow(
                icon = Icons.Default.Notifications,
                title = "New Release Notifications",
                subtitle = "Get notified when favorite artists release music",
                checked = settings.isPushNotificationsEnabled,
                onCheckedChange = settingsState::toggleNotifications
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsToggleRow(
                icon = Icons.Default.DataSaverOn,
                title = "Data Saver Mode",
                subtitle = "Optimize stream quality over cellular networks",
                checked = settings.isDataSaverEnabled,
                onCheckedChange = settingsState::toggleDataSaver
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Storage & Cache
        SettingsGroupTitle("Storage & Cache")

        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "App Cache Size",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${settings.cacheSizeMb} MB cached album art & metadata",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AuraButton(
                    text = "Clear Cache",
                    onClick = settingsState::clearCache,
                    variant = AuraButtonVariant.OUTLINED
                )
            }
        }
    }

    // Theme Picker Dialog
    if (showThemeDialog) {
        AuraGlassDialog(
            onDismissRequest = { showThemeDialog = false },
            title = "Choose Theme Mode"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AuraChip(
                    label = "Dark Theme (Default)",
                    isSelected = themeMode == AuraThemeMode.DARK,
                    onClick = {
                        themeState.setThemeMode(AuraThemeMode.DARK)
                        showThemeDialog = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                AuraChip(
                    label = "Light Theme",
                    isSelected = themeMode == AuraThemeMode.LIGHT,
                    onClick = {
                        themeState.setThemeMode(AuraThemeMode.LIGHT)
                        showThemeDialog = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                AuraChip(
                    label = "System Theme",
                    isSelected = themeMode == AuraThemeMode.SYSTEM,
                    onClick = {
                        themeState.setThemeMode(AuraThemeMode.SYSTEM)
                        showThemeDialog = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Audio Quality Dialog
    if (showAudioDialog) {
        AuraGlassDialog(
            onDismissRequest = { showAudioDialog = false },
            title = "Audio Quality (100% Free)"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AuraChip(
                    label = "Lossless FLAC (24-bit/96kHz) - Best",
                    isSelected = settings.audioQuality.contains("FLAC"),
                    onClick = {
                        settingsState.updateAudioQuality("Lossless FLAC (Hi-Res)")
                        showAudioDialog = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                AuraChip(
                    label = "High Quality (320 kbps AAC)",
                    isSelected = settings.audioQuality.contains("320"),
                    onClick = {
                        settingsState.updateAudioQuality("High Quality (320 kbps)")
                        showAudioDialog = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SettingsGroupTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = AuraColors.NeonCyan,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AuraColors.ElectricPurple,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AuraColors.ElectricPurple,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AuraColors.ElectricPurple
            )
        )
    }
}
