package com.example.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass

enum class HeaderType {
    ARTIST,
    ALBUM
}

@Composable
fun ArtworkHeader(
    type: HeaderType,
    title: String,
    subtitle: String,
    imageUrl: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    isVerified: Boolean = false,
    monthlyListenersText: String? = null,
    isPrimaryActionActive: Boolean = false,
    primaryActionLabel: String = "Follow",
    onPrimaryActionClick: () -> Unit = {},
    onPlayAllClick: () -> Unit = {},
    onShuffleClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    testTag: String = "artwork_header"
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .height(340.dp)
    ) {
        // Hero Background Artwork Image with Blur & Dark Gradient Overlay
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            modifier = Modifier
                .fillMaxSize()
                .background(AuraColors.DarkSurface),
            contentScale = ContentScale.Crop
        )

        // Gradient Scrim Overlays (Cinematic Dark Glow)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            AuraColors.DarkSurface.copy(alpha = 0.4f),
                            AuraColors.DarkSurface
                        )
                    )
                )
        )

        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AuraColors.DarkSurfaceVariant.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = onShareClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AuraColors.DarkSurfaceVariant.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White
                )
            }
        }

        // Bottom Layered Content Card (Glassmorphic)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(AuraRadius.Large))
                .auraGlass(shape = RoundedCornerShape(AuraRadius.Large))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AuraColors.DarkSurfaceVariant.copy(alpha = 0.85f),
                            AuraColors.DarkSurface.copy(alpha = 0.95f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular Avatar for Artist or Square Artwork for Album
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(if (type == HeaderType.ARTIST) CircleShape else RoundedCornerShape(AuraRadius.Medium))
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isVerified) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Artist",
                                tint = AuraColors.NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (monthlyListenersText != null) {
                        Text(
                            text = monthlyListenersText,
                            style = MaterialTheme.typography.labelMedium,
                            color = AuraColors.NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play button
                        IconButton(
                            onClick = onPlayAllClick,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AuraColors.NeonCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play All",
                                tint = Color.Black
                            )
                        }

                        // Shuffle button
                        IconButton(
                            onClick = onShuffleClick,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AuraColors.ElectricPurple.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = AuraColors.NeonCyan
                            )
                        }

                        // Follow / Like Toggle
                        OutlinedButton(
                            onClick = onPrimaryActionClick,
                            shape = RoundedCornerShape(AuraRadius.Pill),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(38.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isPrimaryActionActive) AuraColors.NeonCyan.copy(alpha = 0.15f) else Color.Transparent,
                                contentColor = if (isPrimaryActionActive) AuraColors.NeonCyan else Color.White
                            )
                        ) {
                            Icon(
                                imageVector = if (isPrimaryActionActive) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = primaryActionLabel,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = primaryActionLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
