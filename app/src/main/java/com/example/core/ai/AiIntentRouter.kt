package com.example.core.ai

object AiIntentRouter {

    fun routeIntent(prompt: String): IntentType {
        val lower = prompt.lowercase()
        return when {
            lower.contains("play") || lower.contains("listen") || lower.contains("stream") -> IntentType.PLAY_SONGS
            lower.contains("playlist") || lower.contains("make mix") || lower.contains("generate") -> IntentType.CREATE_PLAYLIST
            lower.contains("search") || lower.contains("find") || lower.contains("show") -> IntentType.SEARCH_FILTER
            lower.contains("mood") || lower.contains("feel") || lower.contains("vibe") -> IntentType.CHANGE_MOOD
            lower.contains("insight") || lower.contains("stats") || lower.contains("report") || lower.contains("history") -> IntentType.GET_INSIGHTS
            lower.contains("dj") || lower.contains("host") -> IntentType.START_DJ
            lower.contains("why") || lower.contains("explain") -> IntentType.EXPLAIN_TRACK
            lower.contains("open") || lower.contains("go to") || lower.contains("navigate") -> IntentType.NAVIGATE
            else -> IntentType.GENERAL_QNA
        }
    }
}
