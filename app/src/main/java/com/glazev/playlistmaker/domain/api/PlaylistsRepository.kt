package com.glazev.playlistmaker.domain.api

import com.glazev.playlistmaker.domain.models.Playlist
import com.glazev.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsRepository {
    suspend fun createPlaylist(playlist: Playlist, coverUri: String?): Long
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun addTrack(track: Track, playlistId: Long): Boolean
}
