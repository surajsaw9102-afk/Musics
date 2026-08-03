package com.example.core.player

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.core.catalog.MusicCatalog
import com.example.core.database.entities.SongEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import androidx.media3.common.PlaybackParameters
import com.example.core.player.lyrics.LyricsRepository

enum class RepeatModeState {
    OFF, ALL, ONE
}

data class PlayerStateData(
    val currentSong: SongEntity? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val isLiked: Boolean = false,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatModeState = RepeatModeState.OFF,
    val queue: List<SongEntity> = emptyList(),
    val currentIndex: Int = -1,
    val errorMessage: String? = null,
    val audioQuality: String = "Lossless FLAC 24-bit / 96kHz",
    val volume: Float = 1.0f
)

@OptIn(UnstableApi::class)
object AuraAudioPlayerManager : PlayerController {

    private var exoPlayer: ExoPlayer? = null
    private var appContext: Context? = null
    private var prefs: SharedPreferences? = null

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _state = MutableStateFlow(PlayerStateData())
    override val state: StateFlow<PlayerStateData> = _state.asStateFlow()

    private const val PREFS_NAME = "aura_player_prefs"
    private const val KEY_LAST_SONG_ID = "last_song_id"
    private const val KEY_LAST_POSITION = "last_position_ms"

    fun initialize(context: Context) {
        if (appContext != null) return
        val applicationContext = context.applicationContext
        appContext = applicationContext
        prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        getExoPlayer(applicationContext)
        restoreLastState()
    }

    fun getExoPlayer(context: Context): ExoPlayer {
        val player = exoPlayer ?: synchronized(this) {
            exoPlayer ?: buildExoPlayer(context.applicationContext).also {
                exoPlayer = it
            }
        }
        return player
    }

    private fun buildExoPlayer(context: Context): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val player = ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(isPlaying = isPlaying)
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                    saveCurrentPosition()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _state.value = _state.value.copy(
                            isBuffering = true,
                            errorMessage = null
                        )
                    }
                    Player.STATE_READY -> {
                        val duration = if (player.duration > 0) player.duration else _state.value.currentSong?.durationMs ?: 0L
                        _state.value = _state.value.copy(
                            isBuffering = false,
                            durationMs = duration,
                            bufferedMs = player.bufferedPosition,
                            errorMessage = null
                        )
                    }
                    Player.STATE_ENDED -> {
                        _state.value = _state.value.copy(
                            isPlaying = false,
                            isBuffering = false
                        )
                        handleSongEnded()
                    }
                    Player.STATE_IDLE -> {
                        _state.value = _state.value.copy(isBuffering = false)
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val errorMsg = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                        "Network error. Please check your internet connection."
                    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
                    PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                        "Audio track unavailable or stream link broken."
                    else -> "Playback error: ${error.localizedMessage ?: "Failed to stream audio"}"
                }

                _state.value = _state.value.copy(
                    isPlaying = false,
                    isBuffering = false,
                    errorMessage = errorMsg
                )
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val mediaId = mediaItem?.mediaId
                if (mediaId != null) {
                    val song = _state.value.queue.find { it.id == mediaId }
                        ?: MusicCatalog.getSongById(mediaId)
                    if (song != null) {
                        _state.value = _state.value.copy(
                            currentSong = song,
                            audioQuality = song.audioQuality,
                            durationMs = song.durationMs
                        )
                    }
                }
            }
        })

        return player
    }

    override fun playSong(song: SongEntity, newQueue: List<SongEntity>?) {
        val context = appContext ?: return
        val player = getExoPlayer(context)

        val queueList = if (!newQueue.isNullOrEmpty()) newQueue else {
            if (_state.value.queue.contains(song)) _state.value.queue else MusicCatalog.ALL_SONGS
        }

        val targetIndex = queueList.indexOfFirst { it.id == song.id }.let { if (it >= 0) it else 0 }

        _state.value = _state.value.copy(
            currentSong = song,
            queue = queueList,
            currentIndex = targetIndex,
            progressMs = 0L,
            durationMs = song.durationMs,
            audioQuality = song.audioQuality,
            errorMessage = null,
            isBuffering = true
        )

        scope.launch {
            LyricsRepository.loadLyricsForSong(song)
        }

        val mediaItem = buildMediaItem(song)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    override fun playQueueIndex(index: Int) {
        val queue = _state.value.queue
        if (index in queue.indices) {
            val song = queue[index]
            _state.value = _state.value.copy(currentIndex = index)
            playSong(song, queue)
        }
    }

    override fun togglePlayPause() {
        val context = appContext ?: return
        val player = getExoPlayer(context)

        if (_state.value.currentSong == null) {
            val defaultSong = MusicCatalog.ALL_SONGS.firstOrNull() ?: return
            playSong(defaultSong, MusicCatalog.ALL_SONGS)
            return
        }

        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0)
            }
            player.play()
        }
    }

    override fun seekTo(positionMs: Long) {
        val context = appContext ?: return
        val player = getExoPlayer(context)
        player.seekTo(positionMs)
        _state.value = _state.value.copy(progressMs = positionMs)
    }

    override fun fastForward(deltaMs: Long) {
        val context = appContext ?: return
        val player = getExoPlayer(context)
        val newPos = (player.currentPosition + deltaMs).coerceAtMost(player.duration.coerceAtLeast(0L))
        seekTo(newPos)
    }

    override fun rewind(deltaMs: Long) {
        val context = appContext ?: return
        val player = getExoPlayer(context)
        val newPos = (player.currentPosition - deltaMs).coerceAtLeast(0L)
        seekTo(newPos)
    }

    override fun skipNext() {
        val queue = _state.value.queue
        if (queue.isEmpty()) return

        var nextIndex = _state.value.currentIndex + 1

        if (_state.value.isShuffle) {
            nextIndex = (queue.indices).random()
        } else if (nextIndex >= queue.size) {
            if (_state.value.repeatMode == RepeatModeState.ALL) {
                nextIndex = 0
            } else {
                return
            }
        }

        playQueueIndex(nextIndex)
    }

    override fun skipPrevious() {
        val context = appContext ?: return
        val player = getExoPlayer(context)

        if (player.currentPosition > 3000) {
            player.seekTo(0)
            _state.value = _state.value.copy(progressMs = 0L)
            return
        }

        val queue = _state.value.queue
        if (queue.isEmpty()) return

        var prevIndex = _state.value.currentIndex - 1
        if (prevIndex < 0) {
            prevIndex = if (_state.value.repeatMode == RepeatModeState.ALL) queue.size - 1 else 0
        }

        playQueueIndex(prevIndex)
    }

    override fun toggleShuffle() {
        val newShuffle = !_state.value.isShuffle
        _state.value = _state.value.copy(isShuffle = newShuffle)
    }

    override fun toggleRepeat() {
        val nextRepeat = when (_state.value.repeatMode) {
            RepeatModeState.OFF -> RepeatModeState.ALL
            RepeatModeState.ALL -> RepeatModeState.ONE
            RepeatModeState.ONE -> RepeatModeState.OFF
        }
        _state.value = _state.value.copy(repeatMode = nextRepeat)
    }

    override fun addToQueue(song: SongEntity) {
        val updatedQueue = _state.value.queue.toMutableList().apply { add(song) }
        _state.value = _state.value.copy(queue = updatedQueue)
    }

    override fun addNext(song: SongEntity) {
        val currentIdx = _state.value.currentIndex
        val updatedQueue = _state.value.queue.toMutableList()
        val insertPos = if (currentIdx in updatedQueue.indices) currentIdx + 1 else updatedQueue.size
        updatedQueue.add(insertPos, song)
        _state.value = _state.value.copy(queue = updatedQueue)
    }

    override fun removeFromQueue(index: Int) {
        val currentQueue = _state.value.queue.toMutableList()
        if (index in currentQueue.indices) {
            currentQueue.removeAt(index)
            val currentIdx = _state.value.currentIndex
            val newIdx = when {
                index < currentIdx -> currentIdx - 1
                index == currentIdx -> currentIdx.coerceAtMost(currentQueue.size - 1)
                else -> currentIdx
            }
            _state.value = _state.value.copy(
                queue = currentQueue,
                currentIndex = newIdx
            )
        }
    }

    override fun reorderQueue(fromIndex: Int, toIndex: Int) {
        val currentQueue = _state.value.queue.toMutableList()
        if (fromIndex in currentQueue.indices && toIndex in currentQueue.indices) {
            val item = currentQueue.removeAt(fromIndex)
            currentQueue.add(toIndex, item)
            _state.value = _state.value.copy(queue = currentQueue)
        }
    }

    override fun clearQueue() {
        val currentSong = _state.value.currentSong
        val newQueue = if (currentSong != null) listOf(currentSong) else emptyList()
        _state.value = _state.value.copy(
            queue = newQueue,
            currentIndex = if (currentSong != null) 0 else -1
        )
    }

    override fun retryPlayback() {
        val song = _state.value.currentSong
        if (song != null) {
            playSong(song, _state.value.queue)
        }
    }

    override fun setVolume(volume: Float) {
        val context = appContext ?: return
        val player = getExoPlayer(context)
        val clamped = volume.coerceIn(0f, 1f)
        player.volume = clamped
        _state.value = _state.value.copy(volume = clamped)
    }

    override fun setPlaybackSpeed(speed: Float) {
        val context = appContext ?: return
        val player = getExoPlayer(context)
        val params = player.playbackParameters
        player.playbackParameters = PlaybackParameters(speed, params.pitch)
    }

    override fun setPitch(pitch: Float) {
        val context = appContext ?: return
        val player = getExoPlayer(context)
        val params = player.playbackParameters
        player.playbackParameters = PlaybackParameters(params.speed, pitch)
    }

    private fun handleSongEnded() {
        SleepTimerManager.onSongEnded {
            exoPlayer?.pause()
        }

        when (_state.value.repeatMode) {
            RepeatModeState.ONE -> {
                val song = _state.value.currentSong
                if (song != null) playSong(song, _state.value.queue)
            }
            RepeatModeState.ALL -> skipNext()
            RepeatModeState.OFF -> {
                if (_state.value.currentIndex < _state.value.queue.size - 1) {
                    skipNext()
                }
            }
        }
    }

    private fun buildMediaItem(song: SongEntity): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artistName)
            .setAlbumTitle(song.albumTitle)
            .setArtworkUri(Uri.parse(song.coverUrl))
            .build()

        return MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(Uri.parse(song.audioUrl))
            .setMediaMetadata(metadata)
            .build()
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        val currentPos = player.currentPosition
                        val buffered = player.bufferedPosition
                        val duration = if (player.duration > 0) player.duration else _state.value.durationMs
                        _state.value = _state.value.copy(
                            progressMs = currentPos,
                            bufferedMs = buffered,
                            durationMs = duration
                        )
                    }
                }
                delay(200)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun saveCurrentPosition() {
        val song = _state.value.currentSong ?: return
        val pos = _state.value.progressMs
        prefs?.edit()?.apply {
            putString(KEY_LAST_SONG_ID, song.id)
            putLong(KEY_LAST_POSITION, pos)
            apply()
        }
    }

    private fun restoreLastState() {
        val lastSongId = prefs?.getString(KEY_LAST_SONG_ID, null) ?: return
        val lastPos = prefs?.getLong(KEY_LAST_POSITION, 0L) ?: 0L
        val song = MusicCatalog.getSongById(lastSongId) ?: MusicCatalog.ALL_SONGS.firstOrNull() ?: return

        _state.value = _state.value.copy(
            currentSong = song,
            progressMs = lastPos,
            durationMs = song.durationMs,
            queue = MusicCatalog.ALL_SONGS,
            currentIndex = MusicCatalog.ALL_SONGS.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        )
    }
}
