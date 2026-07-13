package com.glazev.playlistmaker.domain.impl

import com.glazev.playlistmaker.domain.api.PlayerInteractor
import com.glazev.playlistmaker.domain.api.PlayerRepository

class PlayerInteractorImpl(private val repository: PlayerRepository) : PlayerInteractor {
    override fun preparePlayer(previewUrl: String, onPrepared: () -> Unit, onCompletion: () -> Unit) {
        repository.preparePlayer(previewUrl, onPrepared, onCompletion)
    }

    override fun startPlayer() {
        repository.startPlayer()
    }

    override fun pausePlayer() {
        repository.pausePlayer()
    }

    override fun releasePlayer() {
        repository.releasePlayer()
    }

    override fun getCurrentPosition(): Int {
        return repository.getCurrentPosition()
    }

    override fun isPlaying(): Boolean {
        return repository.isPlaying()
    }
}
