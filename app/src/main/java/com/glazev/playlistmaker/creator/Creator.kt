package com.glazev.playlistmaker.creator

import android.content.Context
import com.glazev.playlistmaker.App
import com.glazev.playlistmaker.data.network.RetrofitNetworkClient
import com.glazev.playlistmaker.data.repository.PlayerRepositoryImpl
import com.glazev.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.glazev.playlistmaker.data.repository.TracksRepositoryImpl
import com.glazev.playlistmaker.domain.api.PlayerInteractor
import com.glazev.playlistmaker.domain.api.PlayerRepository
import com.glazev.playlistmaker.domain.api.SearchHistoryInteractor
import com.glazev.playlistmaker.domain.api.SearchHistoryRepository
import com.glazev.playlistmaker.domain.api.TracksInteractor
import com.glazev.playlistmaker.domain.api.TracksRepository
import com.glazev.playlistmaker.domain.impl.PlayerInteractorImpl
import com.glazev.playlistmaker.domain.impl.SearchHistoryInteractorImpl
import com.glazev.playlistmaker.domain.impl.TracksInteractorImpl

object Creator {
    private fun getTracksRepository(): TracksRepository {
        return TracksRepositoryImpl(RetrofitNetworkClient())
    }

    fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(getTracksRepository())
    }

    private fun getPlayerRepository(): PlayerRepository {
        return PlayerRepositoryImpl()
    }

    fun providePlayerInteractor(): PlayerInteractor {
        return PlayerInteractorImpl(getPlayerRepository())
    }

    private fun getSearchHistoryRepository(context: Context): SearchHistoryRepository {
        return SearchHistoryRepositoryImpl(context.getSharedPreferences(App.PLAYLIST_MAKER_PREFERENCES, Context.MODE_PRIVATE))
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        return SearchHistoryInteractorImpl(getSearchHistoryRepository(context))
    }
}
