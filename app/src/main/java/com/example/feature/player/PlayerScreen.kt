package com.example.feature.player

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.core.components.*
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass
import com.example.core.player.RepeatModeState
import com.example.core.player.SleepTimerManager
import com.example.core.player.SleepTimerOption
import com.example.core.player.lyrics.LyricLine
import com.example.core.player.lyrics.LyricsData
import com.example.core.state.PlayerState
import kotlinx.coroutines.launch

enum class PlayerTab {
    ARTWORK, LYRICS, AUDIO_SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onDismiss: () -> Unit,
    onOpenQueue: () -> Unit = {},
    playerState: PlayerState = viewModel(),
    modifier: Modifier = Modifier,
    testTag: String = "player_screen"
) {
    val context = LocalContext.current
    val playerData by playerState.playerData.collectAsState()
    val song = playerData.currentSong ?: return

    var activeTab by remember { mutableStateOf(PlayerTab.ARTWORK) }
    var isUserScrubbing by remember { mutableStateOf(false) }
    var scrubProgressFraction by remember { mutableFloatStateOf(0f) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showSongOverflowSheet by remember { mutableStateOf(false) }
    var showLikeHeartBurst by remember { mutableStateOf(false) }

    val duration = if (playerData.durationMs > 0) playerData.durationMs else song.durationMs
    val currentProgressFraction = if (duration > 0) {
        (playerData.progressMs.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val sliderValue = if (isUserScrubbing) scrubProgressFraction else currentProgressFraction

    // Vinyl Rotation Animation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_rotation"
    )

    // Pulse animation for heart burst on double tap
    val heartScale by animateFloatAsState(
        targetValue = if (showLikeHeartBurst) 1.5f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        finishedListener = { showLikeHeartBurst = false },
        label = "heart_scale"
    )

    // Dynamic Atmospheric Background Gradient derived from current song theme
    val backgroundBrush = remember(song.id) {
        Brush.verticalGradient(
            colors = listOf(
                AuraColors.ElectricPurple.copy(alpha = 0.45f),
                AuraColors.DarkBackground,
                AuraColors.DarkBackground
            )
        )
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .fillMaxSize()
            .background(backgroundBrush)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 50) {
                        onDismiss() // Swipe down to minimize player
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation & Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuraIconButton(
                    icon = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Minimize player",
                    onClick = onDismiss,
                    size = 40.dp
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM ALBUM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = song.albumTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AuraIconButton(
                        icon = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Queue",
                        onClick = {
                            onDismiss()
                            onOpenQueue()
                        },
                        size = 40.dp
                    )
                    AuraIconButton(
                        icon = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        onClick = { showSongOverflowSheet = true },
                        size = 40.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-Navigation Bar Tabs (Artwork, Lyrics, Audio Controls)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(AuraRadius.Large))
                    .background(AuraColors.DarkSurfaceVariant.copy(alpha = 0.6f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PlayerTabChip(
                    title = "Artwork",
                    icon = Icons.Default.Album,
                    isSelected = activeTab == PlayerTab.ARTWORK,
                    onClick = { activeTab = PlayerTab.ARTWORK }
                )
                PlayerTabChip(
                    title = "Lyrics",
                    icon = Icons.Default.Lyrics,
                    isSelected = activeTab == PlayerTab.LYRICS,
                    onClick = { activeTab = PlayerTab.LYRICS }
                )
                PlayerTabChip(
                    title = if (playerData.sleepTimerOption != SleepTimerOption.OFF) "Audio (${SleepTimerManager.formatRemainingTime(playerData.sleepTimerRemainingSeconds)})" else "Audio & Speed",
                    icon = Icons.Default.Tune,
                    isSelected = activeTab == PlayerTab.AUDIO_SETTINGS,
                    onClick = { activeTab = PlayerTab.AUDIO_SETTINGS }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Connection Banner
            if (playerData.errorMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = AuraColors.DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraColors.GoldAmber)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = AuraColors.GoldAmber
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Playback Connection Issue",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AuraColors.GoldAmber
                            )
                            Text(
                                text = playerData.errorMessage ?: "Failed to stream audio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { playerState.retryPlayback() }) {
                            Text("Retry", color = AuraColors.NeonCyan)
                        }
                    }
                }
            }

            // --- TAB CONTENT VIEW ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "player_tab_content"
                ) { target ->
                    when (target) {
                        PlayerTab.ARTWORK -> {
                            ArtworkTabContent(
                                song = song,
                                playerData = playerData,
                                rotation = rotation,
                                onDoubleTap = {
                                    playerState.toggleLike()
                                    showLikeHeartBurst = true
                                },
                                onLongPress = { showSongOverflowSheet = true },
                                onSwipeNext = { playerState.skipNext() },
                                onSwipePrevious = { playerState.skipPrevious() }
                            )
                        }

                        PlayerTab.LYRICS -> {
                            LyricsTabContent(
                                lyricsData = playerData.lyrics,
                                currentProgressMs = playerData.progressMs,
                                onLyricLineClick = { timestamp -> playerState.seekTo(timestamp) }
                            )
                        }

                        PlayerTab.AUDIO_SETTINGS -> {
                            AudioSettingsTabContent(
                                playerData = playerData,
                                playerState = playerState
                            )
                        }
                    }
                }

                // Heart Burst Animation Overlay on Double Tap
                if (heartScale > 0.1f) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Liked",
                        tint = AuraColors.MagentaPulse,
                        modifier = Modifier
                            .size(100.dp)
                            .scale(heartScale)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- BOTTOM CONTROLS SECTION (PERSISTENT ACROSS TABS) ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title, Artist, and Like Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (song.isHdAudio) AuraHdBadge()
                        }

                        Text(
                            text = song.artistName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    AuraIconButton(
                        icon = if (playerData.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like song",
                        onClick = playerState::toggleLike,
                        tint = if (playerData.isLiked) AuraColors.MagentaPulse else MaterialTheme.colorScheme.onSurface,
                        size = 42.dp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Equalizer & Audio Quality Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AuraEqualizerWave(isPlaying = playerData.isPlaying, barCount = 6)
                    Text(
                        text = "${playerData.audioQuality} • ${playerData.playbackSpeed}x",
                        style = MaterialTheme.typography.labelSmall,
                        color = AuraColors.NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Slider
                Slider(
                    value = sliderValue,
                    onValueChange = { newValue ->
                        isUserScrubbing = true
                        scrubProgressFraction = newValue
                    },
                    onValueChangeFinished = {
                        val seekPositionMs = (scrubProgressFraction * duration).toLong()
                        playerState.seekTo(seekPositionMs)
                        isUserScrubbing = false
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = AuraColors.NeonCyan,
                        activeTrackColor = AuraColors.ElectricPurple,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val displayMs = if (isUserScrubbing) (scrubProgressFraction * duration).toLong() else playerData.progressMs
                    Text(
                        text = formatTimeMs(displayMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTimeMs(duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Transport Controls Bar (+10s, -10s, Skip, Play, Shuffle, Repeat)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    AuraIconButton(
                        icon = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        onClick = playerState::toggleShuffle,
                        size = 38.dp,
                        isGlass = false,
                        tint = if (playerData.isShuffle) AuraColors.NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Rewind 10s
                    AuraIconButton(
                        icon = Icons.Default.FastRewind,
                        contentDescription = "Rewind 10s",
                        onClick = { playerState.rewind(10000L) },
                        size = 38.dp,
                        isGlass = false
                    )

                    // Skip Previous
                    AuraIconButton(
                        icon = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        onClick = playerState::skipPrevious,
                        size = 46.dp
                    )

                    // Play / Pause Circle
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(AuraColors.ElectricPurple)
                            .clickable(onClick = playerState::togglePlayPause),
                        contentAlignment = Alignment.Center
                    ) {
                        if (playerData.isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (playerData.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playerData.isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Skip Next
                    AuraIconButton(
                        icon = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        onClick = playerState::skipNext,
                        size = 46.dp
                    )

                    // Fast Forward 10s
                    AuraIconButton(
                        icon = Icons.Default.FastForward,
                        contentDescription = "Fast Forward 10s",
                        onClick = { playerState.fastForward(10000L) },
                        size = 38.dp,
                        isGlass = false
                    )

                    // Repeat
                    val repeatIcon = when (playerData.repeatMode) {
                        RepeatModeState.ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    }
                    val repeatTint = when (playerData.repeatMode) {
                        RepeatModeState.OFF -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> AuraColors.NeonCyan
                    }

                    AuraIconButton(
                        icon = repeatIcon,
                        contentDescription = "Repeat",
                        onClick = playerState::toggleRepeat,
                        size = 38.dp,
                        isGlass = false,
                        tint = repeatTint
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Volume Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (playerData.volume == 0f) Icons.Default.VolumeOff else Icons.Default.VolumeMute,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )

                    Slider(
                        value = playerData.volume,
                        onValueChange = { newVol -> playerState.setVolume(newVol) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = AuraColors.ElectricPurple,
                            activeTrackColor = AuraColors.ElectricPurple,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Queue Modal Bottom Sheet
    if (showQueueSheet) {
        QueueBottomSheet(
            playerData = playerData,
            playerState = playerState,
            onDismiss = { showQueueSheet = false },
            onOpenFullQueue = {
                onDismiss()
                onOpenQueue()
            }
        )
    }

    // Song Overflow Sheet (Three-Dot Menu)
    if (showSongOverflowSheet) {
        SongOverflowSheet(
            song = song,
            onDismiss = { showSongOverflowSheet = false },
            onPlayNow = { playerState.playSong(song) },
            onPlayNext = { playerState.addNext(song) },
            onPlayLater = { playerState.addToQueue(song) },
            onAddToPlaylist = { playerState.saveQueueAsPlaylist("Playlist - ${song.title}") },
            onToggleLike = { playerState.toggleLike() },
            isLiked = playerData.isLiked
        )
    }
}

@Composable
fun ArtworkTabContent(
    song: com.example.core.database.entities.SongEntity,
    playerData: com.example.core.state.PlayerData,
    rotation: Float,
    onDoubleTap: () -> Unit,
    onLongPress: () -> Unit,
    onSwipeNext: () -> Unit,
    onSwipePrevious: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDoubleTap() },
                    onLongPress = { onLongPress() }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -40) onSwipeNext()
                    else if (dragAmount > 40) onSwipePrevious()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Rotating Vinyl Record Artwork Frame
        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(CircleShape)
                .auraGlass(shape = CircleShape)
                .border(2.dp, AuraColors.DarkGlassBorder, CircleShape)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = song.coverUrl,
                contentDescription = song.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .rotate(if (playerData.isPlaying) rotation else 0f),
                contentScale = ContentScale.Crop
            )

            // Inner Vinyl Hole
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AuraColors.DarkBackground)
                    .border(2.dp, AuraColors.NeonCyan, CircleShape)
            )
        }
    }
}

@Composable
fun LyricsTabContent(
    lyricsData: LyricsData,
    currentProgressMs: Long,
    onLyricLineClick: (Long) -> Unit
) {
    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .auraGlass(shape = RoundedCornerShape(AuraRadius.Large))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (lyricsData) {
            is LyricsData.Loading -> {
                CircularProgressIndicator(color = AuraColors.NeonCyan)
            }

            is LyricsData.Synced -> {
                val lines = lyricsData.lines
                val activeIndex = remember(currentProgressMs, lines) {
                    lines.indexOfLast { it.timestampMs <= currentProgressMs }.coerceAtLeast(0)
                }

                // Smooth auto scroll to active lyric line
                LaunchedEffect(activeIndex) {
                    if (activeIndex in lines.indices) {
                        listState.animateScrollToItem(activeIndex)
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 40.dp)
                ) {
                    itemsIndexed(lines) { index, line ->
                        val isActive = index == activeIndex

                        Text(
                            text = line.text,
                            style = if (isActive) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) AuraColors.NeonCyan else Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLyricLineClick(line.timestampMs) }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            is LyricsData.Plain -> {
                Text(
                    text = lyricsData.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }

            is LyricsData.Instrumental -> {
                AuraEmptyState(
                    title = "Instrumental Track",
                    description = "Sit back and enjoy the acoustic and synthesizer tones.",
                    icon = Icons.Default.GraphicEq
                )
            }

            is LyricsData.None -> {
                AuraEmptyState(
                    title = "Lyrics Unavailable",
                    description = "Time-synced lyrics for this track will be updated soon.",
                    icon = Icons.Default.Lyrics
                )
            }
        }
    }
}

@Composable
fun AudioSettingsTabContent(
    playerData: com.example.core.state.PlayerData,
    playerState: PlayerState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .auraGlass(shape = RoundedCornerShape(AuraRadius.Large))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sleep Timer Section
        Text(
            text = "Sleep Timer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SleepTimerOption.values().take(4).forEach { option ->
                val isSelected = playerData.sleepTimerOption == option
                AuraChip(
                    label = option.label,
                    isSelected = isSelected,
                    onClick = {
                        if (isSelected) playerState.cancelSleepTimer()
                        else playerState.startSleepTimer(option)
                    }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SleepTimerOption.values().drop(4).forEach { option ->
                val isSelected = playerData.sleepTimerOption == option
                AuraChip(
                    label = option.label,
                    isSelected = isSelected,
                    onClick = {
                        if (isSelected) playerState.cancelSleepTimer()
                        else playerState.startSleepTimer(option)
                    }
                )
            }
        }

        Divider(color = Color.White.copy(alpha = 0.1f))

        // Playback Speed Selector
        Text(
            text = "Playback Speed (${playerData.playbackSpeed}x)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                val isSelected = playerData.playbackSpeed == speed
                AuraChip(
                    label = "${speed}x",
                    isSelected = isSelected,
                    onClick = { playerState.setPlaybackSpeed(speed) }
                )
            }
        }

        Divider(color = Color.White.copy(alpha = 0.1f))

        // Audio Settings Switches
        Text(
            text = "Audio Normalization & Controls",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        AudioToggleRow(
            label = "Pause on Headset Disconnect",
            subtitle = "Automatically pause when headphones are unplugged",
            checked = playerData.audioSettings.pauseOnHeadsetDisconnect,
            onCheckedChange = { playerState.togglePauseOnHeadsetDisconnect(it) }
        )

        AudioToggleRow(
            label = "Resume on Headset Connect",
            subtitle = "Automatically resume playback when reconnected",
            checked = playerData.audioSettings.resumeOnHeadsetConnect,
            onCheckedChange = { playerState.toggleResumeOnHeadsetConnect(it) }
        )

        AudioToggleRow(
            label = "Audio Normalization",
            subtitle = "Equalize volume across different tracks (-14 LUFS)",
            checked = playerData.audioSettings.audioNormalization,
            onCheckedChange = { /* Audio Normalization toggled */ }
        )

        AudioToggleRow(
            label = "Mono Audio",
            subtitle = "Combine audio channels into a single mono output",
            checked = playerData.audioSettings.monoAudio,
            onCheckedChange = { playerState.toggleMonoAudio(it) }
        )
    }
}

@Composable
fun PlayerTabChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(AuraRadius.Medium),
        color = if (isSelected) AuraColors.ElectricPurple else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun AudioToggleRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AuraColors.ElectricPurple,
                checkedTrackColor = AuraColors.NeonCyan.copy(alpha = 0.4f)
            ),
            modifier = Modifier.scale(0.8f)
        )
    }
}

@Composable
fun QuickActionsDialog(
    song: com.example.core.database.entities.SongEntity,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = song.title, fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Artist: ${song.artistName}\nAlbum: ${song.albumTitle}\nQuality: ${song.audioQuality}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Divider(color = Color.White.copy(alpha = 0.1f))

                AuraButton(
                    text = "Share Track",
                    onClick = {
                        onShare()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AuraColors.NeonCyan)
            }
        },
        containerColor = AuraColors.DarkSurfaceVariant
    )
}

private fun formatTimeMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
