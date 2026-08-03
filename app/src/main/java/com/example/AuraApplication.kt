package com.example

import android.app.Application
import com.example.core.player.AuraAudioPlayerManager

class AuraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize singleton audio player manager
        AuraAudioPlayerManager.initialize(this)
    }
}
