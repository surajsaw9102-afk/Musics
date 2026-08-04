package com.example.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.components.AuraButton
import com.example.core.components.AuraButtonVariant
import com.example.core.components.AuraGlassDialog
import com.example.core.designsystem.AuraColors
import com.example.core.storage.StorageManager

@Composable
fun StorageDashboardDialog(
    onDismissRequest: () -> Unit
) {
    val stats by StorageManager.stats.collectAsState()

    AuraGlassDialog(
        onDismissRequest = onDismissRequest,
        title = "Storage & Cleanup Dashboard"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Storage Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total App Usage",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(stats.totalAppUsedMb * 10).toInt() / 10.0} MB",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AuraColors.NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    val downloadsFrac = ((stats.musicStorageMb / (stats.totalAppUsedMb.coerceAtLeast(1.0)))).toFloat().coerceIn(0.1f, 0.9f)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(downloadsFrac)
                            .background(AuraColors.NeonCyan)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f - downloadsFrac)
                            .background(AuraColors.ElectricPurple)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StorageLegendItem("Downloaded Tracks", "${stats.musicStorageMb} MB", AuraColors.NeonCyan)
                    StorageLegendItem("Cached Data", "${stats.cacheStorageMb} MB", AuraColors.ElectricPurple)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Free Capacity Estimate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SdStorage,
                        contentDescription = null,
                        tint = AuraColors.EmeraldPulse,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Free Device Storage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${stats.freeDeviceSpaceGb} GB Available",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = AuraColors.EmeraldPulse
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Actions
            AuraButton(
                text = "Clear Cache (${stats.cacheStorageMb} MB)",
                onClick = {
                    StorageManager.clearCache()
                },
                modifier = Modifier.fillMaxWidth(),
                variant = AuraButtonVariant.OUTLINED
            )

            AuraButton(
                text = "Delete All Downloads (${stats.musicStorageMb} MB)",
                onClick = {
                    StorageManager.deleteAllDownloads()
                },
                icon = Icons.Default.Delete,
                modifier = Modifier.fillMaxWidth(),
                variant = AuraButtonVariant.OUTLINED
            )

            AuraButton(
                text = "Close Dashboard",
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth(),
                variant = AuraButtonVariant.PRIMARY_GRADIENT
            )
        }
    }
}

@Composable
private fun StorageLegendItem(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
