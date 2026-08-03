package com.example.core.state

import androidx.lifecycle.ViewModel
import com.example.core.designsystem.AuraThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeState : ViewModel() {
    private val _themeMode = MutableStateFlow(AuraThemeMode.DARK)
    val themeMode: StateFlow<AuraThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: AuraThemeMode) {
        _themeMode.value = mode
    }
}
