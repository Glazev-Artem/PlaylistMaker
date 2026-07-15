package com.glazev.playlistmaker.domain.api

import com.glazev.playlistmaker.domain.models.ThemeSettings

interface SettingsRepository {
    fun getThemeSettings(): ThemeSettings
    fun updateThemeSettings(settings: ThemeSettings)
}
