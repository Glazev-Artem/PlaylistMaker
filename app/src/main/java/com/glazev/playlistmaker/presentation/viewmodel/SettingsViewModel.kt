package com.glazev.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.glazev.playlistmaker.domain.api.SettingsInteractor
import com.glazev.playlistmaker.domain.api.SharingInteractor
import com.glazev.playlistmaker.domain.models.ThemeSettings
import com.glazev.playlistmaker.presentation.models.SettingsScreenState

class SettingsViewModel(
    private val settingsInteractor: SettingsInteractor,
    private val sharingInteractor: SharingInteractor
) : ViewModel() {

    private val _screenState = MutableLiveData(
        SettingsScreenState(settingsInteractor.getThemeSettings().darkTheme)
    )
    val screenState: LiveData<SettingsScreenState> = _screenState

    fun onThemeChanged(isDarkThemeEnabled: Boolean) {
        if (_screenState.value?.isDarkThemeEnabled == isDarkThemeEnabled) return

        settingsInteractor.updateThemeSettings(ThemeSettings(isDarkThemeEnabled))
        _screenState.value = SettingsScreenState(isDarkThemeEnabled)
    }

    fun onShareClicked() {
        sharingInteractor.shareApp()
    }

    fun onSupportClicked() {
        sharingInteractor.openSupport()
    }

    fun onAgreementClicked() {
        sharingInteractor.openTerms()
    }
}
