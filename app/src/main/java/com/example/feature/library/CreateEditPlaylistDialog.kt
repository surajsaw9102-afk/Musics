package com.example.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.core.components.AuraButton
import com.example.core.components.AuraTextField
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass

@Composable
fun CreateEditPlaylistDialog(
    initialName: String = "",
    initialDescription: String = "",
    initialCoverUrl: String = "",
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, coverUrl: String) -> Unit,
    testTag: String = "create_edit_playlist_dialog"
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    var coverUrl by remember { mutableStateOf(initialCoverUrl) }

    val presetCovers = remember {
        listOf(
            "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500", // Cyberpunk Dark
            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500", // High Voltage
            "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500", // Sunset Glow
            "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=500", // Rain Lo-Fi
            "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500"  // Synthwave Horizon
        )
    }

    if (coverUrl.isBlank()) {
        coverUrl = presetCovers.first()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .testTag(testTag)
                .fillMaxWidth()
                .clip(RoundedCornerShape(AuraRadius.Large))
                .auraGlass(shape = RoundedCornerShape(AuraRadius.Large))
                .background(AuraColors.DarkSurfaceVariant.copy(alpha = 0.95f))
                .padding(20.dp),
            color = Color.Transparent
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Playlist" else "Create Playlist",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Selected Cover Preview
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(AuraRadius.Medium))
                        .border(1.dp, AuraColors.ElectricPurple, RoundedCornerShape(AuraRadius.Medium))
                ) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = "Playlist Cover Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Preset Covers Picker
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Choose Preset Artwork",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presetCovers) { preset ->
                            val isSelected = preset == coverUrl
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(AuraRadius.Small))
                                    .clickable { coverUrl = preset }
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) AuraColors.NeonCyan else Color.Transparent,
                                        shape = RoundedCornerShape(AuraRadius.Small)
                                    )
                            ) {
                                AsyncImage(
                                    model = preset,
                                    contentDescription = "Preset Cover",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = AuraColors.NeonCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Inputs
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Title", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    AuraTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Playlist Title (e.g. Cyberpunk Chill)",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Description", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    AuraTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Optional description...",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(AuraRadius.Medium)
                    ) {
                        Text("Cancel", color = Color.White)
                    }

                    AuraButton(
                        text = if (isEditing) "Save Changes" else "Create",
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name.trim(), description.trim(), coverUrl)
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
