package com.glazev.playlistmaker.presentation.models

data class NewPlaylistScreenState(
    val name: String = "",
    val description: String = "",
    val coverUri: String? = null,
    val isSaving: Boolean = false,
    val showExitConfirmation: Boolean = false,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val existingCoverPath: String? = null
) {
    val isCreateEnabled: Boolean get() = name.isNotBlank() && !isSaving && !isLoading
    val hasUnsavedData: Boolean
        get() = name.isNotEmpty() || description.isNotEmpty() || coverUri != null
}

sealed interface NewPlaylistEvent {
    data object Close : NewPlaylistEvent
    data class Created(val name: String) : NewPlaylistEvent
    data object Saved : NewPlaylistEvent
    data object LoadError : NewPlaylistEvent
    data object SaveError : NewPlaylistEvent
}
