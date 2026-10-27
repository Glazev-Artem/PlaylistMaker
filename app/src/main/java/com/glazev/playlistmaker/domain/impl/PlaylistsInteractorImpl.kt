package com.glazev.playlistmaker.domain.impl

import com.glazev.playlistmaker.domain.api.PlaylistsInteractor
import com.glazev.playlistmaker.domain.api.PlaylistsRepository
import com.glazev.playlistmaker.domain.models.Playlist
import com.glazev.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

class PlaylistsInteractorImpl(private val repository: PlaylistsRepository) : PlaylistsInteractor {
    override suspend fun createPlaylist(playlist: Playlist, coverUri: String?): Long =
        repository.createPlaylist(playlist, coverUri)

    override fun getPlaylists(): Flow<List<Playlist>> = repository.getPlaylists()

    override suspend fun addTrack(track: Track, playlistId: Long): Boolean =
        repository.addTrack(track, playlistId)
}
