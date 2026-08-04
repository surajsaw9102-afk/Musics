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
import com.example.core.state.*

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
    var showStorageDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showPersonalizationDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showExportDataDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .testTag(testTag)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AuraSectionHeader(
            title = "App Settings",
            subtitle = "Complete control over appearance, audio & playback"
        )

        // 1. Appearance & Theme
        SettingsGroupTitle("Appearance & Visual Theme")
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
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsClickableRow(
                icon = Icons.Default.ColorLens,
                title = "Accent Color Selection",
                subtitle = settings.accentColor.displayName,
                onClick = { showThemeDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsToggleRow(
                icon = Icons.Default.MotionPhotosOff,
                title = "Reduced Motion Preference",
                subtitle = "Disable heavy blur and complex animations",
                checked = settings.isReducedMotionEnabled,
                onCheckedChange = settingsState::toggleReducedMotion
            )
        }

        // 2. Playback Preferences
        SettingsGroupTitle("Playback Controls")
        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingsToggleRow(
                icon = Icons.Default.Tune,
                title = "Crossfade Transitions",
                subtitle = "Smooth crossfade between tracks (${settings.crossfadeSeconds}s)",
                checked = settings.isCrossfadeEnabled,
                onCheckedChange = settingsState::toggleCrossfade
            )

            if (settings.isCrossfadeEnabled) {
                Column(modifier = Modifier.padding(start = 34.dp, end = 8.dp, top = 4.dp, bottom = 8.dp)) {
                    Text(
                        text = "Crossfade Duration: ${settings.crossfadeSeconds} Seconds",
                        style = MaterialTheme.typography.labelSmall,
                        color = AuraColors.NeonCyan
                    )
                    Slider(
                        value = settings.crossfadeSeconds.toFloat(),
                        onValueChange = { settingsState.setCrossfadeDuration(it.toInt()) },
                        valueRange = 0f..12f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = AuraColors.NeonCyan,
                            activeTrackColor = AuraColors.ElectricPurple
                        )
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsToggleRow(
                icon = Icons.Default.GraphicEq,
                title = "Gapless Playback",
                subtitle = "Eliminate silent gaps between album tracks",
                checked = settings.isGaplessEnabled,
                onCheckedChange = settingsState::toggleGapless
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsToggleRow(
                icon = Icons.Default.VolumeUp,
                title = "Normalize Volume Level",
                subtitle = "Set consistent audio output across all songs",
                checked = settings.isNormalizeVolumeEnabled,
                onCheckedChange = settingsState::toggleNormalizeVolume
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsToggleRow(
                icon = Icons.Default.Autorenew,
                title = "Autoplay Similar Music",
                subtitle = "Keep listening when your queue finishes",
                checked = settings.isAutoplayEnabled,
                onCheckedChange = settingsState::toggleAutoplay
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsToggleRow(
                icon = Icons.Default.Explicit,
                title = "Explicit Content Filter",
                subtitle = "Filter explicit songs and lyrics",
                checked = settings.isExplicitFilterEnabled,
                onCheckedChange = settingsState::toggleExplicitFilter
            )
        }

        // 3. Audio & Quality
        SettingsGroupTitle("Audio Quality & Soundstage")
        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingsClickableRow(
                icon = Icons.Default.HighQuality,
                title = "Streaming Audio Quality",
                subtitle = settings.streamingQuality,
                onClick = { showAudioDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsClickableRow(
                icon = Icons.Default.SurroundSound,
                title = "Audio Output Mode",
                subtitle = "${settings.audioOutputMode.title} • ${settings.audioOutputMode.description}",
                onClick = { showAudioDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsToggleRow(
                icon = Icons.Default.Wifi,
                title = "Download Over Wi-Fi Only",
                subtitle = "Prevent downloading music on mobile data",
                checked = settings.isDownloadOverWifiOnly,
                onCheckedChange = settingsState::toggleWifiOnlyDownloads
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsToggleRow(
                icon = Icons.Default.DataSaverOn,
                title = "Data Saver Mode",
                subtitle = "Compress stream bandwidth over cellular networks",
                checked = settings.isDataSaverEnabled,
                onCheckedChange = settingsState::toggleDataSaver
            )
        }

        // 4. Personalization Controls
        SettingsGroupTitle("Personalization & Discovery")
        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingsClickableRow(
                icon = Icons.Default.Tune,
                title = "Personalization Preferences",
                subtitle = "Startup screen, discovery style & mood filters",
                onClick = { showPersonalizationDialog = true }
            )
        }

        // 5. Library, History & Storage
        SettingsGroupTitle("Library, History & Storage")
        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingsClickableRow(
                icon = Icons.Default.History,
                title = "History Management",
                subtitle = "Manage search and play history items",
                onClick = { showHistoryDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsClickableRow(
                icon = Icons.Default.Storage,
                title = "Storage & Cleanup Dashboard",
                subtitle = "${settings.cacheSizeMb} MB cached • ${settings.downloadSizeMb} MB downloads",
                onClick = { showStorageDialog = true }
            )
        }

        // 6. Privacy & Local Data
        SettingsGroupTitle("Privacy & Local Data Controls")
        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingsClickableRow(
                icon = Icons.Default.Visibility,
                title = "Listening History Visibility",
                subtitle = settings.historyVisibility.title,
                onClick = {}
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsClickableRow(
                icon = Icons.Default.Download,
                title = "Export Local App Data",
                subtitle = "Export settings, favorites and history as JSON",
                onClick = { showExportDataDialog = true }
            )
        }

        // 7. App Info & Support
        SettingsGroupTitle("App Info & Support")
        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingsClickableRow(
                icon = Icons.Default.Info,
                title = "About Aura Music Stream",
                subtitle = "v3.2.0-AURA • 100% Free Lifetime Music Experience",
                onClick = { showLicensesDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsClickableRow(
                icon = Icons.Default.Code,
                title = "Open-Source Licenses",
                subtitle = "View third-party software disclosures",
                onClick = { showLicensesDialog = true }
            )
        }
    }

    // Dialogs
    if (showThemeDialog) {
        AuraGlassDialog(
            onDismissRequest = { showThemeDialog = false },
            title = "Choose Theme & Accent"
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

                Spacer(modifier = Modifier.height(8.dp))
                Text("Select Accent Color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                AuraAccentColor.values().forEach { accent ->
                    AuraChip(
                        label = accent.displayName,
                        isSelected = settings.accentColor == accent,
                        onClick = {
                            settingsState.setAccentColor(accent)
                            showThemeDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showAudioDialog) {
        AuraGlassDialog(
            onDismissRequest = { showAudioDialog = false },
            title = "Audio Quality & Spatial Settings"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Streaming Quality (100% Free)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                listOf("Lossless FLAC (24-bit/96kHz)", "High Quality (320 kbps AAC)", "Normal (160 kbps)").forEach { quality ->
                    AuraChip(
                        label = quality,
                        isSelected = settings.streamingQuality == quality,
                        onClick = {
                            settingsState.updateStreamingQuality(quality)
                            showAudioDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Spatial Output Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                AudioOutputMode.values().forEach { mode ->
                    AuraChip(
                        label = "${mode.title} — ${mode.description}",
                        isSelected = settings.audioOutputMode == mode,
                        onClick = {
                            settingsState.setAudioOutputMode(mode)
                            showAudioDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showStorageDialog) {
        StorageDashboardDialog(onDismissRequest = { showStorageDialog = false })
    }

    if (showHistoryDialog) {
        HistoryManagerDialog(onDismissRequest = { showHistoryDialog = false })
    }

    if (showPersonalizationDialog) {
        PersonalizationDialog(onDismissRequest = { showPersonalizationDialog = false })
    }

    if (showLicensesDialog) {
        OpenSourceLicensesDialog(onDismissRequest = { showLicensesDialog = false })
    }

    if (showExportDataDialog) {
        AuraGlassDialog(
            onDismissRequest = { showExportDataDialog = false },
            title = "Export Local App Data"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Local backup generated successfully:",
                    style = MaterialTheme.typography.bodySmall,
                    color = AuraColors.NeonCyan
                )

                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "{\n  \"app\": \"Aura Music Stream\",\n  \"user\": \"Alex Vance\",\n  \"saved_tracks\": 1420,\n  \"playlists\": 8,\n  \"version\": \"3.2.0\"\n}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                AuraButton(
                    text = "Close",
                    onClick = { showExportDataDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    variant = AuraButtonVariant.PRIMARY_GRADIENT
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
        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
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
