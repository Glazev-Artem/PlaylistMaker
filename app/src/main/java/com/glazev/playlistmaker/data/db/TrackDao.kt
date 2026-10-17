package com.glazev.playlistmaker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Delete
    suspend fun deleteTrack(track: TrackEntity)

    @Query("SELECT * FROM favorite_tracks ORDER BY addedAt DESC, trackId DESC")
    fun getFavoriteTracks(): Flow<List<TrackEntity>>

    @Query("SELECT trackId FROM favorite_tracks")
    suspend fun getFavoriteTrackIds(): List<Long>
}
