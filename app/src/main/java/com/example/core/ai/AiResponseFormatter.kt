package com.example.core.ai

object AiResponseFormatter {

    fun formatTrackCount(count: Int): String {
        return "$count tracks • AI Curated"
    }

    fun formatDuration(totalMs: Long): String {
        val totalSec = totalMs / 1000
        val mins = totalSec / 60
        val secs = totalSec % 60
        return String.format("%d:%02d", mins, secs)
    }
}
