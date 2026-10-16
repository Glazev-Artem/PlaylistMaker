package com.glazev.playlistmaker.presentation.models

import com.glazev.playlistmaker.domain.models.Track

sealed interface FavoriteTracksScreenState {
    data object Empty : FavoriteTracksScreenState
    data class Content(val tracks: List<Track>) : FavoriteTracksScreenState
}
