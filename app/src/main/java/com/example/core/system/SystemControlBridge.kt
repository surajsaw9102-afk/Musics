package com.example.core.system

import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.core.player.AuraAudioPlayerManager
import com.example.core.player.AuraAudioService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object SystemControlBridge {

    private var interruptHandler: PlaybackInterruptHandler? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var isInitialized = false

    @OptIn(UnstableApi::class)
    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        val appContext = context.applicationContext

        // 1. Initialize Player Manager
        AuraAudioPlayerManager.initialize(appContext)

        // 2. Initialize Interrupt Handler (Audio Focus & Noisy Headphones)
        interruptHandler = PlaybackInterruptHandler(appContext).apply {
            register()
        }

        // 3. Setup App Shortcuts
        AuraShortcutManager.updateShortcuts(appContext)

        // 4. Observe Player State for Widgets and Service
        WidgetUpdateManager.startObserving(appContext)

        scope.launch {
            AuraAudioPlayerManager.state.collectLatest { state ->
                if (state.isPlaying) {
                    val serviceIntent = Intent(appContext, AuraAudioService::class.java)
                    try {
                        appContext.startService(serviceIntent)
                    } catch (e: Exception) {
                        // Ignore if Android background start limits apply
                    }
                }
            }
        }
    }

    fun cleanUp() {
        interruptHandler?.unregister()
        interruptHandler = null
        isInitialized = false
    }
}
