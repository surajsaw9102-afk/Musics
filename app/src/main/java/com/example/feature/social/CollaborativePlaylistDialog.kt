package com.example.feature.social

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.components.*
import com.example.core.designsystem.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollaborativePlaylistManagerDialog(
    playlistId: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colPlaylists by CollaborationManager.collaborativePlaylists.collectAsState()
    val playlist = colPlaylists.find { it.playlistId == playlistId }

    var inviteInputCode by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Collaborators, 1 = Activity Log, 2 = Invite

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AuraColors.DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(topStart = AuraRadius.ExtraLarge, topEnd = AuraRadius.ExtraLarge),
        modifier = Modifier.testTag("collab_playlist_manager_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = "Collaborators",
                        tint = AuraColors.NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Collaborative Playlist",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (playlist == null) {
                // Join or Create Prompt
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Enable collaborative mode for friends to add and manage songs together!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AuraButton(
                        text = "Enable Collaboration Mode",
                        icon = Icons.Filled.GroupAdd,
                        onClick = {
                            val enabled = CollaborationManager.toggleCollaborativeMode(playlistId)
                            if (enabled) {
                                Toast.makeText(context, "Collaboration Mode Activated!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(0.1f))

                    Text(
                        text = "Or join a friend's playlist with an Invite Code:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    AuraTextField(
                        value = inviteInputCode,
                        onValueChange = { inviteInputCode = it.uppercase() },
                        placeholder = "ENTER INVITE CODE (e.g. CYBER2026)",
                        leadingIcon = Icons.Filled.Key,
                        modifier = Modifier.fillMaxWidth()
                    )

                    AuraButton(
                        text = "Join Shared Playlist",
                        icon = Icons.Filled.Check,
                        variant = AuraButtonVariant.SECONDARY,
                        onClick = {
                            val joined = CollaborationManager.joinByInviteCode(inviteInputCode)
                            if (joined != null) {
                                Toast.makeText(context, "Joined ${joined.title}!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            } else {
                                Toast.makeText(context, "Invalid Invite Code", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // Playlist Details Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .auraGlass(shape = RoundedCornerShape(AuraRadius.Medium))
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(AuraRadius.Small))
                        ) {
                            AsyncImage(
                                model = playlist.coverUrl,
                                contentDescription = playlist.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playlist.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Invite Code: ${playlist.inviteCode}",
                                style = MaterialTheme.typography.labelSmall.copy(color = AuraColors.NeonCyan)
                            )
                        }

                        Switch(
                            checked = playlist.isCollaborative,
                            onCheckedChange = {
                                CollaborationManager.toggleCollaborativeMode(playlistId)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AuraColors.ElectricPurple
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: Collaborators | Activity Log | Invite Code
                AuraSegmentedTabs(
                    tabs = listOf("Contributors", "Activity Log", "Invite Code"),
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.heightIn(max = 280.dp)) {
                    when (selectedTab) {
                        0 -> {
                            // Contributors List
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(playlist.collaborators) { collab ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .auraGlass(shape = RoundedCornerShape(AuraRadius.Medium))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AuraAvatar(
                                            photoUrl = collab.avatarUrl,
                                            size = 40.dp,
                                            showFreeBadge = false
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = collab.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${collab.handle} • ${collab.tracksAddedCount} tracks added",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(AuraRadius.Pill))
                                                .background(AuraColors.ElectricPurple.copy(alpha = 0.25f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = collab.role.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = AuraColors.NeonCyan
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Activity Log Feed
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(playlist.activityLogs) { log ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(AuraRadius.Small))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                            .padding(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.History,
                                                contentDescription = "Activity",
                                                tint = AuraColors.ElectricPurple,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "${log.userName} • ${log.action}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Just now",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Invite Code Section
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Share this code with friends to let them join and edit this playlist:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .auraGlass(shape = RoundedCornerShape(AuraRadius.Medium))
                                        .padding(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                ) {
                                    Text(
                                        text = playlist.inviteCode,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 4.sp,
                                            color = AuraColors.NeonCyan
                                        ),
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }

                                AuraButton(
                                    text = "Copy Invite Link",
                                    icon = Icons.Filled.ContentCopy,
                                    onClick = {
                                        ShareManager.copyLinkToClipboard(
                                            context,
                                            ShareContent(
                                                contentType = ShareContentType.PLAYLIST,
                                                id = playlist.playlistId,
                                                title = playlist.title,
                                                subtitle = "Invite Code: ${playlist.inviteCode}",
                                                imageUrl = playlist.coverUrl,
                                                shareUrl = ShareManager.createShareUrl(ShareContentType.PLAYLIST, playlist.playlistId),
                                                deepLink = ShareManager.createDeepLink(ShareContentType.PLAYLIST, playlist.playlistId)
                                            )
                                        )
                                        Toast.makeText(context, "Invite link copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
