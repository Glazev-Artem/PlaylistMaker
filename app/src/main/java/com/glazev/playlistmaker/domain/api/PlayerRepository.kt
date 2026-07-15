package com.glazev.playlistmaker.domain.api

interface PlayerRepository {
    fun preparePlayer(previewUrl: String, onPrepared: () -> Unit, onCompletion: () -> Unit)
    fun startPlayer()
    fun pausePlayer()
    fun releasePlayer()
    fun getCurrentPosition(): Int
    fun isPlaying(): Boolean
}
