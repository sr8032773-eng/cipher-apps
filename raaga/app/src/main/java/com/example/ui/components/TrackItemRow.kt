package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SongEntity
import com.example.ui.theme.RaagaAmber
import com.example.ui.theme.RaagaDarkSurfaceHighlight
import com.example.ui.theme.RaagaDarkSurfaceVariant
import com.example.ui.theme.RaagaTextMuted
import com.example.ui.theme.RaagaTextPrimary
import com.example.ui.theme.RaagaTextSecondary

@Composable
fun TrackItemRow(
    song: SongEntity,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    onTrackClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onRemoveFromPlaylistClick: (() -> Unit)? = null,
    rankIndex: Int? = null,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCurrentSong) RaagaDarkSurfaceHighlight.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onTrackClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("track_item_row_${song.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track Rank or Thumbnail
        if (rankIndex != null) {
            Text(
                text = "$rankIndex",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = if (isCurrentSong) RaagaAmber else RaagaTextMuted,
                modifier = Modifier.width(28.dp)
            )
        }

        AlbumArtCard(
            gradientKey = song.colorGradientKey,
            size = 46.dp,
            shapeRadius = 10.dp,
            showVinylOverlay = false,
            isPlaying = isCurrentSong && isPlaying
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Artist
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 15.sp
                ),
                color = if (isCurrentSong) RaagaAmber else RaagaTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = RaagaTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = " • ${song.ragaOrGenre}",
                    style = MaterialTheme.typography.labelSmall,
                    color = RaagaTextMuted,
                    maxLines = 1
                )
            }
        }

        // Live Indicator or Duration
        if (isCurrentSong && isPlaying) {
            LiveWaveformBar(
                amplitudes = listOf(0.4f, 0.9f, 0.6f, 0.8f),
                maxHeight = 16.dp,
                barWidth = 2.dp,
                isPlaying = true
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else {
            Text(
                text = formatTime(song.durationMs),
                style = MaterialTheme.typography.bodySmall,
                color = RaagaTextMuted
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        // More Options Dropdown
        Box {
            IconButton(
                onClick = { showDropdown = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = RaagaTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false },
                modifier = Modifier.background(RaagaDarkSurfaceVariant)
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            if (song.isFavorite) "Remove from Liked" else "Save to Liked Songs",
                            color = RaagaTextPrimary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            null,
                            tint = RaagaAmber
                        )
                    },
                    onClick = {
                        showDropdown = false
                        onToggleFavorite()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Add to Playlist", color = RaagaTextPrimary) },
                    leadingIcon = { Icon(Icons.Default.PlaylistAdd, null, tint = RaagaTextPrimary) },
                    onClick = {
                        showDropdown = false
                        onAddToPlaylistClick()
                    }
                )

                if (onRemoveFromPlaylistClick != null) {
                    DropdownMenuItem(
                        text = { Text("Remove from this Playlist", color = androidx.compose.ui.graphics.Color(0xFFEF4444)) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = androidx.compose.ui.graphics.Color(0xFFEF4444)) },
                        onClick = {
                            showDropdown = false
                            onRemoveFromPlaylistClick()
                        }
                    )
                }
            }
        }
    }
}
