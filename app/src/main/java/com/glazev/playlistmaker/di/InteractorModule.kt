package com.glazev.playlistmaker.di

import com.glazev.playlistmaker.domain.api.PlayerInteractor
import com.glazev.playlistmaker.domain.api.SearchHistoryInteractor
import com.glazev.playlistmaker.domain.api.SettingsInteractor
import com.glazev.playlistmaker.domain.api.SharingInteractor
import com.glazev.playlistmaker.domain.api.TracksInteractor
import com.glazev.playlistmaker.domain.impl.PlayerInteractorImpl
import com.glazev.playlistmaker.domain.impl.SearchHistoryInteractorImpl
import com.glazev.playlistmaker.domain.impl.SettingsInteractorImpl
import com.glazev.playlistmaker.domain.impl.SharingInteractorImpl
import com.glazev.playlistmaker.domain.impl.TracksInteractorImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

val interactorModule = module {

    single<ExecutorService> {
        Executors.newCachedThreadPool()
    }

    single<TracksInteractor> {
        TracksInteractorImpl(get(), get())
    }

    single<SearchHistoryInteractor> {
        SearchHistoryInteractorImpl(get())
    }

    single<SettingsInteractor> {
        SettingsInteractorImpl(get())
    }

    single<SharingInteractor> {
        SharingInteractorImpl(
            externalNavigator = get(),
            shareAppLink = get(named(SHARE_APP_LINK_QUALIFIER)),
            termsLink = get(named(TERMS_LINK_QUALIFIER)),
            supportEmailData = get()
        )
    }

    factory<PlayerInteractor> {
        PlayerInteractorImpl(get())
    }
}
