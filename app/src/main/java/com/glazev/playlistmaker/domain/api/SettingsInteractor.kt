package com.glazev.playlistmaker.domain.api

import com.glazev.playlistmaker.domain.models.ThemeSettings

interface SettingsInteractor {
    fun getThemeSettings(): ThemeSettings
    fun updateThemeSettings(settings: ThemeSettings)
}
