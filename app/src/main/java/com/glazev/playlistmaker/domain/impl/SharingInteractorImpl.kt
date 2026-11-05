package com.glazev.playlistmaker.domain.impl

import com.glazev.playlistmaker.domain.api.ExternalNavigator
import com.glazev.playlistmaker.domain.api.SharingInteractor
import com.glazev.playlistmaker.domain.models.EmailData
import com.glazev.playlistmaker.domain.models.Playlist
import com.glazev.playlistmaker.domain.models.Track

class SharingInteractorImpl(
    private val externalNavigator: ExternalNavigator,
    private val shareAppLink: String,
    private val termsLink: String,
    private val supportEmailData: EmailData
) : SharingInteractor {
    override fun sharePlaylist(playlist: Playlist, tracks: List<Track>) = externalNavigator.sharePlaylist(playlist, tracks)

    override fun shareApp() {
        externalNavigator.shareLink(shareAppLink)
    }

    override fun openTerms() {
        externalNavigator.openLink(termsLink)
    }

    override fun openSupport() {
        externalNavigator.openEmail(supportEmailData)
    }
}
