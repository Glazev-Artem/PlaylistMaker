package com.glazev.playlistmaker.data.repository

import com.glazev.playlistmaker.data.NetworkClient
import com.glazev.playlistmaker.data.dto.TracksSearchRequest
import com.glazev.playlistmaker.data.dto.TracksSearchResponse
import com.glazev.playlistmaker.domain.api.Resource
import com.glazev.playlistmaker.domain.api.TracksRepository
import com.glazev.playlistmaker.domain.models.Track

class TracksRepositoryImpl(private val networkClient: NetworkClient) : TracksRepository {

    override fun searchTracks(expression: String): Resource<List<Track>> {
        val response = networkClient.doRequest(TracksSearchRequest(expression))
        return when (response.resultCode) {
            -1 -> Resource.Error("Проверьте подключение к интернету")
            200 -> {
                // Безопасное приведение типа, чтобы не было вылета
                val searchResponse = response as? TracksSearchResponse
                if (searchResponse != null) {
                    Resource.Success(searchResponse.results.map {
                        Track(
                            it.trackId, it.trackName, it.artistName, it.trackTimeMillis,
                            it.artworkUrl100, it.collectionName, it.releaseDate,
                            it.primaryGenreName, it.country, it.previewUrl
                        )
                    })
                } else {
                    Resource.Error("Ошибка обработки данных")
                }
            }
            else -> Resource.Error("Ошибка сервера")
        }
    }
}
