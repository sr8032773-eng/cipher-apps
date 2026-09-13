package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

enum class EqualizerPreset(val displayName: String, val bassGain: Float, val midGain: Float, val trebleGain: Float) {
    STUDIO_FLAT("Studio Flat", 1.0f, 1.0f, 1.0f),
    BASS_BOOST("Bass Boost", 1.5f, 1.0f, 0.8f),
    RAGA_ACOUSTIC("Raga Acoustic", 1.1f, 1.4f, 1.2f),
    VOCAL_CLARITY("Vocal Clarity", 0.9f, 1.5f, 1.3f),
    NIGHT_MEDITATION("Night Calm", 1.2f, 0.9f, 0.7f)
}

enum class RepeatMode {
    OFF, ALL, ONE
}

class RaagaAudioEngine(private val scope: CoroutineScope) {

    private val sampleRate = 22050
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(180000L)
    val totalDurationMs: StateFlow<Long> = _totalDurationMs.asStateFlow()

    private val _waveformAmplitudes = MutableStateFlow(List(16) { 0.2f })
    val waveformAmplitudes: StateFlow<List<Float>> = _waveformAmplitudes.asStateFlow()

    private val _equalizerPreset = MutableStateFlow(EqualizerPreset.RAGA_ACOUSTIC)
    val equalizerPreset: StateFlow<EqualizerPreset> = _equalizerPreset.asStateFlow()

    private val _volume = MutableStateFlow(0.85f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _sleepTimerMinutesLeft = MutableStateFlow<Int?>(null)
    val sleepTimerMinutesLeft: StateFlow<Int?> = _sleepTimerMinutesLeft.asStateFlow()

    var onTrackCompleted: (() -> Unit)? = null

    private var currentNotes: List<Double> = listOf(261.63, 293.66, 329.63, 392.00, 440.00)
    private var currentBpm: Int = 75

    fun playTrack(scaleNotes: String, bpm: Int, durationMs: Long, startFromMs: Long = 0L) {
        stopPlayback()

        currentNotes = scaleNotes.split(",")
            .mapNotNull { it.trim().toDoubleOrNull() }
            .ifEmpty { listOf(261.63, 293.66, 329.63, 392.00, 440.00) }
        currentBpm = bpm.coerceIn(50, 180)
        _totalDurationMs.value = durationMs
        _currentPositionMs.value = startFromMs

        initAudioTrack()
        _isPlaying.value = true

        startPlaybackLoop()
        startProgressLoop()
    }

    fun resumePlayback() {
        if (!_isPlaying.value && audioTrack != null) {
            _isPlaying.value = true
            startPlaybackLoop()
            startProgressLoop()
        }
    }

    fun pausePlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        progressJob?.cancel()
        progressJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (e: Exception) {
            Log.e("RaagaAudioEngine", "Error pausing AudioTrack", e)
        }
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _totalDurationMs.value)
        _currentPositionMs.value = clamped
    }

    fun setVolumeLevel(vol: Float) {
        val clamped = vol.coerceIn(0.0f, 1.0f)
        _volume.value = clamped
        try {
            audioTrack?.setVolume(clamped)
        } catch (_: Exception) {}
    }

    fun setPreset(preset: EqualizerPreset) {
        _equalizerPreset.value = preset
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        _sleepTimerMinutesLeft.value = minutes
        if (minutes == null || minutes <= 0) return

        sleepTimerJob = scope.launch(Dispatchers.Default) {
            var mins = minutes
            while (mins > 0 && isActive) {
                delay(60000L)
                mins -= 1
                _sleepTimerMinutesLeft.value = mins
            }
            if (isActive) {
                pausePlayback()
                _sleepTimerMinutesLeft.value = null
            }
        }
    }

    private fun initAudioTrack() {
        try {
            audioTrack?.release()
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.setVolume(_volume.value)
            audioTrack?.play()
        } catch (e: Exception) {
            Log.e("RaagaAudioEngine", "Failed to initialize AudioTrack", e)
        }
    }

    private fun startPlaybackLoop() {
        playbackJob?.cancel()
        playbackJob = scope.launch(Dispatchers.Default) {
            val track = audioTrack ?: return@launch
            val notes = currentNotes
            var noteIndex = 0
            val beatDurationSec = 60.0 / currentBpm
            val samplesPerNote = (sampleRate * beatDurationSec).toInt()
            val buffer = ShortArray(samplesPerNote)

            // Drone fundamental frequency (Sa base at 130.81 Hz C3)
            val droneFreq = (notes.firstOrNull() ?: 261.63) * 0.5
            val dronePaFreq = droneFreq * 1.5 // fifth

            while (isActive && _isPlaying.value) {
                val targetNote = notes[noteIndex % notes.size]
                val eq = _equalizerPreset.value
                val vol = _volume.value

                for (i in 0 until samplesPerNote) {
                    val t = i.toDouble() / sampleRate
                    // Envelope: pluck attack + gentle exponential decay
                    val env = kotlin.math.exp(-3.0 * (i.toDouble() / samplesPerNote)).toFloat()

                    // Melodic note synthesis with rich harmonics
                    val melodicWave = (
                        sin(2.0 * PI * targetNote * t) * 0.6 +
                        sin(2.0 * PI * (targetNote * 2.0) * t) * 0.25 * eq.midGain +
                        sin(2.0 * PI * (targetNote * 3.0) * t) * 0.12 * eq.trebleGain
                    ).toFloat()

                    // Tanpura drone bed (Sa + Pa gentle foundation)
                    val droneWave = (
                        sin(2.0 * PI * droneFreq * t) * 0.25 * eq.bassGain +
                        sin(2.0 * PI * dronePaFreq * t) * 0.18
                    ).toFloat()

                    val sampleValue = ((melodicWave * env + droneWave) * vol * 0.45f).coerceIn(-1.0f, 1.0f)
                    buffer[i] = (sampleValue * Short.MAX_VALUE).toInt().toShort()
                }

                // Update visualizer amplitudes
                val amps = List(16) { bar ->
                    val factor = ((sin(noteIndex * 0.6 + bar * 0.4) + 1.0) * 0.4 + 0.2).toFloat()
                    (factor * vol).coerceIn(0.1f, 1.0f)
                }
                _waveformAmplitudes.value = amps

                try {
                    track.write(buffer, 0, buffer.size)
                } catch (e: Exception) {
                    Log.e("RaagaAudioEngine", "AudioTrack write error", e)
                    break
                }

                noteIndex++
            }
        }
    }

    private fun startProgressLoop() {
        progressJob?.cancel()
        progressJob = scope.launch(Dispatchers.Default) {
            while (isActive && _isPlaying.value) {
                delay(200L)
                val newPos = _currentPositionMs.value + 200L
                if (newPos >= _totalDurationMs.value) {
                    _currentPositionMs.value = _totalDurationMs.value
                    _isPlaying.value = false
                    playbackJob?.cancel()
                    onTrackCompleted?.invoke()
                    break
                } else {
                    _currentPositionMs.value = newPos
                }
            }
        }
    }

    fun stopPlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        progressJob?.cancel()
        progressJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        _waveformAmplitudes.value = List(16) { 0.15f }
    }

    fun release() {
        stopPlayback()
        sleepTimerJob?.cancel()
    }
}
