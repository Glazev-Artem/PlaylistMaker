package com.glazev.playlistmaker.domain.impl

import com.glazev.playlistmaker.domain.api.Resource
import com.glazev.playlistmaker.domain.api.TracksInteractor
import com.glazev.playlistmaker.domain.api.TracksRepository
import com.glazev.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TracksInteractorImpl(
    private val repository: TracksRepository
) : TracksInteractor {

    override fun searchTracks(expression: String): Flow<Pair<List<Track>?, String?>> {
        return repository.searchTracks(expression).map { resource ->
            when (resource) {
                is Resource.Success -> Pair(resource.data, null)
                is Resource.Error -> Pair(null, resource.message)
            }
        }
    }
}
