package com.glazev.playlistmaker.presentation.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.glazev.playlistmaker.domain.api.PlayerInteractor
import com.glazev.playlistmaker.domain.models.Track
import com.glazev.playlistmaker.presentation.models.PlayerScreenState
import java.util.Locale

class AudioPlayerViewModel(
    private val playerInteractor: PlayerInteractor,
    track: Track
) : ViewModel() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var playerState = PlayerState.DEFAULT

    private val _screenState = MutableLiveData(PlayerScreenState(track))
    val screenState: LiveData<PlayerScreenState> = _screenState

    private val updatePlaybackTimeRunnable = object : Runnable {
        override fun run() {
            if (playerState != PlayerState.PLAYING) return

            updateScreenState(
                isPlaying = true,
                playbackTime = formatTime(playerInteractor.getCurrentPosition())
            )
            mainHandler.postDelayed(this, REFRESH_PLAYBACK_TIME_DELAY_MS)
        }
    }

    init {
        preparePlayer(track.previewUrl)
    }

    fun onPlaybackControlClicked() {
        when (playerState) {
            PlayerState.PLAYING -> pausePlayer()
            PlayerState.PREPARED, PlayerState.PAUSED -> startPlayer()
            PlayerState.DEFAULT -> Unit
        }
    }

    fun onScreenPaused() {
        pausePlayer()
    }

    private fun preparePlayer(previewUrl: String?) {
        if (previewUrl == null) return

        playerInteractor.preparePlayer(
            previewUrl,
            onPrepared = {
                playerState = PlayerState.PREPARED
                updateScreenState(isPlayButtonEnabled = true, isPlaying = false)
            },
            onCompletion = {
                playerState = PlayerState.PREPARED
                mainHandler.removeCallbacks(updatePlaybackTimeRunnable)
                updateScreenState(
                    isPlayButtonEnabled = true,
                    isPlaying = false,
                    playbackTime = PlayerScreenState.INITIAL_PLAYBACK_TIME
                )
            }
        )
    }

    private fun startPlayer() {
        playerInteractor.startPlayer()
        playerState = PlayerState.PLAYING
        updateScreenState(isPlaying = true)
        mainHandler.post(updatePlaybackTimeRunnable)
    }

    private fun pausePlayer() {
        if (playerState != PlayerState.PLAYING) return

        playerInteractor.pausePlayer()
        playerState = PlayerState.PAUSED
        mainHandler.removeCallbacks(updatePlaybackTimeRunnable)
        updateScreenState(isPlaying = false)
    }

    private fun updateScreenState(
        isPlayButtonEnabled: Boolean = _screenState.value?.isPlayButtonEnabled ?: false,
        isPlaying: Boolean = _screenState.value?.isPlaying ?: false,
        playbackTime: String = _screenState.value?.playbackTime
            ?: PlayerScreenState.INITIAL_PLAYBACK_TIME
    ) {
        val currentState = _screenState.value ?: return
        _screenState.value = currentState.copy(
            isPlayButtonEnabled = isPlayButtonEnabled,
            isPlaying = isPlaying,
            playbackTime = playbackTime
        )
    }

    private fun formatTime(positionMillis: Int): String {
        val totalSeconds = positionMillis / 1000
        return String.format(
            Locale.getDefault(),
            "%02d:%02d",
            totalSeconds / 60,
            totalSeconds % 60
        )
    }

    override fun onCleared() {
        mainHandler.removeCallbacksAndMessages(null)
        playerInteractor.releasePlayer()
    }

    private enum class PlayerState {
        DEFAULT,
        PREPARED,
        PLAYING,
        PAUSED
    }

    companion object {
        private const val REFRESH_PLAYBACK_TIME_DELAY_MS = 300L
    }
}
