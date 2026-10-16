package com.glazev.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glazev.playlistmaker.domain.api.FavoriteTracksInteractor
import com.glazev.playlistmaker.domain.models.Track
import com.glazev.playlistmaker.presentation.models.Event
import com.glazev.playlistmaker.presentation.models.FavoriteTracksScreenState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FavoriteTracksViewModel(
    private val favoriteTracksInteractor: FavoriteTracksInteractor
) : ViewModel() {
    private val _screenState = MutableLiveData<FavoriteTracksScreenState>(FavoriteTracksScreenState.Empty)
    val screenState: LiveData<FavoriteTracksScreenState> = _screenState

    private val _openPlayerEvent = MutableLiveData<Event<Track>>()
    val openPlayerEvent: LiveData<Event<Track>> = _openPlayerEvent

    private var isClickAllowed = true

    init {
        viewModelScope.launch {
            favoriteTracksInteractor.getFavoriteTracks().collect { tracks ->
                _screenState.value = if (tracks.isEmpty()) {
                    FavoriteTracksScreenState.Empty
                } else {
                    FavoriteTracksScreenState.Content(tracks)
                }
            }
        }
    }

    fun onTrackClicked(track: Track) {
        if (!isClickAllowed) return
        isClickAllowed = false
        viewModelScope.launch {
            delay(CLICK_DEBOUNCE_DELAY)
            isClickAllowed = true
        }
        _openPlayerEvent.value = Event(track)
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}
