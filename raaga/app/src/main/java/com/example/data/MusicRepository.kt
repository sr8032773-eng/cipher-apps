package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MusicRepository(private val dao: MusicDao) {

    val allSongs: Flow<List<SongEntity>> = dao.getAllSongs()
    val favoriteSongs: Flow<List<SongEntity>> = dao.getFavoriteSongs()
    val recentlyPlayedSongs: Flow<List<SongEntity>> = dao.getRecentlyPlayedSongs()
    val allPlaylists: Flow<List<PlaylistEntity>> = dao.getAllPlaylists()

    fun searchSongs(query: String): Flow<List<SongEntity>> = dao.searchSongs(query)

    fun getSongsByGenre(genre: String): Flow<List<SongEntity>> = dao.getSongsByGenre(genre)

    fun getPlaylistSongs(playlistId: Long): Flow<List<SongEntity>> = dao.getSongsForPlaylist(playlistId)

    suspend fun toggleFavorite(songId: String, currentFav: Boolean) {
        dao.setFavorite(songId, !currentFav)
    }

    suspend fun recordPlay(songId: String) {
        dao.recordPlay(songId, System.currentTimeMillis())
    }

    suspend fun createPlaylist(name: String, description: String, colorIndex: Int): Long {
        return dao.insertPlaylist(
            PlaylistEntity(
                name = name,
                description = description,
                colorIndex = colorIndex
            )
        )
    }

    suspend fun deletePlaylist(playlistId: Long) {
        dao.deletePlaylistItems(playlistId)
        dao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: String) {
        dao.addSongToPlaylist(PlaylistItemEntity(playlistId, songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        dao.removeSongFromPlaylist(playlistId, songId)
    }

    suspend fun ensureCatalogPopulated() {
        if (dao.getSongCount() == 0) {
            val songs = DefaultMusicCatalog.getInitialSongs()
            dao.insertSongs(songs)

            val p1 = dao.insertPlaylist(
                PlaylistEntity(
                    name = "Morning Awakening Ragas",
                    description = "Soulful dawn melodies to center the mind and elevate spirits",
                    colorIndex = 0
                )
            )
            val p2 = dao.insertPlaylist(
                PlaylistEntity(
                    name = "Deep Focus & Sitar Flow",
                    description = "Continuous meditative acoustic rhythms for deep study and work",
                    colorIndex = 1
                )
            )
            val p3 = dao.insertPlaylist(
                PlaylistEntity(
                    name = "Evening Golden Sunset",
                    description = "Warm acoustic Yaman and Bhupali twilight harmonies",
                    colorIndex = 2
                )
            )

            songs.take(4).forEach { dao.addSongToPlaylist(PlaylistItemEntity(p1, it.id)) }
            songs.drop(2).take(4).forEach { dao.addSongToPlaylist(PlaylistItemEntity(p2, it.id)) }
            songs.takeLast(4).forEach { dao.addSongToPlaylist(PlaylistItemEntity(p3, it.id)) }
        }
    }
}
