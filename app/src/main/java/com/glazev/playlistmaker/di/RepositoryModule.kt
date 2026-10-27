package com.glazev.playlistmaker.di

import com.glazev.playlistmaker.data.repository.FavoriteTracksRepositoryImpl
import com.glazev.playlistmaker.data.repository.PlayerRepositoryImpl
import com.glazev.playlistmaker.data.repository.PlaylistsRepositoryImpl
import com.glazev.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.glazev.playlistmaker.data.repository.SettingsRepositoryImpl
import com.glazev.playlistmaker.data.repository.TracksRepositoryImpl
import com.glazev.playlistmaker.domain.api.FavoriteTracksRepository
import com.glazev.playlistmaker.domain.api.PlayerRepository
import com.glazev.playlistmaker.domain.api.PlaylistsRepository
import com.glazev.playlistmaker.domain.api.SearchHistoryRepository
import com.glazev.playlistmaker.domain.api.SettingsRepository
import com.glazev.playlistmaker.domain.api.TracksRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {

    single<PlaylistsRepository> {
        PlaylistsRepositoryImpl(get(), androidContext(), get())
    }

    single<FavoriteTracksRepository> {
        FavoriteTracksRepositoryImpl(get())
    }

    factory<TracksRepository> {
        TracksRepositoryImpl(get(), get())
    }

    factory<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(get(), get(), get())
    }

    factory<SettingsRepository> {
        SettingsRepositoryImpl(get())
    }

    factory<PlayerRepository> {
        PlayerRepositoryImpl(get())
    }
}
