package com.example.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.components.AuraButton
import com.example.core.components.AuraButtonVariant
import com.example.core.components.AuraGlassDialog
import com.example.core.components.AuraIconButton
import com.example.core.designsystem.AuraColors
import com.example.core.state.HistoryManager

@Composable
fun HistoryManagerDialog(
    onDismissRequest: () -> Unit
) {
    val searchHistory by HistoryManager.searchHistory.collectAsState()
    val playHistory by HistoryManager.playHistory.collectAsState()

    var activeTab by remember { mutableStateOf(0) }

    AuraGlassDialog(
        onDismissRequest = onDismissRequest,
        title = "History Management"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tab Switcher
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                contentColor = AuraColors.NeonCyan
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Recent Searches (${searchHistory.size})", style = MaterialTheme.typography.bodySmall) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Play History (${playHistory.size})", style = MaterialTheme.typography.bodySmall) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (activeTab == 0) {
                // Search History Tab
                if (searchHistory.isEmpty()) {
                    Text(
                        text = "Search history is empty.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Search Queries",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = HistoryManager::clearSearchHistory) {
                            Text("Clear All Searches", color = AuraColors.MagentaFlare, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(searchHistory, key = { it.id }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = AuraColors.NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = item.query,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                AuraIconButton(
                                    icon = Icons.Default.Clear,
                                    contentDescription = "Delete",
                                    onClick = { HistoryManager.removeSearchQuery(item.id) },
                                    size = 28.dp
                                )
                            }
                        }
                    }
                }
            } else {
                // Play History Tab
                if (playHistory.isEmpty()) {
                    Text(
                        text = "Play history is empty.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recently Played Tracks",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = HistoryManager::clearPlayHistory) {
                            Text("Clear All Play History", color = AuraColors.MagentaFlare, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(playHistory, key = { it.id }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.song.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = item.song.artistName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                AuraIconButton(
                                    icon = Icons.Default.Clear,
                                    contentDescription = "Delete",
                                    onClick = { HistoryManager.removePlayHistoryItem(item.id) },
                                    size = 28.dp
                                )
                            }
                        }
                    }
                }
            }

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
