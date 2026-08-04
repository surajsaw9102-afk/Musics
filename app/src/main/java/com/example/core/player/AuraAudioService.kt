package com.example.core.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.core.system.NotificationController
import com.example.core.system.SystemControlBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@UnstableApi
class AuraAudioService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var serviceJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        val player = AuraAudioPlayerManager.getExoPlayer(this)

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()

        mediaSession?.let { com.example.core.system.MediaSessionManager.registerSession(it) }

        createNotificationChannel()
        observePlaybackState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            NotificationController.ACTION_PLAY -> {
                if (!AuraAudioPlayerManager.state.value.isPlaying) {
                    AuraAudioPlayerManager.togglePlayPause()
                }
            }
            NotificationController.ACTION_PAUSE -> {
                if (AuraAudioPlayerManager.state.value.isPlaying) {
                    AuraAudioPlayerManager.togglePlayPause()
                }
            }
            NotificationController.ACTION_NEXT -> {
                AuraAudioPlayerManager.skipNext()
            }
            NotificationController.ACTION_PREVIOUS -> {
                AuraAudioPlayerManager.skipPrevious()
            }
            NotificationController.ACTION_STOP -> {
                if (AuraAudioPlayerManager.state.value.isPlaying) {
                    AuraAudioPlayerManager.togglePlayPause()
                }
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        serviceJob?.cancel()
        com.example.core.system.MediaSessionManager.unregisterSession()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun observePlaybackState() {
        serviceJob = scope.launch {
            AuraAudioPlayerManager.state.collectLatest { state ->
                val song = state.currentSong
                if (song != null) {
                    val notification = NotificationController.buildMediaNotification(
                        context = this@AuraAudioService,
                        title = song.title,
                        artist = song.artistName,
                        coverUrl = song.coverUrl,
                        isPlaying = state.isPlaying
                    )
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Aura Music Player"
            val descriptionText = "Live music playback and audio controls"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "aura_audio_playback_channel"
        const val NOTIFICATION_ID = 1001
    }
}
