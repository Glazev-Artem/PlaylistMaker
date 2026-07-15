package com.glazev.playlistmaker.data.repository

import android.content.SharedPreferences
import com.glazev.playlistmaker.domain.api.SettingsRepository
import com.glazev.playlistmaker.domain.models.ThemeSettings

class SettingsRepositoryImpl(private val sharedPrefs: SharedPreferences) : SettingsRepository {

    private val DARK_THEME_KEY = "dark_theme_key"

    override fun getThemeSettings(): ThemeSettings {
        val darkTheme = sharedPrefs.getBoolean(DARK_THEME_KEY, false)
        return ThemeSettings(darkTheme)
    }

    override fun updateThemeSettings(settings: ThemeSettings) {
        sharedPrefs.edit()
            .putBoolean(DARK_THEME_KEY, settings.darkTheme)
            .apply()
    }
}
