package com.glazev.playlistmaker.domain.api

import com.glazev.playlistmaker.domain.models.EmailData
import com.glazev.playlistmaker.domain.models.Playlist
import com.glazev.playlistmaker.domain.models.Track

interface ExternalNavigator {
    fun shareLink(link: String)
    fun openLink(link: String)
    fun openEmail(emailData: EmailData)
    fun sharePlaylist(playlist: Playlist, tracks: List<Track>)
}
