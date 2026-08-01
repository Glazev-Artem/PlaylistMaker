package com.glazev.playlistmaker.presentation.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.glazev.playlistmaker.domain.api.SearchHistoryInteractor
import com.glazev.playlistmaker.domain.api.TracksInteractor
import com.glazev.playlistmaker.domain.models.Track
import com.glazev.playlistmaker.presentation.models.Event
import com.glazev.playlistmaker.presentation.models.SearchScreenContent
import com.glazev.playlistmaker.presentation.models.SearchScreenState

class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor
) : ViewModel() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentQuery = ""
    private var hasFocus = false
    private var lastTrackClickTime = 0L
    private var latestRequestId = 0
    private val searchRunnable = Runnable { search(currentQuery) }

    private val _screenState = MutableLiveData(SearchScreenState())
    val screenState: LiveData<SearchScreenState> = _screenState

    private val _openPlayerEvent = MutableLiveData<Event<Track>>()
    val openPlayerEvent: LiveData<Event<Track>> = _openPlayerEvent

    fun onQueryChanged(query: String) {
        if (query == currentQuery) return

        currentQuery = query
        latestRequestId++
        mainHandler.removeCallbacks(searchRunnable)

        if (query.isEmpty()) {
            showHistoryOrIdle()
        } else {
            _screenState.value = SearchScreenState(query, SearchScreenContent.Idle)
            mainHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
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
        mainHandler.removeCallbacks(searchRunnable)
        search(query)
    }

    fun onHistoryClearClicked() {
        searchHistoryInteractor.clear()
        showHistoryOrIdle()
    }

    fun onTrackClicked(track: Track) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastTrackClickTime < CLICK_DEBOUNCE_DELAY) return
        lastTrackClickTime = clickTime

        searchHistoryInteractor.add(track)
        if (_screenState.value?.content is SearchScreenContent.History) {
            showHistoryOrIdle()
        }
        _openPlayerEvent.value = Event(track)
    }

    private fun search(query: String) {
        if (query.isBlank()) {
            showHistoryOrIdle()
            return
        }

        _screenState.value = SearchScreenState(query, SearchScreenContent.Loading)
        val requestId = ++latestRequestId
        tracksInteractor.searchTracks(query, object : TracksInteractor.TracksConsumer {
            override fun consume(foundTracks: List<Track>?, errorMessage: String?) {
                mainHandler.post {
                    if (
                        requestId != latestRequestId ||
                        query != currentQuery ||
                        currentQuery.isEmpty()
                    ) {
                        return@post
                    }

                    val content = when {
                        foundTracks == null -> SearchScreenContent.Error
                        foundTracks.isEmpty() -> SearchScreenContent.NothingFound
                        else -> SearchScreenContent.Results(foundTracks)
                    }
                    _screenState.value = SearchScreenState(currentQuery, content)
                }
            }
        })
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

    override fun onCleared() {
        mainHandler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}
