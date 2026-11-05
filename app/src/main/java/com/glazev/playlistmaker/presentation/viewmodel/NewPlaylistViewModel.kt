package com.glazev.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glazev.playlistmaker.domain.api.PlaylistsInteractor
import com.glazev.playlistmaker.domain.models.Playlist
import com.glazev.playlistmaker.presentation.models.Event
import com.glazev.playlistmaker.presentation.models.NewPlaylistEvent
import com.glazev.playlistmaker.presentation.models.NewPlaylistScreenState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NewPlaylistViewModel(
    private val playlistsInteractor: PlaylistsInteractor,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val playlistId: Long = savedStateHandle["playlistId"] ?: -1L
    private val isEditing = playlistId != -1L
    private val _screenState = MutableLiveData(
        NewPlaylistScreenState(
            name = savedStateHandle[NAME_KEY] ?: "",
            description = savedStateHandle[DESCRIPTION_KEY] ?: "",
            coverUri = savedStateHandle[COVER_KEY],
            isEditing = isEditing,
            isLoading = isEditing
        )
    )
    val screenState: LiveData<NewPlaylistScreenState> = _screenState

    private val _events = MutableLiveData<Event<NewPlaylistEvent>>()
    val events: LiveData<Event<NewPlaylistEvent>> = _events

    init {
        if (isEditing) viewModelScope.launch {
            try {
                val playlist = playlistsInteractor.getPlaylist(playlistId).first()
                if (playlist == null) {
                    _events.value = Event(NewPlaylistEvent.Close)
                } else {
                    val name = savedStateHandle.get<String>(NAME_KEY) ?: playlist.name
                    val description = savedStateHandle.get<String>(DESCRIPTION_KEY) ?: playlist.description
                    savedStateHandle[NAME_KEY] = name
                    savedStateHandle[DESCRIPTION_KEY] = description
                    _screenState.value = _screenState.value?.copy(
                        name = name, description = description,
                        existingCoverPath = playlist.coverPath, isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _events.value = Event(NewPlaylistEvent.SaveError)
                _events.value = Event(NewPlaylistEvent.Close)
            }
        }
    }

    fun onNameChanged(name: String) {
        val state = _screenState.value ?: return
        if (state.isSaving || state.isLoading) return
        savedStateHandle[NAME_KEY] = name
        _screenState.value = state.copy(name = name)
    }

    fun onDescriptionChanged(description: String) {
        val state = _screenState.value ?: return
        if (state.isSaving || state.isLoading) return
        savedStateHandle[DESCRIPTION_KEY] = description
        _screenState.value = state.copy(description = description)
    }

    fun onCoverSelected(uri: String) {
        val state = _screenState.value ?: return
        if (state.isSaving) return
        savedStateHandle[COVER_KEY] = uri
        _screenState.value = state.copy(coverUri = uri)
    }

    fun onBackRequested() {
        val state = _screenState.value ?: return
        if (state.isSaving) return
        if (!isEditing && state.hasUnsavedData) {
            _screenState.value = state.copy(showExitConfirmation = true)
        } else {
            _events.value = Event(NewPlaylistEvent.Close)
        }
    }

    fun onExitCancelled() {
        _screenState.value = _screenState.value?.copy(showExitConfirmation = false)
    }

    fun onExitConfirmed() {
        onExitCancelled()
        _events.value = Event(NewPlaylistEvent.Close)
    }

    fun onCreateClicked() {
        val state = _screenState.value ?: return
        if (!state.isCreateEnabled) return
        _screenState.value = state.copy(isSaving = true)
        viewModelScope.launch {
            try {
                if (isEditing) {
                    playlistsInteractor.updatePlaylist(playlistId, state.name, state.description, state.coverUri)
                    _events.value = Event(NewPlaylistEvent.Saved)
                } else {
                    playlistsInteractor.createPlaylist(
                        Playlist(name = state.name, description = state.description), state.coverUri
                    )
                    _events.value = Event(NewPlaylistEvent.Created(state.name))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _screenState.value = state.copy(isSaving = false)
                _events.value = Event(NewPlaylistEvent.SaveError)
            }
        }
    }

    companion object {
        private const val NAME_KEY = "playlist_name"
        private const val DESCRIPTION_KEY = "playlist_description"
        private const val COVER_KEY = "playlist_cover_uri"
    }
}
