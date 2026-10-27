package com.glazev.playlistmaker.presentation.models

import com.glazev.playlistmaker.domain.models.Playlist

data class PlaylistsScreenState(val playlists: List<Playlist> = emptyList())
