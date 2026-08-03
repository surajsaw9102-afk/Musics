package com.example.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
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
import coil.compose.AsyncImage
import com.example.core.catalog.MusicCatalog
import com.example.core.components.AuraButton
import com.example.core.database.entities.ArtistEntity
import com.example.core.database.entities.SongEntity
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistId: String,
    onBackClick: () -> Unit,
    onPlaySong: (SongEntity) -> Unit,
    libraryState: com.example.core.state.LibraryState,
    modifier: Modifier = Modifier,
    testTag: String = "artist_detail_screen"
) {
    val artist = remember(artistId) { MusicCatalog.getArtistById(artistId) }
    val artistSongs = remember(artistId) { MusicCatalog.getSongsByArtist(artistId) }
    val libraryData by libraryState.libraryDataState.collectAsState()

    if (artist == null) {
        onBackClick()
        return
    }

    val isFollowed = libraryData.followedArtists.any { it.id == artist.id }

    Column(
        modifier = modifier
            .testTag(testTag)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text(artist.name, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AuraRadius.Large))
                        .auraGlass(shape = RoundedCornerShape(AuraRadius.Large))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(AuraColors.ElectricPurple.copy(alpha = 0.35f), AuraColors.DarkSurfaceVariant)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                        ) {
                            AsyncImage(
                                model = artist.avatarUrl,
                                contentDescription = artist.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(artist.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${artist.monthlyListeners / 1000000f}M Monthly Listeners", style = MaterialTheme.typography.bodyMedium, color = AuraColors.NeonCyan)
                        if (artist.bio.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(artist.bio, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            AuraButton(
                                text = if (isFollowed) "Following" else "Follow Artist",
                                icon = if (isFollowed) Icons.Default.Check else Icons.Default.PersonAdd,
                                onClick = { libraryState.toggleFollowArtist(artist.id) },
                                modifier = Modifier.weight(1f)
                            )

                            if (artistSongs.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { onPlaySong(artistSongs.first()) },
                                    shape = RoundedCornerShape(AuraRadius.Medium),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = AuraColors.NeonCyan)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Play Popular", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("Popular Songs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }

            items(artistSongs, key = { it.id }) { song ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AuraRadius.Small))
                        .auraGlass(shape = RoundedCornerShape(AuraRadius.Small))
                        .clickable { onPlaySong(song) }
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                        AsyncImage(model = song.coverUrl, contentDescription = song.title, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(AuraRadius.Small)), contentScale = ContentScale.Crop)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(song.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(song.albumTitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = { onPlaySong(song) }) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = AuraColors.NeonCyan)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
