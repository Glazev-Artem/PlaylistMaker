package com.glazev.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glazev.playlistmaker.domain.api.PlaylistsInteractor
import com.glazev.playlistmaker.domain.api.SharingInteractor
import com.glazev.playlistmaker.domain.models.Track
import com.glazev.playlistmaker.presentation.models.Event
import com.glazev.playlistmaker.presentation.models.PlaylistDetailEvent
import com.glazev.playlistmaker.presentation.models.PlaylistDetailState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistDetailViewModel(
    private val playlistsInteractor: PlaylistsInteractor,
    private val sharingInteractor: SharingInteractor,
    private val playlistId: Long
) : ViewModel() {
    private val _screenState = MutableLiveData(PlaylistDetailState())
    val screenState: LiveData<PlaylistDetailState> = _screenState
    private val _events = MutableLiveData<Event<PlaylistDetailEvent>>()
    val events: LiveData<Event<PlaylistDetailEvent>> = _events
    private var canOpenTrack = true

    init {
        viewModelScope.launch {
            playlistsInteractor.getPlaylist(playlistId).flatMapLatest { playlist ->
                if (playlist == null) flowOf(null to emptyList<Track>())
                else playlistsInteractor.getPlaylistTracks(playlist.trackIds).map { playlist to it }
            }.catch { error ->
                if (error is CancellationException) throw error
                update { it.copy(isLoading = false) }
                emitEvent(PlaylistDetailEvent.Error)
            }.collect { (playlist, tracks) ->
                val state = _screenState.value ?: return@collect
                if (playlist == null && !state.isBusy) emitEvent(PlaylistDetailEvent.Close)
                update { it.copy(playlist = playlist, tracks = tracks, isLoading = false) }
            }
        }
    }

    fun onMenuVisibilityChanged(visible: Boolean) {
        update { it.copy(isMenuVisible = visible && it.playlist != null && !it.isBusy) }
    }

    fun onShareClicked() {
        val state = _screenState.value ?: return
        val playlist = state.playlist ?: return
        if (state.isBusy) return
        onMenuVisibilityChanged(false)
        if (state.tracks.isEmpty()) {
            emitEvent(PlaylistDetailEvent.EmptyShare)
        } else {
            try {
                sharingInteractor.sharePlaylist(playlist, state.tracks)
            } catch (_: Exception) {
                emitEvent(PlaylistDetailEvent.Error)
            }
        }
    }

    fun onEditClicked() {
        val state = _screenState.value ?: return
        if (state.isBusy || state.playlist == null) return
        onMenuVisibilityChanged(false)
        emitEvent(PlaylistDetailEvent.Edit(playlistId))
    }

    fun onTrackClicked(track: Track) {
        if (!canOpenTrack || _screenState.value?.isBusy == true) return
        canOpenTrack = false
        emitEvent(PlaylistDetailEvent.OpenTrack(track))
        viewModelScope.launch {
            delay(1000)
            canOpenTrack = true
        }
    }

    fun onTrackLongClicked(track: Track) {
        update { if (it.isBusy) it else it.copy(trackToRemove = track) }
    }

    fun onTrackRemovalCancelled() = update { it.copy(trackToRemove = null) }

    fun onTrackRemovalConfirmed() {
        val state = _screenState.value ?: return
        val track = state.trackToRemove ?: return
        if (state.isBusy) return
        update { it.copy(trackToRemove = null, isBusy = true) }
        viewModelScope.launch {
            try {
                playlistsInteractor.removeTrack(track.trackId, playlistId)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                emitEvent(PlaylistDetailEvent.Error)
            } finally {
                update { it.copy(isBusy = false) }
            }
        }
    }

    fun onDeleteClicked() {
        val state = _screenState.value ?: return
        if (state.isBusy || state.playlist == null) return
        update { it.copy(isMenuVisible = false, showDeleteConfirmation = true) }
    }

    fun onDeleteCancelled() = update { it.copy(showDeleteConfirmation = false) }

    fun onDeleteConfirmed() {
        if (_screenState.value?.isBusy == true) return
        update { it.copy(showDeleteConfirmation = false, isBusy = true) }
        viewModelScope.launch {
            try {
                playlistsInteractor.deletePlaylist(playlistId)
                emitEvent(PlaylistDetailEvent.Close)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                emitEvent(PlaylistDetailEvent.Error)
            } finally {
                update { it.copy(isBusy = false) }
            }
        }
    }

    private fun update(transform: (PlaylistDetailState) -> PlaylistDetailState) {
        _screenState.value = _screenState.value?.let(transform)
    }

    private fun emitEvent(event: PlaylistDetailEvent) {
        _events.value = Event(event)
    }
}
