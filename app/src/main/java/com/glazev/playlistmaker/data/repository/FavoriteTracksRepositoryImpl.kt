package com.glazev.playlistmaker.data.repository

import com.glazev.playlistmaker.data.db.AppDatabase
import com.glazev.playlistmaker.data.db.TrackDbMapper
import com.glazev.playlistmaker.domain.api.FavoriteTracksRepository
import com.glazev.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteTracksRepositoryImpl(
    private val database: AppDatabase
) : FavoriteTracksRepository {
    override suspend fun add(track: Track) {
        database.trackDao().insertTrack(TrackDbMapper.toEntity(track))
    }

    override suspend fun remove(track: Track) {
        database.trackDao().deleteTrack(TrackDbMapper.toEntity(track))
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return database.trackDao().getFavoriteTracks().map { tracks ->
            tracks.map(TrackDbMapper::toTrack)
        }
    }
}
