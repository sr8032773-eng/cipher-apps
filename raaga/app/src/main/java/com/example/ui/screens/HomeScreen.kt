package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlaylistEntity
import com.example.data.SongEntity
import com.example.ui.components.AlbumArtCard
import com.example.ui.components.AlbumGradients
import com.example.ui.components.TrackItemRow
import com.example.ui.theme.RaagaAmber
import com.example.ui.theme.RaagaDarkSurface
import com.example.ui.theme.RaagaDarkSurfaceHighlight
import com.example.ui.theme.RaagaDarkSurfaceVariant
import com.example.ui.theme.RaagaEmerald
import com.example.ui.theme.RaagaTextPrimary
import com.example.ui.theme.RaagaTextSecondary
import com.example.ui.theme.RaagaViolet

@Composable
fun HomeScreen(
    allSongs: List<SongEntity>,
    recentlyPlayed: List<SongEntity>,
    playlists: List<PlaylistEntity>,
    currentSong: SongEntity?,
    isPlaying: Boolean,
    onSongClick: (SongEntity, List<SongEntity>) -> Unit,
    onPlaylistClick: (PlaylistEntity) -> Unit,
    onToggleFavorite: (SongEntity) -> Unit,
    onAddToPlaylistClick: (SongEntity) -> Unit,
    onOpenEqualizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val moodFilters = listOf("All", "Peaceful", "Soulful", "Focus", "Late Night", "Energetic")
    var selectedMood by remember { mutableStateOf("All") }

    val filteredSongs = remember(allSongs, selectedMood) {
        if (selectedMood == "All") allSongs else allSongs.filter { it.moodTag.equals(selectedMood, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_home"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Top App Bar Greeting
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(8.dp, CircleShape, spotColor = RaagaAmber.copy(alpha = 0.5f))
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(RaagaAmber, RaagaViolet))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Raaga Logo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Raaga Music",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = RaagaTextPrimary
                        )
                        Text(
                            text = "Acoustic harmony & classical soul",
                            style = MaterialTheme.typography.bodySmall,
                            color = RaagaTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onOpenEqualizer,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(RaagaDarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Equalizer",
                        tint = RaagaAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Quick Picks 2-column Grid (like competitor app)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Quick Picks",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = RaagaTextPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )

                val quickSongs = (if (recentlyPlayed.isNotEmpty()) recentlyPlayed else allSongs).take(6)
                quickSongs.chunked(2).forEach { rowSongs ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowSongs.forEach { song ->
                            QuickPickCard(
                                song = song,
                                isPlaying = currentSong?.id == song.id && isPlaying,
                                onClick = { onSongClick(song, quickSongs) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowSongs.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Featured Raga Playlists (Horizontal carousel)
        item {
            Column(modifier = Modifier.padding(top = 24.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Featured Collections",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = RaagaTextPrimary
                    )
                    Text(
                        text = "Explore All",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = RaagaAmber
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(playlists) { pl ->
                        PlaylistFeatureCard(
                            playlist = pl,
                            onClick = { onPlaylistClick(pl) }
                        )
                    }
                }
            }
        }

        // Mood & Raga Filter Chips
        item {
            Column(modifier = Modifier.padding(top = 24.dp)) {
                Text(
                    text = "Browse by Mood",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = RaagaTextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(moodFilters) { mood ->
                        val isSelected = mood == selectedMood
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMood = mood },
                            label = {
                                Text(
                                    text = mood,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = RaagaDarkSurfaceVariant,
                                labelColor = RaagaTextSecondary,
                                selectedContainerColor = RaagaAmber,
                                selectedLabelColor = Color(0xFF1F1000)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = RaagaDarkSurfaceHighlight,
                                selectedBorderColor = RaagaAmber
                            )
                        )
                    }
                }
            }
        }

        // Trending Tracks Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedMood == "All") "Trending Melodies" else "$selectedMood Melodies",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = RaagaTextPrimary
                )
                Text(
                    text = "${filteredSongs.size} tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = RaagaTextSecondary
                )
            }
        }

        // Track items
        itemsIndexed(filteredSongs) { index, song ->
            TrackItemRow(
                song = song,
                isCurrentSong = currentSong?.id == song.id,
                isPlaying = isPlaying,
                rankIndex = index + 1,
                onTrackClick = { onSongClick(song, filteredSongs) },
                onToggleFavorite = { onToggleFavorite(song) },
                onAddToPlaylistClick = { onAddToPlaylistClick(song) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun QuickPickCard(
    song: SongEntity,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(RaagaDarkSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumArtCard(
            gradientKey = song.colorGradientKey,
            size = 48.dp,
            shapeRadius = 6.dp,
            isPlaying = isPlaying
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            ),
            color = if (isPlaying) RaagaAmber else RaagaTextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PlaylistFeatureCard(
    playlist: PlaylistEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientColors = AlbumGradients.getOrElse(playlist.colorIndex % AlbumGradients.size) {
        listOf(RaagaAmber, RaagaViolet)
    }

    Column(
        modifier = modifier
            .width(155.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(RaagaDarkSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(135.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = playlist.name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            color = RaagaTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = playlist.description,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = RaagaTextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
