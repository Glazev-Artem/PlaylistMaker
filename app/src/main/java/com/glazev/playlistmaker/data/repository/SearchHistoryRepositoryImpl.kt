package com.glazev.playlistmaker.data.repository

import android.content.SharedPreferences
import com.glazev.playlistmaker.data.dto.TrackDto
import com.glazev.playlistmaker.domain.api.SearchHistoryRepository
import com.glazev.playlistmaker.domain.models.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryRepositoryImpl(
    private val sharedPrefs: SharedPreferences,
    private val gson: Gson
) : SearchHistoryRepository {

    private val SEARCH_HISTORY_KEY = "search_history_key"

    override fun add(track: Track) {
        val history = get().toMutableList()
        history.removeIf { it.trackId == track.trackId }
        history.add(0, track)
        if (history.size > 10) {
            history.removeAt(10)
        }
        save(history)
    }

    override fun get(): List<Track> {
        val json = sharedPrefs.getString(SEARCH_HISTORY_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<TrackDto>>() {}.type
        val dtos: List<TrackDto> = gson.fromJson(json, type)
        return dtos.map { 
            Track(
                it.trackId, it.trackName, it.artistName, it.trackTimeMillis,
                it.artworkUrl100, it.collectionName, it.releaseDate,
                it.primaryGenreName, it.country, it.previewUrl
            )
        }
    }

    override fun clear() {
        sharedPrefs.edit()
            .remove(SEARCH_HISTORY_KEY)
            .apply()
    }

    private fun save(history: List<Track>) {
        val dtos = history.map {
            TrackDto(
                it.trackId, it.trackName, it.artistName, it.trackTimeMillis,
                it.artworkUrl100, it.collectionName, it.releaseDate,
                it.primaryGenreName, it.country, it.previewUrl
            )
        }
        val json = gson.toJson(dtos)
        sharedPrefs.edit()
            .putString(SEARCH_HISTORY_KEY, json)
            .apply()
    }
}
