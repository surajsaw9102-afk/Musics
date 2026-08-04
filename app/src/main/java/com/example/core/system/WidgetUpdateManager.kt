package com.example.core.system

import android.content.Context
import com.example.core.player.AuraAudioPlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object WidgetUpdateManager {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var isObserving = false

    fun startObserving(context: Context) {
        if (isObserving) return
        isObserving = true

        scope.launch {
            AuraAudioPlayerManager.state.collectLatest {
                AuraMusicWidgetProvider.updateAllWidgets(context.applicationContext)
            }
        }
    }
}
