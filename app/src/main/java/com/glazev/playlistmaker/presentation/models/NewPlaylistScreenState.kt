package com.glazev.playlistmaker.presentation.models

data class NewPlaylistScreenState(
    val name: String = "",
    val description: String = "",
    val coverUri: String? = null,
    val isSaving: Boolean = false,
    val showExitConfirmation: Boolean = false
) {
    val isCreateEnabled: Boolean get() = name.isNotBlank() && !isSaving
    val hasUnsavedData: Boolean
        get() = name.isNotEmpty() || description.isNotEmpty() || coverUri != null
}

sealed interface NewPlaylistEvent {
    data object Close : NewPlaylistEvent
    data class Created(val name: String) : NewPlaylistEvent
    data object SaveError : NewPlaylistEvent
}
