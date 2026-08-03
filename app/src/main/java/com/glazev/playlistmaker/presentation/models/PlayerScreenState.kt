package com.glazev.playlistmaker.presentation.models

import com.glazev.playlistmaker.domain.models.Track

data class PlayerScreenState(
    val track: Track,
    val isPlayButtonEnabled: Boolean = false,
    val isPlaying: Boolean = false,
    val playbackTime: String = INITIAL_PLAYBACK_TIME
) {
    companion object {
        const val INITIAL_PLAYBACK_TIME = "00:00"
    }
}
