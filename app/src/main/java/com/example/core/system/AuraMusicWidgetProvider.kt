package com.example.core.system

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.core.player.AuraAudioPlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuraMusicWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_PLAY_PAUSE = "com.example.action.WIDGET_PLAY_PAUSE"
        const val ACTION_WIDGET_NEXT = "com.example.action.WIDGET_NEXT"
        const val ACTION_WIDGET_PREV = "com.example.action.WIDGET_PREV"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, AuraMusicWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, AuraMusicWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val state = AuraAudioPlayerManager.state.value
        val song = state.currentSong
        val isPlaying = state.isPlaying

        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            val bitmap = NotificationArtworkLoader.loadArtworkBitmap(context, song?.coverUrl)

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_aura_player)

                views.setTextViewText(R.id.widget_title, song?.title ?: "Aura Music Player")
                views.setTextViewText(R.id.widget_artist, song?.artistName ?: "Tap to play music")

                if (bitmap != null) {
                    views.setImageViewBitmap(R.id.widget_cover, bitmap)
                } else {
                    views.setImageViewResource(R.id.widget_cover, android.R.drawable.ic_menu_gallery)
                }

                views.setImageViewResource(
                    R.id.widget_btn_play_pause,
                    if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                )

                // Open App Intent
                val appIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val appPendingIntent = PendingIntent.getActivity(
                    context, 0, appIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, appPendingIntent)

                // Play/Pause Intent
                val playIntent = Intent(context, AuraMusicWidgetProvider::class.java).apply {
                    action = ACTION_WIDGET_PLAY_PAUSE
                }
                val playPendingIntent = PendingIntent.getBroadcast(
                    context, 1, playIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_play_pause, playPendingIntent)

                // Next Intent
                val nextIntent = Intent(context, AuraMusicWidgetProvider::class.java).apply {
                    action = ACTION_WIDGET_NEXT
                }
                val nextPendingIntent = PendingIntent.getBroadcast(
                    context, 2, nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_next, nextPendingIntent)

                // Prev Intent
                val prevIntent = Intent(context, AuraMusicWidgetProvider::class.java).apply {
                    action = ACTION_WIDGET_PREV
                }
                val prevPendingIntent = PendingIntent.getBroadcast(
                    context, 3, prevIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_prev, prevPendingIntent)

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_WIDGET_PLAY_PAUSE -> {
                AuraAudioPlayerManager.togglePlayPause()
                updateAllWidgets(context)
            }
            ACTION_WIDGET_NEXT -> {
                AuraAudioPlayerManager.skipNext()
                updateAllWidgets(context)
            }
            ACTION_WIDGET_PREV -> {
                AuraAudioPlayerManager.skipPrevious()
                updateAllWidgets(context)
            }
        }
    }
}
