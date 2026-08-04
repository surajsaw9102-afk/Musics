package com.example.core.downloads

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AuraDownloadService : Service() {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var job: Job? = null

    companion object {
        const val CHANNEL_ID = "aura_download_channel"
        const val NOTIFICATION_ID = 2001

        fun startService(context: Context) {
            val intent = Intent(context, AuraDownloadService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AuraDownloadService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildInitialNotification())
        observeDownloads()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }

    private fun observeDownloads() {
        job = scope.launch {
            DownloadManager.state.collect { state ->
                val activeList = state.itemsMap.values.filter {
                    it.state == DownloadState.DOWNLOADING || it.state == DownloadState.QUEUED
                }

                if (activeList.isEmpty()) {
                    stopSelf()
                    return@collect
                }

                val currentItem = activeList.firstOrNull { it.state == DownloadState.DOWNLOADING } ?: activeList.first()
                val progress = (currentItem.progress * 100).toInt()
                val title = "Downloading ${currentItem.song.title}"
                val content = "${activeList.size} item(s) in queue • $progress%"

                val notification = NotificationCompat.Builder(this@AuraDownloadService, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setProgress(100, progress, currentItem.state == DownloadState.QUEUED)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build()

                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aura Music Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background download progress and notifications"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildInitialNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Aura Download Service")
            .setContentText("Initializing background queue...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .build()
    }
}
