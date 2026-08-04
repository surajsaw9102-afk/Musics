package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.example.core.player.AuraAudioPlayerManager
import com.example.core.system.AuraShortcutManager
import com.example.core.system.SystemControlBridge
import com.example.ui.AuraAppRoot

class MainActivity : ComponentActivity() {

    private val initialRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Phase 14 system controls (MediaSession, Shortcuts, AudioFocus, Widgets)
        SystemControlBridge.initialize(this)

        handleIntent(intent)

        setContent {
            AuraAppRoot(
                initialRouteName = initialRoute.value
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            AuraShortcutManager.ACTION_RESUME_LAST_SONG -> {
                if (!AuraAudioPlayerManager.state.value.isPlaying) {
                    AuraAudioPlayerManager.togglePlayPause()
                }
            }
            AuraShortcutManager.ACTION_OPEN_ROUTE -> {
                val routeStr = intent.getStringExtra(AuraShortcutManager.EXTRA_ROUTE)
                if (!routeStr.isNullOrBlank()) {
                    initialRoute.value = routeStr
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SystemControlBridge.cleanUp()
    }
}
