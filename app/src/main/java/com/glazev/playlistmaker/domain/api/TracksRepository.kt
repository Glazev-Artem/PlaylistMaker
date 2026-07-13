package com.glazev.playlistmaker.domain.api

import com.glazev.playlistmaker.domain.models.Track

interface TracksRepository {
    fun searchTracks(expression: String): List<Track>
}
