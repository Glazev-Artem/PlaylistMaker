package com.glazev.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glazev.playlistmaker.domain.api.PlaylistsInteractor
import com.glazev.playlistmaker.presentation.models.PlaylistsScreenState
import kotlinx.coroutines.launch

class PlaylistsViewModel(private val playlistsInteractor: PlaylistsInteractor) : ViewModel() {
    private val _screenState = MutableLiveData(PlaylistsScreenState())
    val screenState: LiveData<PlaylistsScreenState> = _screenState

    init {
        viewModelScope.launch {
            playlistsInteractor.getPlaylists().collect { playlists ->
                _screenState.value = PlaylistsScreenState(playlists)
            }
        }
    }
}
