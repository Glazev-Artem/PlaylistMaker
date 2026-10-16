package com.glazev.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glazev.playlistmaker.domain.api.FavoriteTracksInteractor
import com.glazev.playlistmaker.domain.api.PlayerInteractor
import com.glazev.playlistmaker.domain.models.Track
import com.glazev.playlistmaker.presentation.models.PlayerScreenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class AudioPlayerViewModel(
    private val playerInteractor: PlayerInteractor,
    private val favoriteTracksInteractor: FavoriteTracksInteractor,
    track: Track
) : ViewModel() {

    private var timerJob: Job? = null
    private var playerState = PlayerState.DEFAULT
    private var isFavoriteUpdating = false

    private val _screenState = MutableLiveData(PlayerScreenState(track))
    val screenState: LiveData<PlayerScreenState> = _screenState

    init {
        preparePlayer(track.previewUrl)
        viewModelScope.launch {
            favoriteTracksInteractor.getFavoriteTracks().collect { tracks ->
                val state = _screenState.value ?: return@collect
                val isFavorite = tracks.any { it.trackId == state.track.trackId }
                state.track.isFavorite = isFavorite
                _screenState.value = state.copy(
                    isFavorite = isFavorite,
                    isFavoriteButtonEnabled = !isFavoriteUpdating
                )
            }
        }
    }

    fun onFavoriteClicked() {
        val state = _screenState.value ?: return
        if (!state.isFavoriteButtonEnabled || isFavoriteUpdating) return

        isFavoriteUpdating = true
        _screenState.value = state.copy(isFavoriteButtonEnabled = false)
        viewModelScope.launch {
            try {
                if (state.isFavorite) {
                    favoriteTracksInteractor.remove(state.track)
                } else {
                    favoriteTracksInteractor.add(state.track)
                }
                val currentState = _screenState.value ?: return@launch
                val isFavorite = !state.isFavorite
                currentState.track.isFavorite = isFavorite
                _screenState.value = currentState.copy(isFavorite = isFavorite)
            } finally {
                isFavoriteUpdating = false
                _screenState.value = _screenState.value?.copy(isFavoriteButtonEnabled = true)
            }
        }
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
                timerJob?.cancel()
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
        updateScreenState(
            isPlaying = true,
            playbackTime = formatTime(playerInteractor.getCurrentPosition())
        )
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (playerState == PlayerState.PLAYING) {
                delay(REFRESH_PLAYBACK_TIME_DELAY_MS)
                updateScreenState(
                    playbackTime = formatTime(playerInteractor.getCurrentPosition())
                )
            }
        }
    }

    private fun pausePlayer() {
        if (playerState != PlayerState.PLAYING) return

        playerInteractor.pausePlayer()
        playerState = PlayerState.PAUSED
        timerJob?.cancel()
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
        timerJob?.cancel()
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
