package com.glazev.playlistmaker

import android.app.Application
import com.glazev.playlistmaker.di.dataModule
import com.glazev.playlistmaker.di.interactorModule
import com.glazev.playlistmaker.di.repositoryModule
import com.glazev.playlistmaker.di.viewModelModule
import com.glazev.playlistmaker.domain.api.SettingsInteractor
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val koinApplication = startKoin {
            androidContext(this@App)
            modules(
                dataModule,
                repositoryModule,
                interactorModule,
                viewModelModule
            )
        }

        val settingsInteractor = koinApplication.koin.get<SettingsInteractor>()
        val themeSettings = settingsInteractor.getThemeSettings()
        settingsInteractor.updateThemeSettings(themeSettings)
    }

    companion object {
        const val PLAYLIST_MAKER_PREFERENCES = "playlist_maker_preferences"
    }
}
