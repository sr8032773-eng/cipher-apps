package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val ragaOrGenre: String,
    val durationMs: Long,
    val moodTag: String,
    val tempoBpm: Int,
    val ragaScaleNotes: String, // Comma-separated frequencies or note names for synth
    val lyrics: String,
    val colorGradientKey: Int, // 0..5 gradient index
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
    val isFavorite: Boolean = false
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val colorIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_items", primaryKeys = ["playlistId", "songId"])
data class PlaylistItemEntity(
    val playlistId: Long,
    val songId: String,
    val addedAt: Long = System.currentTimeMillis()
)
