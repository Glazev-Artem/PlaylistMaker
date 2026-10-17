package com.glazev.playlistmaker.data.db

import com.glazev.playlistmaker.domain.models.Track
import java.util.Locale

object TrackDbMapper {
    fun toEntity(track: Track): TrackEntity {
        val seconds = (track.trackTimeMillis ?: 0L) / 1000
        return TrackEntity(
            trackId = track.trackId,
            trackName = track.trackName,
            artistName = track.artistName,
            trackTime = String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60),
            artworkUrl100 = track.artworkUrl100,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            previewUrl = track.previewUrl,
            addedAt = System.currentTimeMillis()
        )
    }

    fun toTrack(entity: TrackEntity): Track {
        val duration = entity.trackTime.split(":")
        return Track(
            trackId = entity.trackId,
            trackName = entity.trackName,
            artistName = entity.artistName,
            trackTimeMillis = (duration[0].toLong() * 60 + duration[1].toLong()) * 1000,
            artworkUrl100 = entity.artworkUrl100,
            collectionName = entity.collectionName,
            releaseDate = entity.releaseDate,
            primaryGenreName = entity.primaryGenreName,
            country = entity.country,
            previewUrl = entity.previewUrl,
            isFavorite = true
        )
    }
}
