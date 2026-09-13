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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlaylistEntity
import com.example.data.SongEntity
import com.example.ui.components.AlbumGradients
import com.example.ui.components.TrackItemRow
import com.example.ui.theme.RaagaAmber
import com.example.ui.theme.RaagaDarkBackground
import com.example.ui.theme.RaagaDarkSurface
import com.example.ui.theme.RaagaDarkSurfaceHighlight
import com.example.ui.theme.RaagaDarkSurfaceVariant
import com.example.ui.theme.RaagaTextPrimary
import com.example.ui.theme.RaagaTextSecondary
import com.example.ui.theme.RaagaViolet

@Composable
fun LibraryScreen(
    playlists: List<PlaylistEntity>,
    favoriteSongs: List<SongEntity>,
    recentlyPlayed: List<SongEntity>,
    selectedPlaylist: PlaylistEntity?,
    selectedPlaylistSongs: List<SongEntity>,
    currentSong: SongEntity?,
    isPlaying: Boolean,
    onSongClick: (SongEntity, List<SongEntity>) -> Unit,
    onPlaylistClick: (PlaylistEntity) -> Unit,
    onBackFromPlaylist: () -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onDeletePlaylistClick: (Long) -> Unit,
    onRemoveSongFromPlaylist: (Long, String) -> Unit,
    onToggleFavorite: (SongEntity) -> Unit,
    onAddToPlaylistClick: (SongEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedPlaylist != null) {
        // Detail screen for a selected playlist
        PlaylistDetailView(
            playlist = selectedPlaylist,
            songs = selectedPlaylistSongs,
            currentSong = currentSong,
            isPlaying = isPlaying,
            onBack = onBackFromPlaylist,
            onSongClick = onSongClick,
            onDeletePlaylist = { onDeletePlaylistClick(selectedPlaylist.id) },
            onRemoveSong = { songId -> onRemoveSongFromPlaylist(selectedPlaylist.id, songId) },
            onToggleFavorite = onToggleFavorite,
            onAddToPlaylistClick = onAddToPlaylistClick,
            modifier = modifier
        )
        return
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Playlists", "Liked (${favoriteSongs.size})", "Recently Played")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_library"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Library",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = RaagaTextPrimary
                )

                IconButton(
                    onClick = onCreatePlaylistClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(RaagaAmber)
                        .testTag("btn_new_playlist")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Playlist",
                        tint = Color(0xFF1F1000),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Tab Row
        item {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = RaagaDarkBackground,
                contentColor = RaagaAmber,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = RaagaAmber
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (selectedTabIndex == index) RaagaAmber else RaagaTextSecondary
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        when (selectedTabIndex) {
            0 -> {
                // Playlists Tab
                if (playlists.isEmpty()) {
                    item {
                        EmptyLibraryPlaceholder(
                            title = "No Playlists Yet",
                            message = "Create a custom playlist to curate your favorite morning or evening melodies.",
                            actionLabel = "Create Playlist",
                            onAction = onCreatePlaylistClick
                        )
                    }
                } else {
                    items(playlists) { pl ->
                        PlaylistItemRow(
                            playlist = pl,
                            onClick = { onPlaylistClick(pl) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            1 -> {
                // Liked Songs Tab
                if (favoriteSongs.isEmpty()) {
                    item {
                        EmptyLibraryPlaceholder(
                            title = "No Liked Melodies",
                            message = "Tap the heart icon on any song to save it to your Liked collection.",
                            actionLabel = null,
                            onAction = {}
                        )
                    }
                } else {
                    item {
                        // Play All / Shuffle All Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${favoriteSongs.size} Liked Melodies",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = RaagaTextPrimary
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onSongClick(favoriteSongs.first(), favoriteSongs) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RaagaAmber)
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color(0xFF1E1000),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Play All", color = Color(0xFF1E1000), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    items(favoriteSongs) { song ->
                        TrackItemRow(
                            song = song,
                            isCurrentSong = currentSong?.id == song.id,
                            isPlaying = isPlaying,
                            onTrackClick = { onSongClick(song, favoriteSongs) },
                            onToggleFavorite = { onToggleFavorite(song) },
                            onAddToPlaylistClick = { onAddToPlaylistClick(song) },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            2 -> {
                // Recently Played Tab
                if (recentlyPlayed.isEmpty()) {
                    item {
                        EmptyLibraryPlaceholder(
                            title = "No History Yet",
                            message = "Songs you listen to will automatically appear here.",
                            actionLabel = null,
                            onAction = {}
                        )
                    }
                } else {
                    items(recentlyPlayed) { song ->
                        TrackItemRow(
                            song = song,
                            isCurrentSong = currentSong?.id == song.id,
                            isPlaying = isPlaying,
                            onTrackClick = { onSongClick(song, recentlyPlayed) },
                            onToggleFavorite = { onToggleFavorite(song) },
                            onAddToPlaylistClick = { onAddToPlaylistClick(song) },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistItemRow(
    playlist: PlaylistEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientColors = AlbumGradients.getOrElse(playlist.colorIndex % AlbumGradients.size) {
        listOf(RaagaAmber, RaagaViolet)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RaagaDarkSurfaceVariant),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = RaagaTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = playlist.description.ifEmpty { "Curated Playlist" },
                    style = MaterialTheme.typography.bodySmall,
                    color = RaagaTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = RaagaAmber,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PlaylistDetailView(
    playlist: PlaylistEntity,
    songs: List<SongEntity>,
    currentSong: SongEntity?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onSongClick: (SongEntity, List<SongEntity>) -> Unit,
    onDeletePlaylist: () -> Unit,
    onRemoveSong: (String) -> Unit,
    onToggleFavorite: (SongEntity) -> Unit,
    onAddToPlaylistClick: (SongEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientColors = AlbumGradients.getOrElse(playlist.colorIndex % AlbumGradients.size) {
        listOf(RaagaAmber, RaagaViolet)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_playlist_detail"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = RaagaTextPrimary
                    )
                }

                IconButton(onClick = onDeletePlaylist) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Playlist",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }

        // Hero Banner
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = RaagaTextPrimary
                )

                if (playlist.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = playlist.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = RaagaTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${songs.size} tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = RaagaAmber
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Play All & Shuffle Buttons
                if (songs.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Button(
                            onClick = { onSongClick(songs.first(), songs) },
                            colors = ButtonDefaults.buttonColors(containerColor = RaagaAmber)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF1E1000))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play All", color = Color(0xFF1E1000), fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onSongClick(songs.random(), songs) }
                        ) {
                            Icon(Icons.Default.Shuffle, null, tint = RaagaTextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Shuffle", color = RaagaTextPrimary)
                        }
                    }
                }
            }
        }

        // Song Items
        if (songs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No songs in this playlist yet. Add songs from Home or Search!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RaagaTextSecondary
                    )
                }
            }
        } else {
            items(songs) { song ->
                TrackItemRow(
                    song = song,
                    isCurrentSong = currentSong?.id == song.id,
                    isPlaying = isPlaying,
                    onTrackClick = { onSongClick(song, songs) },
                    onToggleFavorite = { onToggleFavorite(song) },
                    onAddToPlaylistClick = { onAddToPlaylistClick(song) },
                    onRemoveFromPlaylistClick = { onRemoveSong(song.id) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyLibraryPlaceholder(
    title: String,
    message: String,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = RaagaTextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = RaagaTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = RaagaTextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (actionLabel != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = RaagaAmber)
                ) {
                    Text(actionLabel, color = Color(0xFF1F1000), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
