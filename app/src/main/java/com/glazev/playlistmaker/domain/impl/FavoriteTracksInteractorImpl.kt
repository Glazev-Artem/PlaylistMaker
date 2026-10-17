package com.glazev.playlistmaker.domain.impl

import com.glazev.playlistmaker.domain.api.FavoriteTracksInteractor
import com.glazev.playlistmaker.domain.api.FavoriteTracksRepository
import com.glazev.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

class FavoriteTracksInteractorImpl(
    private val repository: FavoriteTracksRepository
) : FavoriteTracksInteractor {
    override suspend fun add(track: Track) = repository.add(track)

    override suspend fun remove(track: Track) = repository.remove(track)

    override fun getFavoriteTracks(): Flow<List<Track>> = repository.getFavoriteTracks()
}
