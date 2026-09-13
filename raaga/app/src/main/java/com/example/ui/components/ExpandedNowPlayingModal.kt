package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.EqualizerPreset
import com.example.audio.RepeatMode
import com.example.data.SongEntity
import com.example.ui.theme.RaagaAmber
import com.example.ui.theme.RaagaDarkBackground
import com.example.ui.theme.RaagaDarkSurfaceHighlight
import com.example.ui.theme.RaagaDarkSurfaceVariant
import com.example.ui.theme.RaagaEmerald
import com.example.ui.theme.RaagaTextMuted
import com.example.ui.theme.RaagaTextPrimary
import com.example.ui.theme.RaagaTextSecondary
import com.example.ui.theme.RaagaViolet
import java.util.Locale

@Composable
fun ExpandedNowPlayingModal(
    song: SongEntity,
    isPlaying: Boolean,
    progressMs: Long,
    totalDurationMs: Long,
    amplitudes: List<Float>,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    currentVolume: Float,
    equalizerPreset: EqualizerPreset,
    sleepTimerMinutes: Int?,
    showLyrics: Boolean,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleLyrics: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPosition by remember { mutableStateOf(0f) }
    var showMenu by remember { mutableStateOf(false) }

    val sliderValue = if (isScrubbing) {
        scrubPosition
    } else {
        if (totalDurationMs > 0) progressMs.toFloat() / totalDurationMs.toFloat() else 0f
    }

    val gradientColors = AlbumGradients.getOrElse(song.colorGradientKey % AlbumGradients.size) {
        listOf(RaagaAmber, RaagaViolet)
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("expanded_now_playing_screen"),
        color = RaagaDarkBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            gradientColors.first().copy(alpha = 0.35f),
                            RaagaDarkBackground.copy(alpha = 0.85f),
                            RaagaDarkBackground
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCollapse,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("now_playing_collapse")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse Player",
                            tint = RaagaTextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PLAYING FROM RAAGA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = RaagaAmber
                        )
                        Text(
                            text = song.album,
                            style = MaterialTheme.typography.bodySmall,
                            color = RaagaTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = RaagaTextPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(RaagaDarkSurfaceVariant)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add to Playlist", color = RaagaTextPrimary) },
                                leadingIcon = { Icon(Icons.Default.PlaylistAdd, null, tint = RaagaAmber) },
                                onClick = {
                                    showMenu = false
                                    onOpenAddToPlaylist()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sound Equalizer", color = RaagaTextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Equalizer, null, tint = RaagaViolet) },
                                onClick = {
                                    showMenu = false
                                    onOpenEqualizer()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sleep Timer", color = RaagaTextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Bedtime, null, tint = RaagaEmerald) },
                                onClick = {
                                    showMenu = false
                                    onOpenSleepTimer()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hero Vinyl Record / Album Art
                if (showLyrics) {
                    // Interactive Lyrics View
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(RaagaDarkSurfaceVariant.copy(alpha = 0.7f))
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = song.lyrics.ifEmpty { "Instrumental acoustic composition with rich harmonics." },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 28.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = RaagaTextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(270.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AlbumArtCard(
                            gradientKey = song.colorGradientKey,
                            size = 250.dp,
                            shapeRadius = 24.dp,
                            showVinylOverlay = true,
                            isPlaying = isPlaying
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Track Title & Favorite Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = RaagaTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp
                            ),
                            color = RaagaTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("now_playing_favorite")
                    ) {
                        Icon(
                            imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (song.isFavorite) "Liked" else "Like",
                            tint = if (song.isFavorite) RaagaAmber else RaagaTextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Raga & Scale Tag Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(RaagaDarkSurfaceHighlight)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${song.ragaOrGenre} • ${song.moodTag} • ${song.tempoBpm} BPM",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = RaagaAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Real-time Waveform Equalizer Display
                LiveWaveformBar(
                    amplitudes = amplitudes,
                    maxHeight = 28.dp,
                    barWidth = 4.dp,
                    isPlaying = isPlaying,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Scrubbing Progress Bar
                Slider(
                    value = sliderValue.coerceIn(0f, 1f),
                    onValueChange = {
                        isScrubbing = true
                        scrubPosition = it
                    },
                    onValueChangeFinished = {
                        isScrubbing = false
                        val targetMs = (scrubPosition * totalDurationMs).toLong()
                        onSeek(targetMs)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = RaagaAmber,
                        activeTrackColor = RaagaAmber,
                        inactiveTrackColor = RaagaDarkSurfaceHighlight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scrubber_slider")
                )

                // Timestamp display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentDisplayMs = if (isScrubbing) {
                        (scrubPosition * totalDurationMs).toLong()
                    } else progressMs

                    Text(
                        text = formatTime(currentDisplayMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = RaagaTextMuted
                    )
                    Text(
                        text = formatTime(totalDurationMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = RaagaTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Master Playback Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffle) RaagaAmber else RaagaTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Previous
                    IconButton(
                        onClick = onSkipPrevious,
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("btn_skip_previous")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = RaagaTextPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Play/Pause Master Floating Button
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(16.dp, CircleShape, spotColor = RaagaAmber.copy(alpha = 0.5f))
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(RaagaAmber, Color(0xFFFF7A00))
                                )
                            )
                            .clickable(onClick = onTogglePlayPause)
                            .testTag("btn_play_pause"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color(0xFF190C00),
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Next
                    IconButton(
                        onClick = onSkipNext,
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("btn_skip_next")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = RaagaTextPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Repeat mode
                    IconButton(
                        onClick = onToggleRepeat,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                            contentDescription = "Repeat",
                            tint = if (repeatMode != RepeatMode.OFF) RaagaAmber else RaagaTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Volume slider
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeDown,
                        contentDescription = "Volume Down",
                        tint = RaagaTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = currentVolume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = RaagaTextPrimary,
                            activeTrackColor = RaagaTextPrimary,
                            inactiveTrackColor = RaagaDarkSurfaceHighlight
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Volume Up",
                        tint = RaagaTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Pills (Lyrics, Equalizer, Sleep Timer)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Lyrics button
                    ActionPill(
                        icon = Icons.AutoMirrored.Filled.QueueMusic,
                        label = if (showLyrics) "Cover" else "Lyrics",
                        isActive = showLyrics,
                        activeColor = RaagaAmber,
                        onClick = onToggleLyrics
                    )

                    // Equalizer preset button
                    ActionPill(
                        icon = Icons.Default.Equalizer,
                        label = equalizerPreset.displayName,
                        isActive = true,
                        activeColor = RaagaViolet,
                        onClick = onOpenEqualizer
                    )

                    // Sleep Timer button
                    ActionPill(
                        icon = Icons.Default.Bedtime,
                        label = if (sleepTimerMinutes != null) "${sleepTimerMinutes}m left" else "Sleep",
                        isActive = sleepTimerMinutes != null,
                        activeColor = RaagaEmerald,
                        onClick = onOpenSleepTimer
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) activeColor.copy(alpha = 0.15f) else RaagaDarkSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) activeColor else RaagaTextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (isActive) activeColor else RaagaTextSecondary
        )
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
