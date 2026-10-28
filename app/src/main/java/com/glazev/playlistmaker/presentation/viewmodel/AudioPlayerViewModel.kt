package com.glazev.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glazev.playlistmaker.domain.api.FavoriteTracksInteractor
import com.glazev.playlistmaker.domain.api.PlayerInteractor
import com.glazev.playlistmaker.domain.api.PlaylistsInteractor
import com.glazev.playlistmaker.domain.models.Playlist
import com.glazev.playlistmaker.domain.models.Track
import com.glazev.playlistmaker.presentation.models.Event
import com.glazev.playlistmaker.presentation.models.PlaylistSheetEvent
import com.glazev.playlistmaker.presentation.models.PlaylistSheetState
import com.glazev.playlistmaker.presentation.models.PlayerScreenState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class AudioPlayerViewModel(
    private val playerInteractor: PlayerInteractor,
    private val favoriteTracksInteractor: FavoriteTracksInteractor,
    private val playlistsInteractor: PlaylistsInteractor,
    track: Track
) : ViewModel() {

    private var timerJob: Job? = null
    private var playerState = PlayerState.DEFAULT
    private var isFavoriteUpdating = false
    private var playlistsJob: Job? = null

    private val _screenState = MutableLiveData(PlayerScreenState(track))
    val screenState: LiveData<PlayerScreenState> = _screenState

    private val _playlistSheetState = MutableLiveData(PlaylistSheetState())
    val playlistSheetState: LiveData<PlaylistSheetState> = _playlistSheetState

    private val _playlistSheetEvents = MutableLiveData<Event<PlaylistSheetEvent>>()
    val playlistSheetEvents: LiveData<Event<PlaylistSheetEvent>> = _playlistSheetEvents

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

    fun onAddToPlaylistClicked() {
        _playlistSheetState.value = _playlistSheetState.value?.copy(isVisible = true)
        playlistsJob?.cancel()
        playlistsJob = viewModelScope.launch {
            playlistsInteractor.getPlaylists().collect { playlists ->
                _playlistSheetState.value = _playlistSheetState.value?.copy(playlists = playlists)
            }
        }
    }

    fun onPlaylistSheetHidden() {
        _playlistSheetState.value = _playlistSheetState.value?.copy(isVisible = false)
        playlistsJob?.cancel()
    }

    fun onNewPlaylistClicked() {
        onPlaylistSheetHidden()
        _playlistSheetEvents.value = Event(PlaylistSheetEvent.CreatePlaylist)
    }

    fun onPlaylistClicked(playlist: Playlist) {
        val sheet = _playlistSheetState.value ?: return
        val track = _screenState.value?.track ?: return
        if (sheet.isAdding) return
        if (track.trackId in playlist.trackIds) {
            _playlistSheetEvents.value = Event(PlaylistSheetEvent.AlreadyAdded(playlist.name))
            return
        }
        _playlistSheetState.value = sheet.copy(isAdding = true)
        viewModelScope.launch {
            try {
                if (playlistsInteractor.addTrack(track, playlist.id)) {
                    onPlaylistSheetHidden()
                    _playlistSheetEvents.value = Event(PlaylistSheetEvent.Added(playlist.name))
                } else {
                    _playlistSheetEvents.value = Event(PlaylistSheetEvent.AlreadyAdded(playlist.name))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _playlistSheetEvents.value = Event(PlaylistSheetEvent.Error)
            } finally {
                _playlistSheetState.value = _playlistSheetState.value?.copy(isAdding = false)
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
