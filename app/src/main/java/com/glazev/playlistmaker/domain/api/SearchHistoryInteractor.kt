package com.glazev.playlistmaker.domain.api

import com.glazev.playlistmaker.domain.models.Track

interface SearchHistoryInteractor {
    fun add(track: Track)
    suspend fun get(): List<Track>
    fun clear()
}
