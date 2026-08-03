package com.glazev.playlistmaker.presentation.models

import com.glazev.playlistmaker.domain.models.Track

data class SearchScreenState(
    val query: String = "",
    val content: SearchScreenContent = SearchScreenContent.Idle
) {
    val isClearButtonVisible: Boolean
        get() = query.isNotEmpty()
}

sealed interface SearchScreenContent {
    data object Idle : SearchScreenContent
    data object Loading : SearchScreenContent
    data class Results(val tracks: List<Track>) : SearchScreenContent
    data object NothingFound : SearchScreenContent
    data object Error : SearchScreenContent
    data class History(val tracks: List<Track>) : SearchScreenContent
}
