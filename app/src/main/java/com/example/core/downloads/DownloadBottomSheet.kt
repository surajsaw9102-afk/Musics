package com.example.core.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.components.AuraButton
import com.example.core.components.AuraButtonVariant
import com.example.core.database.entities.SongEntity
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.provider.DownloadQuality
import com.example.core.storage.StorageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadBottomSheet(
    title: String,
    songs: List<SongEntity>,
    onDismissRequest: () -> Unit,
    onConfirmDownload: (DownloadQuality, Boolean) -> Unit
) {
    var selectedQuality by remember { mutableStateOf(DownloadManager.state.value.downloadQuality) }
    var wifiOnly by remember { mutableStateOf(DownloadManager.state.value.wifiOnlyDownloads) }

    val stats by StorageManager.stats.collectAsState()
    val totalEstimatedMb = songs.size * when (selectedQuality) {
        DownloadQuality.LOSSLESS -> 32.0
        DownloadQuality.HIGH -> 10.0
        DownloadQuality.MEDIUM -> 5.0
        DownloadQuality.LOW -> 2.5
        DownloadQuality.AUTO -> 8.0
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = AuraColors.DarkSurfaceVariant,
        modifier = Modifier.testTag("download_options_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Download Options",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Configure offline audio settings for $title (${songs.size} track${if (songs.size > 1) "s" else ""})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Storage estimate banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AuraRadius.Medium)),
                color = AuraColors.DarkSurface.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SdStorage,
                            contentDescription = "Storage",
                            tint = AuraColors.NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Estimated Size: ~${"%.1f".format(totalEstimatedMb)} MB",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Free space: ${stats.freeDeviceSpaceGb} GB available",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quality Options List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Audio Quality",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )

                DownloadQuality.values().forEach { quality ->
                    val isSelected = selectedQuality == quality
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AuraRadius.Medium))
                            .background(
                                if (isSelected) AuraColors.ElectricPurple.copy(alpha = 0.25f)
                                else AuraColors.DarkSurface.copy(alpha = 0.4f)
                            )
                            .clickable { selectedQuality = quality }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = quality.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) AuraColors.NeonCyan else Color.White
                            )
                            Text(
                                text = quality.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = AuraColors.NeonCyan
                            )
                        }
                    }
                }
            }

            // Wi-Fi Only Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AuraRadius.Medium))
                    .background(AuraColors.DarkSurface.copy(alpha = 0.4f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Download over Wi-Fi only",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Avoid cellular data usage",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = wifiOnly,
                    onCheckedChange = { wifiOnly = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AuraColors.ElectricPurple,
                        checkedTrackColor = AuraColors.NeonCyan.copy(alpha = 0.4f)
                    )
                )
            }

            AuraButton(
                text = "Download (${songs.size} tracks)",
                icon = Icons.Default.Download,
                onClick = {
                    onConfirmDownload(selectedQuality, wifiOnly)
                    onDismissRequest()
                },
                modifier = Modifier.fillMaxWidth(),
                variant = AuraButtonVariant.PRIMARY_GRADIENT
            )
        }
    }
}
