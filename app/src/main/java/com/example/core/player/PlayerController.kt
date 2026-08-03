package com.example.core.player

import com.example.core.database.entities.SongEntity
import kotlinx.coroutines.flow.StateFlow

interface PlayerController {
    val state: StateFlow<PlayerStateData>

    fun playSong(song: SongEntity, newQueue: List<SongEntity>? = null)
    fun playQueueIndex(index: Int)
    fun togglePlayPause()
    fun seekTo(positionMs: Long)
    fun skipNext()
    fun skipPrevious()
    fun fastForward(deltaMs: Long = 10000L)
    fun rewind(deltaMs: Long = 10000L)
    fun toggleShuffle()
    fun toggleRepeat()
    fun setVolume(volume: Float)
    fun setPlaybackSpeed(speed: Float)
    fun setPitch(pitch: Float)
    fun addToQueue(song: SongEntity)
    fun addNext(song: SongEntity)
    fun removeFromQueue(index: Int)
    fun reorderQueue(fromIndex: Int, toIndex: Int)
    fun clearQueue()
    fun retryPlayback()
}
