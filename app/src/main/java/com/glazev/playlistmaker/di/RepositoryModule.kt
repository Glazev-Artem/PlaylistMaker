package com.glazev.playlistmaker.di

import com.glazev.playlistmaker.data.repository.PlayerRepositoryImpl
import com.glazev.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.glazev.playlistmaker.data.repository.SettingsRepositoryImpl
import com.glazev.playlistmaker.data.repository.TracksRepositoryImpl
import com.glazev.playlistmaker.domain.api.PlayerRepository
import com.glazev.playlistmaker.domain.api.SearchHistoryRepository
import com.glazev.playlistmaker.domain.api.SettingsRepository
import com.glazev.playlistmaker.domain.api.TracksRepository
import org.koin.dsl.module

val repositoryModule = module {

    factory<TracksRepository> {
        TracksRepositoryImpl(get())
    }

    factory<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(get(), get())
    }

    factory<SettingsRepository> {
        SettingsRepositoryImpl(get())
    }

    factory<PlayerRepository> {
        PlayerRepositoryImpl(get())
    }
}
