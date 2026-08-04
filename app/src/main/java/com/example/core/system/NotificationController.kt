package com.example.core.system

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaStyleNotificationHelper
import com.example.MainActivity
import com.example.core.player.AuraAudioPlayerManager
import com.example.core.player.AuraAudioService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
object NotificationController {

    const val ACTION_PLAY = "com.example.action.NOTIF_PLAY"
    const val ACTION_PAUSE = "com.example.action.NOTIF_PAUSE"
    const val ACTION_NEXT = "com.example.action.NOTIF_NEXT"
    const val ACTION_PREVIOUS = "com.example.action.NOTIF_PREVIOUS"
    const val ACTION_STOP = "com.example.action.NOTIF_STOP"

    private val scope = CoroutineScope(Dispatchers.Main)

    suspend fun buildMediaNotification(
        context: Context,
        title: String,
        artist: String,
        coverUrl: String?,
        isPlaying: Boolean
    ): Notification {
        val bitmap = NotificationArtworkLoader.loadArtworkBitmap(context, coverUrl)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Previous Action
        val prevIntent = Intent(context, AuraAudioService::class.java).apply { action = ACTION_PREVIOUS }
        val prevPendingIntent = PendingIntent.getService(context, 10, prevIntent, PendingIntent.FLAG_IMMUTABLE)

        // Play/Pause Action
        val playPauseIntent = Intent(context, AuraAudioService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getService(context, 11, playPauseIntent, PendingIntent.FLAG_IMMUTABLE)

        // Next Action
        val nextIntent = Intent(context, AuraAudioService::class.java).apply { action = ACTION_NEXT }
        val nextPendingIntent = PendingIntent.getService(context, 12, nextIntent, PendingIntent.FLAG_IMMUTABLE)

        // Stop Action
        val stopIntent = Intent(context, AuraAudioService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(context, 13, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseTitle = if (isPlaying) "Pause" else "Play"

        val builder = NotificationCompat.Builder(context, AuraAudioService.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(artist)
            .setLargeIcon(bitmap)
            .setContentIntent(contentPendingIntent)
            .setDeleteIntent(stopPendingIntent)
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
            .addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)

        val session = MediaSessionManager.getSession()
        if (session != null) {
            builder.setStyle(
                MediaStyleNotificationHelper.MediaStyle(session)
                    .setShowActionsInCompactView(0, 1, 2)
            )
        }

        return builder.build()
    }

    fun updateNotification(context: Context) {
        val song = AuraAudioPlayerManager.state.value.currentSong ?: return
        val isPlaying = AuraAudioPlayerManager.state.value.isPlaying

        scope.launch {
            val notification = buildMediaNotification(
                context = context,
                title = song.title,
                artist = song.artistName,
                coverUrl = song.coverUrl,
                isPlaying = isPlaying
            )

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(AuraAudioService.NOTIFICATION_ID, notification)
        }
    }
}
