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
import kotlinx.coroutines.flow.flowOf
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
            entities.map { it.toPlaylist() }
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
                    entity.primaryGenreName, entity.country, entity.previewUrl, entity.addedAt, track.trackTimeMillis
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

    override fun getPlaylist(id: Long): Flow<Playlist?> =
        database.playlistDao().observePlaylist(id).map { it?.toPlaylist() }

    override fun getPlaylistTracks(trackIds: List<Long>): Flow<List<Track>> {
        if (trackIds.isEmpty()) return flowOf(emptyList())
        return database.playlistTrackDao().getTracks().map { stored ->
            val byId = stored.associateBy { it.trackId }
            trackIds.asReversed().mapNotNull { byId[it]?.toTrack() }
        }
    }

    override suspend fun removeTrack(trackId: Long, playlistId: Long) {
        database.withTransaction {
            val playlist = database.playlistDao().getPlaylistById(playlistId) ?: return@withTransaction
            val ids = readTrackIds(playlist.trackIds).filterNot { it == trackId }
            database.playlistDao().updatePlaylist(playlist.copy(trackIds = gson.toJson(ids), trackCount = ids.size))
            deleteUnusedTracks(listOf(trackId))
        }
    }

    override suspend fun deletePlaylist(playlistId: Long): Unit = withContext(Dispatchers.IO) {
        val coverPath = database.withTransaction {
            val playlist = database.playlistDao().getPlaylistById(playlistId) ?: return@withTransaction null
            database.playlistDao().deletePlaylist(playlistId)
            deleteUnusedTracks(readTrackIds(playlist.trackIds))
            playlist.coverPath
        }
        coverPath?.let(::deleteCover)
    }

    override suspend fun updatePlaylist(playlistId: Long, name: String, description: String, coverUri: String?): Unit =
        withContext(Dispatchers.IO) {
            val cover = coverUri?.let(::copyCover)
            val oldCover = try {
                database.withTransaction {
                    val playlist = checkNotNull(database.playlistDao().getPlaylistById(playlistId))
                    database.playlistDao().updatePlaylist(
                        playlist.copy(name = name, description = description, coverPath = cover?.absolutePath ?: playlist.coverPath)
                    )
                    playlist.coverPath
                }
            } catch (e: Exception) {
                cover?.delete()
                throw e
            }
            if (cover != null) oldCover?.let(::deleteCover)
        }

    private suspend fun deleteUnusedTracks(candidates: List<Long>) {
        val usedIds = database.playlistDao().getAllPlaylists().flatMap { readTrackIds(it.trackIds) }.toSet()
        candidates.filterNot { it in usedIds }.forEach { database.playlistTrackDao().deleteTrack(it) }
    }

    private fun deleteCover(path: String) {
        runCatching {
            val file = File(path)
            val directory = File(context.filesDir, "playlist_covers").canonicalFile
            if (file.canonicalFile.parentFile == directory) file.delete()
        }
    }

    private fun PlaylistEntity.toPlaylist() = Playlist(id, name, description, coverPath, readTrackIds(trackIds), trackCount)

    private fun PlaylistTrackEntity.toTrack(): Track {
        val duration = trackTime.split(":")
        val millis = durationMillis ?: (((duration.getOrNull(0)?.toLongOrNull() ?: 0L) * 60 +
            (duration.getOrNull(1)?.toLongOrNull() ?: 0L)) * 1000)
        return Track(trackId, trackName, artistName, millis, artworkUrl100, collectionName,
            releaseDate, primaryGenreName, country, previewUrl)
    }

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
