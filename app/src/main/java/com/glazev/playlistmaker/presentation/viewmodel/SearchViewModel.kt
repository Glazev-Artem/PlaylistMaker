package com.glazev.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glazev.playlistmaker.domain.api.SearchHistoryInteractor
import com.glazev.playlistmaker.domain.api.TracksInteractor
import com.glazev.playlistmaker.domain.models.Track
import com.glazev.playlistmaker.presentation.models.Event
import com.glazev.playlistmaker.presentation.models.SearchScreenContent
import com.glazev.playlistmaker.presentation.models.SearchScreenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor
) : ViewModel() {

    private var debounceJob: Job? = null
    private var searchJob: Job? = null
    private var currentQuery = ""
    private var hasFocus = false
    private var isClickAllowed = true
    private var latestRequestId = 0

    private val _screenState = MutableLiveData(SearchScreenState())
    val screenState: LiveData<SearchScreenState> = _screenState

    private val _openPlayerEvent = MutableLiveData<Event<Track>>()
    val openPlayerEvent: LiveData<Event<Track>> = _openPlayerEvent

    fun onQueryChanged(query: String) {
        if (query == currentQuery) return

        currentQuery = query
        latestRequestId++
        debounceJob?.cancel()
        searchJob?.cancel()

        if (query.isEmpty()) {
            showHistoryOrIdle()
        } else {
            _screenState.value = SearchScreenState(query, SearchScreenContent.Idle)
            debounceJob = viewModelScope.launch {
                delay(SEARCH_DEBOUNCE_DELAY)
                search(query)
            }
        }
    }

    fun onFocusChanged(focused: Boolean) {
        hasFocus = focused
        if (currentQuery.isEmpty()) {
            showHistoryOrIdle()
        }
    }

    fun onSearchRequested(query: String) {
        currentQuery = query
        debounceJob?.cancel()
        search(query)
    }

    fun onHistoryClearClicked() {
        searchHistoryInteractor.clear()
        showHistoryOrIdle()
    }

    fun onTrackClicked(track: Track) {
        if (!isClickAllowed) return
        isClickAllowed = false
        viewModelScope.launch {
            delay(CLICK_DEBOUNCE_DELAY)
            isClickAllowed = true
        }

        searchHistoryInteractor.add(track)
        if (_screenState.value?.content is SearchScreenContent.History) {
            showHistoryOrIdle()
        }
        _openPlayerEvent.value = Event(track)
    }

    private fun search(query: String) {
        searchJob?.cancel()
        val requestId = ++latestRequestId
        if (query.isBlank()) {
            showHistoryOrIdle()
            return
        }

        _screenState.value = SearchScreenState(query, SearchScreenContent.Loading)
        searchJob = viewModelScope.launch {
            tracksInteractor.searchTracks(query).collect { (foundTracks, errorMessage) ->
                if (
                    requestId != latestRequestId ||
                    query != currentQuery ||
                    currentQuery.isEmpty()
                ) {
                    return@collect
                }

                val content = when {
                    errorMessage != null || foundTracks == null -> SearchScreenContent.Error
                    foundTracks.isEmpty() -> SearchScreenContent.NothingFound
                    else -> SearchScreenContent.Results(foundTracks)
                }
                _screenState.value = SearchScreenState(currentQuery, content)
            }
        }
    }

    private fun showHistoryOrIdle() {
        val history = searchHistoryInteractor.get()
        val content = if (hasFocus && history.isNotEmpty()) {
            SearchScreenContent.History(history)
        } else {
            SearchScreenContent.Idle
        }
        _screenState.value = SearchScreenState(currentQuery, content)
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}
