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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SongEntity
import com.example.ui.components.AlbumGradients
import com.example.ui.components.TrackItemRow
import com.example.ui.theme.RaagaAmber
import com.example.ui.theme.RaagaDarkSurface
import com.example.ui.theme.RaagaDarkSurfaceHighlight
import com.example.ui.theme.RaagaDarkSurfaceVariant
import com.example.ui.theme.RaagaTextPrimary
import com.example.ui.theme.RaagaTextSecondary

data class CategoryCardData(
    val title: String,
    val subtitle: String,
    val gradientIndex: Int,
    val filterQuery: String
)

val BrowseCategories = listOf(
    CategoryCardData("Hindustani Ragas", "Classical Morning & Night", 0, "Bhairav"),
    CategoryCardData("Carnatic Melodies", "Intricate Southern Scales", 1, "Carnatic"),
    CategoryCardData("Sitar & Sarod", "Royal Chamber Resonances", 2, "Sitar"),
    CategoryCardData("Acoustic Lo-Fi", "Gentle Rain & Plucks", 3, "Acoustic"),
    CategoryCardData("Monsoon Rain", "Megh Malhar Storms", 4, "Megh"),
    CategoryCardData("Twilight Yaman", "Evening Court Romance", 5, "Yaman"),
    CategoryCardData("Himalayan Flute", "Mountain Breeze & Pine", 2, "Pahari"),
    CategoryCardData("Deep Focus", "Meditative Continuous Flow", 1, "Focus")
)

@Composable
fun SearchScreen(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<SongEntity>,
    currentSong: SongEntity?,
    isPlaying: Boolean,
    onSongClick: (SongEntity, List<SongEntity>) -> Unit,
    onToggleFavorite: (SongEntity) -> Unit,
    onAddToPlaylistClick: (SongEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_search"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = RaagaTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    placeholder = { Text("What raga or melody do you want to play?") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = RaagaAmber
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = RaagaTextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = RaagaDarkSurfaceVariant,
                        unfocusedContainerColor = RaagaDarkSurfaceVariant,
                        focusedTextColor = RaagaTextPrimary,
                        unfocusedTextColor = RaagaTextPrimary,
                        focusedBorderColor = RaagaAmber,
                        unfocusedBorderColor = Color.Transparent,
                        focusedPlaceholderColor = RaagaTextSecondary,
                        unfocusedPlaceholderColor = RaagaTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = RaagaAmber.copy(alpha = 0.1f))
                        .testTag("search_text_input")
                )
            }
        }

        if (searchQuery.isNotEmpty()) {
            // Search Results Section
            item {
                Text(
                    text = "Search Results (${searchResults.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = RaagaTextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            if (searchResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = RaagaTextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No melodies found for \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RaagaTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(searchResults) { song ->
                    TrackItemRow(
                        song = song,
                        isCurrentSong = currentSong?.id == song.id,
                        isPlaying = isPlaying,
                        onTrackClick = { onSongClick(song, searchResults) },
                        onToggleFavorite = { onToggleFavorite(song) },
                        onAddToPlaylistClick = { onAddToPlaylistClick(song) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            // Browse Categories Header
            item {
                Text(
                    text = "Explore Ragas & Genres",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = RaagaTextPrimary,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)
                )
            }

            // 2-column grid of browse category cards
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    BrowseCategories.chunked(2).forEach { rowCategories ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowCategories.forEach { cat ->
                                BrowseCategoryCard(
                                    category = cat,
                                    onClick = { onQueryChange(cat.filterQuery) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrowseCategoryCard(
    category: CategoryCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientColors = AlbumGradients.getOrElse(category.gradientIndex % AlbumGradients.size) {
        listOf(RaagaAmber, RaagaDarkSurfaceVariant)
    }

    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(gradientColors))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.TopStart)) {
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = category.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.25f),
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.BottomEnd)
        )
    }
}
