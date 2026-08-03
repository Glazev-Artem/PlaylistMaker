package com.glazev.playlistmaker.domain.api

import com.glazev.playlistmaker.domain.models.EmailData

interface ExternalNavigator {
    fun shareLink(link: String)
    fun openLink(link: String)
    fun openEmail(emailData: EmailData)
}
