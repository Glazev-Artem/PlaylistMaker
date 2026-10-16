package com.glazev.playlistmaker.domain.api

import com.glazev.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksInteractor {
    suspend fun add(track: Track)
    suspend fun remove(track: Track)
    fun getFavoriteTracks(): Flow<List<Track>>
}
