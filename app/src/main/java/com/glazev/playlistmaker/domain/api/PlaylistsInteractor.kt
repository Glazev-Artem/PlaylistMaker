package com.glazev.playlistmaker.domain.api

import com.glazev.playlistmaker.domain.models.Playlist
import com.glazev.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsInteractor {
    suspend fun createPlaylist(playlist: Playlist, coverUri: String?): Long
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun addTrack(track: Track, playlistId: Long): Boolean
    fun getPlaylist(id: Long): Flow<Playlist?>
    fun getPlaylistTracks(trackIds: List<Long>): Flow<List<Track>>
    suspend fun removeTrack(trackId: Long, playlistId: Long)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun updatePlaylist(playlistId: Long, name: String, description: String, coverUri: String?)
}
