package com.example.localliy.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val prefsManager = ThemePreferencesManager(application)

    private val _themeState = MutableStateFlow(prefsManager.getThemeState())
    val themeState: StateFlow<ThemeState> = _themeState.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        prefsManager.themeMode = mode
        _themeState.value = _themeState.value.copy(themeMode = mode)
    }

    fun setAccentColor(accentColor: AccentColor) {
        prefsManager.accentColor = accentColor
        _themeState.value = _themeState.value.copy(accentColor = accentColor)
    }
}
