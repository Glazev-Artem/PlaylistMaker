package com.glazev.playlistmaker.di

import com.glazev.playlistmaker.domain.models.Track
import com.glazev.playlistmaker.presentation.viewmodel.AudioPlayerViewModel
import com.glazev.playlistmaker.presentation.viewmodel.FavoriteTracksViewModel
import com.glazev.playlistmaker.presentation.viewmodel.MainViewModel
import com.glazev.playlistmaker.presentation.viewmodel.PlaylistsViewModel
import com.glazev.playlistmaker.presentation.viewmodel.SearchViewModel
import com.glazev.playlistmaker.presentation.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        MainViewModel()
    }

    viewModel {
        FavoriteTracksViewModel()
    }

    viewModel {
        PlaylistsViewModel()
    }

    viewModel {
        SearchViewModel(get(), get())
    }

    viewModel {
        SettingsViewModel(get(), get())
    }

    viewModel { (track: Track) ->
        AudioPlayerViewModel(get(), track)
    }
}
