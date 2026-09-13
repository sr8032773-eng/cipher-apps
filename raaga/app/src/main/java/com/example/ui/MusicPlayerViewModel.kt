package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.EqualizerPreset
import com.example.audio.RaagaAudioEngine
import com.example.audio.RepeatMode
import com.example.data.MusicRepository
import com.example.data.PlaylistEntity
import com.example.data.RaagaDatabase
import com.example.data.SongEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = RaagaDatabase.getDatabase(application, viewModelScope)
    private val repository = MusicRepository(database.musicDao())
    val audioEngine = RaagaAudioEngine(viewModelScope)

    val allSongs: StateFlow<List<SongEntity>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<SongEntity>> = repository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayedSongs: StateFlow<List<SongEntity>> = repository.recentlyPlayedSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current player state
    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    val currentSong: StateFlow<SongEntity?> = _currentSong.asStateFlow()

    private val _queue = MutableStateFlow<List<SongEntity>>(emptyList())
    val queue: StateFlow<List<SongEntity>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(0)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    // Navigation and UI state
    val selectedTab = MutableStateFlow(0) // 0: Home, 1: Search, 2: Library
    val isNowPlayingExpanded = MutableStateFlow(false)
    val showLyrics = MutableStateFlow(false)
    val showEqualizer = MutableStateFlow(false)
    val showSleepTimer = MutableStateFlow(false)
    val songForPlaylistAddition = MutableStateFlow<SongEntity?>(null)
    val showCreatePlaylistModal = MutableStateFlow(false)

    // Playlist detail view
    private val _selectedPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val selectedPlaylist: StateFlow<PlaylistEntity?> = _selectedPlaylist.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedPlaylistSongs: StateFlow<List<SongEntity>> = _selectedPlaylist
        .flatMapLatest { pl ->
            if (pl != null) repository.getPlaylistSongs(pl.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search query
    val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<SongEntity>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allSongs
            } else {
                repository.searchSongs(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureCatalogPopulated()
        }

        audioEngine.onTrackCompleted = {
            handleTrackFinished()
        }
    }

    fun playSong(song: SongEntity, songList: List<SongEntity>? = null) {
        val currentList = songList ?: allSongs.value.ifEmpty { listOf(song) }
        _queue.value = currentList
        val idx = currentList.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        _queueIndex.value = idx
        _currentSong.value = song

        viewModelScope.launch {
            repository.recordPlay(song.id)
        }

        audioEngine.playTrack(
            scaleNotes = song.ragaScaleNotes,
            bpm = song.tempoBpm,
            durationMs = song.durationMs
        )
    }

    fun togglePlayPause() {
        if (_currentSong.value == null) {
            val first = allSongs.value.firstOrNull() ?: return
            playSong(first)
            return
        }

        if (audioEngine.isPlaying.value) {
            audioEngine.pausePlayback()
        } else {
            audioEngine.resumePlayback()
        }
    }

    fun seekTo(positionMs: Long) {
        audioEngine.seekTo(positionMs)
    }

    fun skipToNext() {
        val q = _queue.value
        if (q.isEmpty()) return

        val nextIdx = if (_isShuffle.value) {
            q.indices.random()
        } else {
            (_queueIndex.value + 1) % q.size
        }
        val nextSong = q[nextIdx]
        _queueIndex.value = nextIdx
        _currentSong.value = nextSong

        viewModelScope.launch {
            repository.recordPlay(nextSong.id)
        }

        audioEngine.playTrack(
            scaleNotes = nextSong.ragaScaleNotes,
            bpm = nextSong.tempoBpm,
            durationMs = nextSong.durationMs
        )
    }

    fun skipToPrevious() {
        // If > 3 seconds in, restart current track
        if (audioEngine.currentPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }

        val q = _queue.value
        if (q.isEmpty()) return

        val prevIdx = if (_queueIndex.value - 1 < 0) q.size - 1 else _queueIndex.value - 1
        val prevSong = q[prevIdx]
        _queueIndex.value = prevIdx
        _currentSong.value = prevSong

        viewModelScope.launch {
            repository.recordPlay(prevSong.id)
        }

        audioEngine.playTrack(
            scaleNotes = prevSong.ragaScaleNotes,
            bpm = prevSong.tempoBpm,
            durationMs = prevSong.durationMs
        )
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun toggleFavorite(song: SongEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id, song.isFavorite)
            if (_currentSong.value?.id == song.id) {
                _currentSong.value = song.copy(isFavorite = !song.isFavorite)
            }
        }
    }

    private fun handleTrackFinished() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                val song = _currentSong.value ?: return
                audioEngine.playTrack(song.ragaScaleNotes, song.tempoBpm, song.durationMs, 0L)
            }
            RepeatMode.ALL -> {
                skipToNext()
            }
            RepeatMode.OFF -> {
                if (_queueIndex.value < _queue.value.size - 1) {
                    skipToNext()
                } else {
                    audioEngine.stopPlayback()
                }
            }
        }
    }

    fun selectPlaylist(playlist: PlaylistEntity?) {
        _selectedPlaylist.value = playlist
    }

    fun createPlaylist(name: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val colorIdx = (0..5).random()
            repository.createPlaylist(name.trim(), description.trim(), colorIdx)
            showCreatePlaylistModal.value = false
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_selectedPlaylist.value?.id == playlistId) {
                _selectedPlaylist.value = null
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
            songForPlaylistAddition.value = null
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
