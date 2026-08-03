package com.example.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
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
import com.example.core.database.entities.AlbumEntity
import com.example.core.database.entities.SongEntity
import com.example.core.designsystem.AuraColors
import com.example.core.designsystem.AuraRadius
import com.example.core.designsystem.auraGlass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumId: String,
    onBackClick: () -> Unit,
    onPlaySong: (SongEntity) -> Unit,
    libraryState: com.example.core.state.LibraryState,
    modifier: Modifier = Modifier,
    testTag: String = "album_detail_screen"
) {
    val album = remember(albumId) { MusicCatalog.getAlbumById(albumId) }
    val albumSongs = remember(albumId) { MusicCatalog.getSongsByAlbum(albumId) }
    val libraryData by libraryState.libraryDataState.collectAsState()

    if (album == null) {
        onBackClick()
        return
    }

    val isSaved = libraryData.savedAlbums.any { it.id == album.id }

    Column(
        modifier = modifier
            .testTag(testTag)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text(album.title, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = { libraryState.toggleSaveAlbum(album.id) }) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "Save Album",
                        tint = if (isSaved) AuraColors.NeonCyan else Color.White
                    )
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
                                .size(140.dp)
                                .clip(RoundedCornerShape(AuraRadius.Medium))
                        ) {
                            AsyncImage(
                                model = album.coverUrl,
                                contentDescription = album.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(album.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${album.artistName} • ${album.releaseYear}", style = MaterialTheme.typography.bodyMedium, color = AuraColors.NeonCyan)

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            AuraButton(
                                text = "Play Album",
                                icon = Icons.Default.PlayArrow,
                                onClick = {
                                    if (albumSongs.isNotEmpty()) onPlaySong(albumSongs.first())
                                },
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedButton(
                                onClick = { libraryState.toggleSaveAlbum(album.id) },
                                shape = RoundedCornerShape(AuraRadius.Medium),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isSaved) "Saved" else "Save Album", color = Color.White)
                            }
                        }
                    }
                }
            }

            item {
                Text("Album Tracks (${albumSongs.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }

            itemsIndexed(albumSongs, key = { _, song -> song.id }) { index, song ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AuraRadius.Small))
                        .auraGlass(shape = RoundedCornerShape(AuraRadius.Small))
                        .clickable { onPlaySong(song) }
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                        Text("${index + 1}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(24.dp))
                        AsyncImage(model = song.coverUrl, contentDescription = song.title, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(AuraRadius.Small)), contentScale = ContentScale.Crop)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(song.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(song.audioQuality, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
