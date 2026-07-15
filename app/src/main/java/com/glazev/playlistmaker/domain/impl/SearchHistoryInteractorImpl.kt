package com.glazev.playlistmaker.domain.impl

import com.glazev.playlistmaker.domain.api.SearchHistoryInteractor
import com.glazev.playlistmaker.domain.api.SearchHistoryRepository
import com.glazev.playlistmaker.domain.models.Track

class SearchHistoryInteractorImpl(private val repository: SearchHistoryRepository) : SearchHistoryInteractor {
    override fun add(track: Track) {
        repository.add(track)
    }

    override fun get(): List<Track> {
        return repository.get()
    }

    override fun clear() {
        repository.clear()
    }
}
