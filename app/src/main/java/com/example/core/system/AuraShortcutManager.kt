package com.example.core.system

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity

object AuraShortcutManager {

    const val ACTION_OPEN_ROUTE = "com.example.action.OPEN_ROUTE"
    const val ACTION_RESUME_LAST_SONG = "com.example.action.RESUME_LAST_SONG"
    const val EXTRA_ROUTE = "extra_route_target"

    fun updateShortcuts(context: Context) {
        try {
            val shortcuts = listOf(
                ShortcutInfoCompat.Builder(context, "shortcut_resume")
                    .setShortLabel("Resume Playing")
                    .setLongLabel("Resume your last played song")
                    .setIcon(IconCompat.createWithResource(context, android.R.drawable.ic_media_play))
                    .setIntent(
                        Intent(context, MainActivity::class.java).apply {
                            action = ACTION_RESUME_LAST_SONG
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    )
                    .build(),

                ShortcutInfoCompat.Builder(context, "shortcut_downloads")
                    .setShortLabel("Downloads")
                    .setLongLabel("Open Offline Music")
                    .setIcon(IconCompat.createWithResource(context, android.R.drawable.stat_sys_download_done))
                    .setIntent(
                        Intent(context, MainActivity::class.java).apply {
                            action = ACTION_OPEN_ROUTE
                            putExtra(EXTRA_ROUTE, "DOWNLOADS")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    )
                    .build(),

                ShortcutInfoCompat.Builder(context, "shortcut_library")
                    .setShortLabel("My Library")
                    .setLongLabel("Open Music Library")
                    .setIcon(IconCompat.createWithResource(context, android.R.drawable.ic_menu_myplaces))
                    .setIntent(
                        Intent(context, MainActivity::class.java).apply {
                            action = ACTION_OPEN_ROUTE
                            putExtra(EXTRA_ROUTE, "LIBRARY")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    )
                    .build(),

                ShortcutInfoCompat.Builder(context, "shortcut_search")
                    .setShortLabel("Search")
                    .setLongLabel("Search songs and artists")
                    .setIcon(IconCompat.createWithResource(context, android.R.drawable.ic_menu_search))
                    .setIntent(
                        Intent(context, MainActivity::class.java).apply {
                            action = ACTION_OPEN_ROUTE
                            putExtra(EXTRA_ROUTE, "SEARCH")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    )
                    .build()
            )

            ShortcutManagerCompat.addDynamicShortcuts(context, shortcuts)
        } catch (e: Exception) {
            // Ignore if OS does not support launcher shortcuts
        }
    }
}
