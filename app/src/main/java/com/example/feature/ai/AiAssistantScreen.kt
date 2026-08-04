package com.example.feature.ai

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.ai.*
import com.example.core.database.entities.SongEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    onNavigateToInsights: () -> Unit = {},
    onPlayTrack: (SongEntity, List<SongEntity>) -> Unit = { _, _ -> }
) {
    val uiState by AiAssistantManager.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0B1E),
                        Color(0xFF070510),
                        Color(0xFF000000)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Aura AI DJ & Music Host",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = if (uiState.isDjModeActive) "Live DJ Mode Active" else "Smart Music Companion",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (uiState.isDjModeActive) Color(0xFF00E5FF) else Color(0xFFA0A0B0)
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // DJ Mode Switch Button
                FilterChip(
                    selected = uiState.isDjModeActive,
                    onClick = { AiAssistantManager.toggleDjMode() },
                    label = { Text(if (uiState.isDjModeActive) "DJ On" else "DJ Off") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (uiState.isDjModeActive) Icons.Filled.GraphicEq else Icons.Outlined.GraphicEq,
                            contentDescription = "Toggle DJ",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFF00E5FF)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onNavigateToInsights,
                    modifier = Modifier.testTag("ai_insights_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Analytics,
                        contentDescription = "Insights",
                        tint = Color(0xFF00E5FF)
                    )
                }
            }
        }

        // --- Mood selector strip ---
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = uiState.activeMood == null,
                    onClick = { AiAssistantManager.setMood(null) },
                    label = { Text("All Vibes") },
                    shape = RoundedCornerShape(20.dp)
                )
            }
            items(MoodType.entries.toTypedArray()) { mood ->
                FilterChip(
                    selected = uiState.activeMood == mood,
                    onClick = {
                        AiAssistantManager.setMood(if (uiState.activeMood == mood) null else mood)
                    },
                    label = { Text("${mood.emoji} ${mood.displayName}") },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF7C4DFF).copy(alpha = 0.3f),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // --- Active AI DJ Host Banner if active ---
        if (uiState.isDjModeActive && uiState.djSpeech != null) {
            val dj = uiState.djSpeech!!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF13102B).copy(alpha = 0.85f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Radio,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier
                            .size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dj.greeting,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00E5FF)
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dj.trackComment,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // --- Chat & Recommendations Body ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(uiState.messages, key = { it.id }) { msg ->
                AiMessageBubble(
                    message = msg,
                    onChipClick = { chip -> AiAssistantManager.handleChipAction(chip) },
                    onPlayTrack = onPlayTrack,
                    onSavePlaylist = { pl -> AiAssistantManager.saveGeneratedPlaylist(pl) }
                )
            }

            if (uiState.isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF00E5FF),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Aura AI is generating music recommendations...",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFA0A0B0))
                        )
                    }
                }
            }
        }

        // --- Voice Listening Modal Indicator if active ---
        if (uiState.isVoiceListening) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B0033).copy(alpha = 0.95f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size((36 * pulseScale).dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF4081)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Listening",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Listening... Speak your music request!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            AiAssistantManager.stopVoiceListeningAndProcess("Play workout tracks")
                        }
                    ) {
                        Text("Simulate", color = Color(0xFF00E5FF))
                    }
                }
            }
        }

        // --- Input Bar ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0A0718),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            "Ask AI: e.g. 'play workout tracks', 'chill hindi'",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6E6E85))
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_assistant_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF201B3A),
                        focusedContainerColor = Color(0xFF131028),
                        unfocusedContainerColor = Color(0xFF131028),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Voice Mic Button
                IconButton(
                    onClick = { AiAssistantManager.startVoiceListening() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF201B3A))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Voice Input",
                        tint = Color(0xFF00E5FF)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            AiAssistantManager.sendPrompt(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF))
                            )
                        )
                        .testTag("ai_assistant_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AiMessageBubble(
    message: AiChatMessage,
    onChipClick: (AiActionChip) -> Unit,
    onPlayTrack: (SongEntity, List<SongEntity>) -> Unit,
    onSavePlaylist: (GeneratedPlaylistResult) -> Unit
) {
    val isUser = message.sender == "USER"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Message Card
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) {
                        Brush.linearGradient(
                            listOf(Color(0xFF7C4DFF), Color(0xFF6200EA))
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(Color(0xFF13102B), Color(0xFF0E0B20))
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) Color.Transparent else Color(0xFF261D4C),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    lineHeight = 20.sp
                )
            )
        }

        // Render Generated Playlist if present
        if (message.generatedPlaylist != null) {
            val pl = message.generatedPlaylist
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13112A))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = pl.coverUrl,
                            contentDescription = pl.title,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pl.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "${pl.tracks.size} tracks • AI Playlist",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF00E5FF))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { onSavePlaylist(pl) },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (pl.tracks.isNotEmpty()) {
                                    onPlayTrack(pl.tracks.first(), pl.tracks)
                                }
                            },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play All", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Render Track List if attached
        if (message.tracks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0E0C20))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                message.tracks.take(4).forEach { song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPlayTrack(song, message.tracks) }
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = song.coverUrl,
                            contentDescription = song.title,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${song.artistName} • ${song.genre}",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFA0A0B0)),
                                maxLines = 1
                            )
                        }
                        IconButton(onClick = { onPlayTrack(song, message.tracks) }) {
                            Icon(
                                imageVector = Icons.Filled.PlayCircleFilled,
                                contentDescription = "Play",
                                tint = Color(0xFF00E5FF)
                            )
                        }
                    }
                }
            }
        }

        // Action Suggestion Chips
        if (message.actionChips.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(message.actionChips) { chip ->
                    SuggestionChip(
                        onClick = { onChipClick(chip) },
                        label = {
                            Text(
                                chip.label,
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF00E5FF))
                            )
                        },
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = Color(0xFF00E5FF).copy(alpha = 0.4f)
                        ),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFF100D28)
                        )
                    )
                }
            }
        }
    }
}
