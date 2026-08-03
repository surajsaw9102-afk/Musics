package com.example.feature.notifications

interface NotificationModule {
    fun showPlaybackNotification(title: String, artist: String, isPlaying: Boolean)
    fun cancelPlaybackNotification()
}
