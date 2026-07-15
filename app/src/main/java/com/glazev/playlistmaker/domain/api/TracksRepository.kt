package com.glazev.playlistmaker.domain.api

import com.glazev.playlistmaker.domain.models.Track

interface TracksRepository {
    fun searchTracks(expression: String): Resource<List<Track>>
}

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}
