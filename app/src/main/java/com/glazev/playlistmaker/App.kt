package com.glazev.playlistmaker

import android.app.Application
import com.glazev.playlistmaker.creator.Creator

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        val settingsInteractor = Creator.provideSettingsInteractor(this)
        val themeSettings = settingsInteractor.getThemeSettings()
        settingsInteractor.updateThemeSettings(themeSettings)
    }

    companion object {
        const val PLAYLIST_MAKER_PREFERENCES = "playlist_maker_preferences"
    }
}
