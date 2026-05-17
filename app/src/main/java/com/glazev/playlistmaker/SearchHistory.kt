package com.glazev.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val sharedPrefs: SharedPreferences) {

    private val gson = Gson()

    fun add(track: Track) {
        val history = get().toMutableList()
        history.removeIf { it.trackId == track.trackId }
        history.add(0, track)
        if (history.size > 10) {
            history.removeAt(10)
        }
        save(history)
    }

    fun get(): List<Track> {
        val json = sharedPrefs.getString(SEARCH_HISTORY_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clear() {
        sharedPrefs.edit()
            .remove(SEARCH_HISTORY_KEY)
            .apply()
    }

    private fun save(history: List<Track>) {
        val json = gson.toJson(history)
        sharedPrefs.edit()
            .putString(SEARCH_HISTORY_KEY, json)
            .apply()
    }

    companion object {
        const val SEARCH_HISTORY_KEY = "search_history_key"
    }
}
