package com.glazev.playlistmaker.data.repository

import android.content.Context
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.room.withTransaction
import com.glazev.playlistmaker.data.db.AppDatabase
import com.glazev.playlistmaker.data.db.PlaylistEntity
import com.glazev.playlistmaker.data.db.PlaylistTrackEntity
import com.glazev.playlistmaker.data.db.TrackDbMapper
import com.glazev.playlistmaker.domain.api.PlaylistsRepository
import com.glazev.playlistmaker.domain.models.Playlist
import com.glazev.playlistmaker.domain.models.Track
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

class PlaylistsRepositoryImpl(
    private val database: AppDatabase,
    private val context: Context,
    private val gson: Gson
) : PlaylistsRepository {

    override suspend fun createPlaylist(playlist: Playlist, coverUri: String?): Long =
        withContext(Dispatchers.IO) {
            val cover = coverUri?.let(::copyCover)
            try {
                database.playlistDao().insertPlaylist(
                    PlaylistEntity(
                        name = playlist.name,
                        description = playlist.description,
                        coverPath = cover?.absolutePath,
                        trackIds = gson.toJson(emptyList<Long>()),
                        trackCount = 0
                    )
                )
            } catch (e: Exception) {
                cover?.delete()
                throw e
            }
        }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return database.playlistDao().getPlaylists().map { entities ->
            entities.map { entity ->
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    description = entity.description,
                    coverPath = entity.coverPath,
                    trackIds = readTrackIds(entity.trackIds),
                    trackCount = entity.trackCount
                )
            }
        }
    }

    override suspend fun addTrack(track: Track, playlistId: Long): Boolean {
        return database.withTransaction {
            val playlist = checkNotNull(database.playlistDao().getPlaylistById(playlistId))
            val trackIds = readTrackIds(playlist.trackIds)
            if (track.trackId in trackIds) return@withTransaction false

            val entity = TrackDbMapper.toEntity(track)
            database.playlistTrackDao().insertTrack(
                PlaylistTrackEntity(
                    entity.trackId, entity.trackName, entity.artistName, entity.trackTime,
                    entity.artworkUrl100, entity.collectionName, entity.releaseDate,
                    entity.primaryGenreName, entity.country, entity.previewUrl, entity.addedAt
                )
            )
            val updatedIds = trackIds + track.trackId
            database.playlistDao().updatePlaylist(
                playlist.copy(trackIds = gson.toJson(updatedIds), trackCount = updatedIds.size)
            )
            true
        }
    }

    private fun readTrackIds(json: String): List<Long> =
        gson.fromJson(json, Array<Long>::class.java).toList()

    private fun copyCover(source: String): File {
        val uri = source.toUri()
        val directory = File(context.filesDir, "playlist_covers")
        if (!directory.isDirectory && !directory.mkdirs()) {
            throw IOException("Unable to create playlist covers directory")
        }
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(context.contentResolver.getType(uri)) ?: "img"
        val target = File(directory, "${UUID.randomUUID()}.$extension")
        try {
            val input = context.contentResolver.openInputStream(uri)
                ?: throw IOException("Unable to open playlist cover")
            input.use { sourceStream ->
                target.outputStream().use { output -> sourceStream.copyTo(output) }
            }
            return target
        } catch (e: Exception) {
            target.delete()
            throw e
        }
    }
}
