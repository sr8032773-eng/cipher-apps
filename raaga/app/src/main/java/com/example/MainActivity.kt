package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MusicPlayerViewModel
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.EqualizerDialog
import com.example.ui.components.ExpandedNowPlayingModal
import com.example.ui.components.MiniPlayer
import com.example.ui.components.SleepTimerDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RaagaAmber
import com.example.ui.theme.RaagaDarkBackground
import com.example.ui.theme.RaagaDarkSurface
import com.example.ui.theme.RaagaDarkSurfaceHighlight
import com.example.ui.theme.RaagaTextPrimary
import com.example.ui.theme.RaagaTextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                RaagaMusicApp()
            }
        }
    }
}

@Composable
fun RaagaMusicApp(viewModel: MusicPlayerViewModel = viewModel()) {
    val allSongs by viewModel.allSongs.collectAsStateWithLifecycle()
    val favoriteSongs by viewModel.favoriteSongs.collectAsStateWithLifecycle()
    val recentlyPlayed by viewModel.recentlyPlayedSongs.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()

    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.audioEngine.isPlaying.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.audioEngine.currentPositionMs.collectAsStateWithLifecycle()
    val totalDurationMs by viewModel.audioEngine.totalDurationMs.collectAsStateWithLifecycle()
    val amplitudes by viewModel.audioEngine.waveformAmplitudes.collectAsStateWithLifecycle()
    val isShuffle by viewModel.isShuffle.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val currentVolume by viewModel.audioEngine.volume.collectAsStateWithLifecycle()
    val equalizerPreset by viewModel.audioEngine.equalizerPreset.collectAsStateWithLifecycle()
    val sleepTimerMinutes by viewModel.audioEngine.sleepTimerMinutesLeft.collectAsStateWithLifecycle()

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsStateWithLifecycle()
    val showLyrics by viewModel.showLyrics.collectAsStateWithLifecycle()
    val showEqualizer by viewModel.showEqualizer.collectAsStateWithLifecycle()
    val showSleepTimer by viewModel.showSleepTimer.collectAsStateWithLifecycle()
    val songForPlaylistAddition by viewModel.songForPlaylistAddition.collectAsStateWithLifecycle()
    val showCreatePlaylistModal by viewModel.showCreatePlaylistModal.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val selectedPlaylistSongs by viewModel.selectedPlaylistSongs.collectAsStateWithLifecycle()

    // Handle back button when now playing is expanded or playlist is open
    BackHandler(enabled = isNowPlayingExpanded || selectedPlaylist != null) {
        if (isNowPlayingExpanded) {
            viewModel.isNowPlayingExpanded.value = false
        } else if (selectedPlaylist != null) {
            viewModel.selectPlaylist(null)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = RaagaDarkBackground,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RaagaDarkBackground)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Mini Player (Only if a track is active)
                    currentSong?.let { song ->
                        MiniPlayer(
                            song = song,
                            isPlaying = isPlaying,
                            progressMs = currentPositionMs,
                            totalDurationMs = totalDurationMs,
                            amplitudes = amplitudes,
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onSkipNext = { viewModel.skipToNext() },
                            onToggleFavorite = { viewModel.toggleFavorite(song) },
                            onClick = { viewModel.isNowPlayingExpanded.value = true }
                        )
                    }

                    // Bottom Navigation Bar
                    NavigationBar(
                        containerColor = RaagaDarkSurface,
                        contentColor = RaagaTextPrimary,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("main_navigation_bar")
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = {
                                viewModel.selectedTab.value = 0
                                viewModel.selectPlaylist(null)
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "Home"
                                )
                            },
                            label = { Text("Home", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RaagaDarkBackground,
                                selectedTextColor = RaagaAmber,
                                unselectedIconColor = RaagaTextSecondary,
                                unselectedTextColor = RaagaTextSecondary,
                                indicatorColor = RaagaAmber
                            ),
                            modifier = Modifier.testTag("nav_item_home")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = {
                                viewModel.selectedTab.value = 1
                                viewModel.selectPlaylist(null)
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 1) Icons.Filled.Search else Icons.Outlined.Search,
                                    contentDescription = "Search"
                                )
                            },
                            label = { Text("Search", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RaagaDarkBackground,
                                selectedTextColor = RaagaAmber,
                                unselectedIconColor = RaagaTextSecondary,
                                unselectedTextColor = RaagaTextSecondary,
                                indicatorColor = RaagaAmber
                            ),
                            modifier = Modifier.testTag("nav_item_search")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { viewModel.selectedTab.value = 2 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 2) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic,
                                    contentDescription = "Your Library"
                                )
                            },
                            label = { Text("Your Library", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RaagaDarkBackground,
                                selectedTextColor = RaagaAmber,
                                unselectedIconColor = RaagaTextSecondary,
                                unselectedTextColor = RaagaTextSecondary,
                                indicatorColor = RaagaAmber
                            ),
                            modifier = Modifier.testTag("nav_item_library")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        allSongs = allSongs,
                        recentlyPlayed = recentlyPlayed,
                        playlists = playlists,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { song, list -> viewModel.playSong(song, list) },
                        onPlaylistClick = { pl ->
                            viewModel.selectPlaylist(pl)
                            viewModel.selectedTab.value = 2
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onAddToPlaylistClick = { viewModel.songForPlaylistAddition.value = it },
                        onOpenEqualizer = { viewModel.showEqualizer.value = true }
                    )

                    1 -> SearchScreen(
                        searchQuery = searchQuery,
                        onQueryChange = { viewModel.searchQuery.value = it },
                        searchResults = searchResults,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { song, list -> viewModel.playSong(song, list) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onAddToPlaylistClick = { viewModel.songForPlaylistAddition.value = it }
                    )

                    2 -> LibraryScreen(
                        playlists = playlists,
                        favoriteSongs = favoriteSongs,
                        recentlyPlayed = recentlyPlayed,
                        selectedPlaylist = selectedPlaylist,
                        selectedPlaylistSongs = selectedPlaylistSongs,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { song, list -> viewModel.playSong(song, list) },
                        onPlaylistClick = { viewModel.selectPlaylist(it) },
                        onBackFromPlaylist = { viewModel.selectPlaylist(null) },
                        onCreatePlaylistClick = { viewModel.showCreatePlaylistModal.value = true },
                        onDeletePlaylistClick = { viewModel.deletePlaylist(it) },
                        onRemoveSongFromPlaylist = { plId, songId -> viewModel.removeSongFromPlaylist(plId, songId) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onAddToPlaylistClick = { viewModel.songForPlaylistAddition.value = it }
                    )
                }
            }
        }

        // Full Screen Now Playing Animated Overlay
        AnimatedVisibility(
            visible = isNowPlayingExpanded && currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            currentSong?.let { song ->
                ExpandedNowPlayingModal(
                    song = song,
                    isPlaying = isPlaying,
                    progressMs = currentPositionMs,
                    totalDurationMs = totalDurationMs,
                    amplitudes = amplitudes,
                    isShuffle = isShuffle,
                    repeatMode = repeatMode,
                    currentVolume = currentVolume,
                    equalizerPreset = equalizerPreset,
                    sleepTimerMinutes = sleepTimerMinutes,
                    showLyrics = showLyrics,
                    onCollapse = { viewModel.isNowPlayingExpanded.value = false },
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onSkipNext = { viewModel.skipToNext() },
                    onSkipPrevious = { viewModel.skipToPrevious() },
                    onSeek = { viewModel.seekTo(it) },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onToggleRepeat = { viewModel.toggleRepeat() },
                    onToggleFavorite = { viewModel.toggleFavorite(song) },
                    onVolumeChange = { viewModel.audioEngine.setVolumeLevel(it) },
                    onToggleLyrics = { viewModel.showLyrics.value = !showLyrics },
                    onOpenEqualizer = { viewModel.showEqualizer.value = true },
                    onOpenSleepTimer = { viewModel.showSleepTimer.value = true },
                    onOpenAddToPlaylist = { viewModel.songForPlaylistAddition.value = song }
                )
            }
        }

        // Modals / Dialogs
        if (showEqualizer) {
            EqualizerDialog(
                currentPreset = equalizerPreset,
                onSelectPreset = {
                    viewModel.audioEngine.setPreset(it)
                    viewModel.showEqualizer.value = false
                },
                onDismiss = { viewModel.showEqualizer.value = false }
            )
        }

        if (showSleepTimer) {
            SleepTimerDialog(
                currentMinutesLeft = sleepTimerMinutes,
                onSetTimer = { viewModel.audioEngine.setSleepTimer(it) },
                onDismiss = { viewModel.showSleepTimer.value = false }
            )
        }

        songForPlaylistAddition?.let { song ->
            AddToPlaylistDialog(
                song = song,
                playlists = playlists,
                onSelectPlaylist = { pl ->
                    viewModel.addSongToPlaylist(pl.id, song.id)
                },
                onCreateNewPlaylistClick = {
                    viewModel.showCreatePlaylistModal.value = true
                },
                onDismiss = { viewModel.songForPlaylistAddition.value = null }
            )
        }

        if (showCreatePlaylistModal) {
            CreatePlaylistDialog(
                onCreate = { name, desc ->
                    viewModel.createPlaylist(name, desc)
                },
                onDismiss = { viewModel.showCreatePlaylistModal.value = false }
            )
        }
    }
}
