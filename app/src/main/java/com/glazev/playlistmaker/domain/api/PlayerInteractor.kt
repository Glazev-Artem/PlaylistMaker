package com.glazev.playlistmaker.domain.api

interface PlayerInteractor {
    fun preparePlayer(previewUrl: String, onPrepared: () -> Unit, onCompletion: () -> Unit)
    fun startPlayer()
    fun pausePlayer()
    fun releasePlayer()
    fun getCurrentPosition(): Int
    fun isPlaying(): Boolean
}
