package com.example.feature.social

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.components.*
import com.example.core.designsystem.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraShareSheet(
    shareContent: ShareContent,
    onDismiss: () -> Unit,
    onPlayClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showQrCode by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AuraColors.DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(topStart = AuraRadius.ExtraLarge, topEnd = AuraRadius.ExtraLarge),
        modifier = Modifier.testTag("aura_share_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sheet Title & Close Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = AuraColors.NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share Content",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_share_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shared Content Premium Preview Card
            SharedContentPreviewCard(
                shareContent = shareContent,
                showQrCode = showQrCode,
                onPlayClick = onPlayClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary System Share Sheet Button
                AuraButton(
                    text = "Share via Apps",
                    icon = Icons.Filled.IosShare,
                    onClick = {
                        ShareManager.shareViaSystemSheet(context, shareContent)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "share_via_apps_button"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Copy Link Button
                    AuraButton(
                        text = "Copy Link",
                        icon = Icons.Filled.ContentCopy,
                        variant = AuraButtonVariant.SECONDARY,
                        onClick = {
                            val success = ShareManager.copyLinkToClipboard(context, shareContent)
                            if (success) {
                                Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "copy_link_button"
                    )

                    // Copy Text Card Button
                    AuraButton(
                        text = "Copy Card",
                        icon = Icons.Filled.AmpStories,
                        variant = AuraButtonVariant.SECONDARY,
                        onClick = {
                            val success = ShareManager.copyTextCardToClipboard(context, shareContent)
                            if (success) {
                                Toast.makeText(context, "Text card copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "copy_text_card_button"
                    )
                }

                // Toggle QR Code Card Preview
                AuraButton(
                    text = if (showQrCode) "Hide QR Preview" else "Show QR Preview",
                    icon = Icons.Filled.QrCode,
                    variant = AuraButtonVariant.SECONDARY,
                    onClick = { showQrCode = !showQrCode },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "toggle_qr_preview_button"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun SharedContentPreviewCard(
    shareContent: ShareContent,
    showQrCode: Boolean = false,
    onPlayClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .auraGlass(shape = RoundedCornerShape(AuraRadius.Large))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(AuraColors.ElectricPurple.copy(alpha = 0.6f), AuraColors.NeonCyan.copy(alpha = 0.4f))
                ),
                shape = RoundedCornerShape(AuraRadius.Large)
            )
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Type Badge & Free Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuraChip(
                    label = shareContent.contentType.name,
                    isSelected = true,
                    onClick = {}
                )
                AuraFreeBadge()
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (showQrCode) {
                // QR Code Style Glassmorphic Card
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.95f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCode2,
                            contentDescription = "QR Code Preview",
                            tint = Color.Black,
                            modifier = Modifier.size(110.dp)
                        )
                        Text(
                            text = "AURA MUSIC",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                color = AuraColors.ElectricPurple
                            )
                        )
                    }
                }
            } else {
                // Media Cover Artwork
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(AuraRadius.Medium))
                        .border(1.dp, AuraColors.ElectricPurple.copy(alpha = 0.4f), RoundedCornerShape(AuraRadius.Medium))
                ) {
                    AsyncImage(
                        model = shareContent.imageUrl,
                        contentDescription = shareContent.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (onPlayClick != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = onPlayClick,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AuraColors.ElectricPurple)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title & Subtitle
            Text(
                text = shareContent.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = shareContent.subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AuraColors.NeonCyan
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            if (!shareContent.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = shareContent.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
