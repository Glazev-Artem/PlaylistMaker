package com.glazev.playlistmaker.creator

import android.content.Context
import android.media.MediaPlayer
import com.glazev.playlistmaker.App
import com.glazev.playlistmaker.data.network.ITunesApi
import com.glazev.playlistmaker.data.network.RetrofitNetworkClient
import com.glazev.playlistmaker.data.repository.PlayerRepositoryImpl
import com.glazev.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.glazev.playlistmaker.data.repository.SettingsRepositoryImpl
import com.glazev.playlistmaker.data.repository.TracksRepositoryImpl
import com.glazev.playlistmaker.data.sharing.ExternalNavigatorImpl
import com.glazev.playlistmaker.domain.api.PlayerInteractor
import com.glazev.playlistmaker.domain.api.PlayerRepository
import com.glazev.playlistmaker.domain.api.SearchHistoryInteractor
import com.glazev.playlistmaker.domain.api.SearchHistoryRepository
import com.glazev.playlistmaker.domain.api.SharingInteractor
import com.glazev.playlistmaker.domain.api.SettingsInteractor
import com.glazev.playlistmaker.domain.api.SettingsRepository
import com.glazev.playlistmaker.domain.api.TracksInteractor
import com.glazev.playlistmaker.domain.api.TracksRepository
import com.glazev.playlistmaker.domain.impl.PlayerInteractorImpl
import com.glazev.playlistmaker.domain.impl.SearchHistoryInteractorImpl
import com.glazev.playlistmaker.domain.impl.SharingInteractorImpl
import com.glazev.playlistmaker.domain.impl.SettingsInteractorImpl
import com.glazev.playlistmaker.domain.impl.TracksInteractorImpl
import com.glazev.playlistmaker.domain.models.EmailData
import com.glazev.playlistmaker.domain.models.Track
import com.glazev.playlistmaker.presentation.viewmodel.AudioPlayerViewModel
import com.glazev.playlistmaker.presentation.viewmodel.SearchViewModel
import com.glazev.playlistmaker.presentation.viewmodel.SettingsViewModel
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Creator {

    private val gson = Gson()

    private fun getITunesApi(): ITunesApi {
        return Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ITunesApi::class.java)
    }

    private fun getTracksRepository(): TracksRepository {
        return TracksRepositoryImpl(RetrofitNetworkClient(getITunesApi()))
    }

    fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(getTracksRepository())
    }

    private fun getPlayerRepository(): PlayerRepository {
        // Создаем НОВЫЙ MediaPlayer для каждого репозитория
        return PlayerRepositoryImpl(MediaPlayer())
    }

    fun providePlayerInteractor(): PlayerInteractor {
        return PlayerInteractorImpl(getPlayerRepository())
    }

    private fun getSearchHistoryRepository(context: Context): SearchHistoryRepository {
        return SearchHistoryRepositoryImpl(
            context.getSharedPreferences(App.PLAYLIST_MAKER_PREFERENCES, Context.MODE_PRIVATE),
            gson
        )
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        return SearchHistoryInteractorImpl(getSearchHistoryRepository(context))
    }

    private fun getSettingsRepository(context: Context): SettingsRepository {
        return SettingsRepositoryImpl(context.getSharedPreferences(App.PLAYLIST_MAKER_PREFERENCES, Context.MODE_PRIVATE))
    }

    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        return SettingsInteractorImpl(getSettingsRepository(context))
    }

    private fun provideSharingInteractor(context: Context): SharingInteractor {
        return SharingInteractorImpl(
            externalNavigator = ExternalNavigatorImpl(context.applicationContext),
            shareAppLink = context.getString(com.glazev.playlistmaker.R.string.share_app_link),
            termsLink = context.getString(com.glazev.playlistmaker.R.string.agreement_link),
            supportEmailData = EmailData(
                email = context.getString(com.glazev.playlistmaker.R.string.support_email),
                subject = context.getString(com.glazev.playlistmaker.R.string.support_subject),
                body = context.getString(com.glazev.playlistmaker.R.string.support_body)
            )
        )
    }

    fun provideSearchViewModelFactory(context: Context): ViewModelFactory {
        val applicationContext = context.applicationContext
        return ViewModelFactory {
            SearchViewModel(
                tracksInteractor = provideTracksInteractor(),
                searchHistoryInteractor = provideSearchHistoryInteractor(applicationContext)
            )
        }
    }

    fun provideSettingsViewModelFactory(context: Context): ViewModelFactory {
        val applicationContext = context.applicationContext
        return ViewModelFactory {
            SettingsViewModel(
                settingsInteractor = provideSettingsInteractor(applicationContext),
                sharingInteractor = provideSharingInteractor(applicationContext)
            )
        }
    }

    fun provideAudioPlayerViewModelFactory(track: Track): ViewModelFactory {
        return ViewModelFactory {
            AudioPlayerViewModel(
                playerInteractor = providePlayerInteractor(),
                track = track
            )
        }
    }
}
