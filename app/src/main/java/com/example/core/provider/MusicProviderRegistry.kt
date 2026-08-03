package com.example.core.provider

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object MusicProviderRegistry {

    private val defaultProvider = DefaultMusicProvider()
    private val providersMap = mutableMapOf<String, MusicProvider>(
        defaultProvider.providerId to defaultProvider
    )

    private val _activeProvider = MutableStateFlow<MusicProvider>(defaultProvider)
    val activeProvider: StateFlow<MusicProvider> = _activeProvider.asStateFlow()

    fun registerProvider(provider: MusicProvider) {
        providersMap[provider.providerId] = provider
    }

    fun setActiveProvider(providerId: String): Boolean {
        val provider = providersMap[providerId] ?: return false
        _activeProvider.value = provider
        return true
    }

    fun getActive(): MusicProvider {
        return _activeProvider.value
    }

    fun getAvailableProviders(): List<MusicProvider> {
        return providersMap.values.toList()
    }
}
