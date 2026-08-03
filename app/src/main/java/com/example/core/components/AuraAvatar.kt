package com.example.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.designsystem.AuraColors

@Composable
fun AuraAvatar(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showFreeBadge: Boolean = true,
    testTag: String = "aura_avatar"
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .size(size)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(AuraColors.ElectricPurple.copy(alpha = 0.3f))
                .border(1.5.dp, AuraColors.ElectricPurple, CircleShape)
        ) {
            AsyncImage(
                model = photoUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                contentDescription = "User avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        if (showFreeBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
            ) {
                AuraFreeBadge()
            }
        }
    }
}
