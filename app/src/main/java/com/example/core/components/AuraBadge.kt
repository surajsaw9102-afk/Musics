package com.example.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius

@Composable
fun AuraFreeBadge(
    modifier: Modifier = Modifier,
    testTag: String = "aura_free_badge"
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(AuraRadius.Pill))
            .background(AuraColors.FreeBadgeBg)
            .border(0.8.dp, AuraColors.FreeBadgeText, RoundedCornerShape(AuraRadius.Pill))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "100% FREE",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            fontWeight = FontWeight.ExtraBold,
            color = AuraColors.FreeBadgeText
        )
    }
}

@Composable
fun AuraHdBadge(
    modifier: Modifier = Modifier,
    testTag: String = "aura_hd_badge"
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(4.dp))
            .background(AuraColors.ElectricPurple.copy(alpha = 0.25f))
            .border(0.8.dp, AuraColors.ElectricPurple, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "LOSSLESS HD",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
            fontWeight = FontWeight.Bold,
            color = AuraColors.NeonCyan
        )
    }
}
