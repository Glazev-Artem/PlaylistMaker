package com.glazev.playlistmaker.data.repository

import com.glazev.playlistmaker.data.NetworkClient
import com.glazev.playlistmaker.data.dto.TracksSearchRequest
import com.glazev.playlistmaker.data.dto.TracksSearchResponse
import com.glazev.playlistmaker.domain.api.TracksRepository
import com.glazev.playlistmaker.domain.models.Track

class TracksRepositoryImpl(private val networkClient: NetworkClient) : TracksRepository {

    override fun searchTracks(expression: String): List<Track> {
        val response = networkClient.doRequest(TracksSearchRequest(expression))
        if (response.resultCode == 200) {
            return (response as TracksSearchResponse).results.map {
                Track(
                    it.trackId,
                    it.trackName,
                    it.artistName,
                    it.trackTimeMillis,
                    it.artworkUrl100,
                    it.collectionName,
                    it.releaseDate,
                    it.primaryGenreName,
                    it.country,
                    it.previewUrl
                )
            }
        } else {
            return emptyList()
        }
    }
}
