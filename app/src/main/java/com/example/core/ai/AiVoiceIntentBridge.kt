package com.example.core.ai

object AiVoiceIntentBridge {

    fun processVoiceCommand(spokenText: String) {
        if (spokenText.isNotBlank()) {
            AiAssistantManager.sendPrompt(spokenText)
        }
    }
}
