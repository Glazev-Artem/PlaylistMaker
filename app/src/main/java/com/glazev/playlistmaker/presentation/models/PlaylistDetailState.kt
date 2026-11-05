package com.glazev.playlistmaker.presentation.models

import com.glazev.playlistmaker.domain.models.Playlist
import com.glazev.playlistmaker.domain.models.Track

data class PlaylistDetailState(
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = true,
    val isBusy: Boolean = false,
    val isMenuVisible: Boolean = false,
    val trackToRemove: Track? = null,
    val showDeleteConfirmation: Boolean = false
) {
    val durationMinutes: Long get() = tracks.sumOf { it.trackTimeMillis ?: 0L } / 60000
}

sealed interface PlaylistDetailEvent {
    data class OpenTrack(val track: Track) : PlaylistDetailEvent
    data class Edit(val playlistId: Long) : PlaylistDetailEvent
    data object Close : PlaylistDetailEvent
    data object EmptyShare : PlaylistDetailEvent
    data object Error : PlaylistDetailEvent
}
