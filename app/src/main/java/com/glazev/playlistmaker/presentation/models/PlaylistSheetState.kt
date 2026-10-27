package com.glazev.playlistmaker.presentation.models

import com.glazev.playlistmaker.domain.models.Playlist

data class PlaylistSheetState(
    val playlists: List<Playlist> = emptyList(),
    val isVisible: Boolean = false,
    val isAdding: Boolean = false
)

sealed interface PlaylistSheetEvent {
    data class Added(val playlistName: String) : PlaylistSheetEvent
    data class AlreadyAdded(val playlistName: String) : PlaylistSheetEvent
    data object CreatePlaylist : PlaylistSheetEvent
    data object Error : PlaylistSheetEvent
}
