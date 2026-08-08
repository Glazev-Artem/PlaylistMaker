package com.glazev.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.glazev.playlistmaker.presentation.models.Event
import com.glazev.playlistmaker.presentation.models.MainDestination

class MainViewModel : ViewModel() {

    private val _navigationEvent = MutableLiveData<Event<MainDestination>>()
    val navigationEvent: LiveData<Event<MainDestination>> = _navigationEvent

    fun onSearchClicked() {
        navigateTo(MainDestination.SEARCH)
    }

    fun onLibraryClicked() {
        navigateTo(MainDestination.LIBRARY)
    }

    fun onSettingsClicked() {
        navigateTo(MainDestination.SETTINGS)
    }

    private fun navigateTo(destination: MainDestination) {
        _navigationEvent.value = Event(destination)
    }
}
