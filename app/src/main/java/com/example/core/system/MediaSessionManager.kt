package com.example.core.system

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import com.example.core.player.AuraAudioPlayerManager

@OptIn(UnstableApi::class)
object MediaSessionManager {

    private var activeSession: MediaSession? = null

    fun registerSession(mediaSession: MediaSession) {
        activeSession = mediaSession
    }

    fun unregisterSession() {
        activeSession?.release()
        activeSession = null
    }

    fun getSession(): MediaSession? = activeSession
}
