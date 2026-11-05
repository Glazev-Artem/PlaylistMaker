package com.glazev.playlistmaker.data.sharing

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.glazev.playlistmaker.R
import com.glazev.playlistmaker.domain.api.ExternalNavigator
import com.glazev.playlistmaker.domain.models.EmailData
import com.glazev.playlistmaker.domain.models.Playlist
import com.glazev.playlistmaker.domain.models.Track
import java.util.Locale

class ExternalNavigatorImpl(private val context: Context) : ExternalNavigator {

    override fun sharePlaylist(playlist: Playlist, tracks: List<Track>) {
        val lines = mutableListOf(playlist.name)
        if (playlist.description.isNotEmpty()) lines += playlist.description
        lines += context.resources.getQuantityString(R.plurals.playlist_track_count, tracks.size, tracks.size)
        tracks.forEachIndexed { index, track ->
            val seconds = (track.trackTimeMillis ?: 0L) / 1000
            val duration = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60)
            lines += context.getString(R.string.playlist_share_track, index + 1,
                track.artistName.orEmpty(), track.trackName.orEmpty(), duration)
        }
        shareLink(lines.joinToString("\n"))
    }

    override fun shareLink(link: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, link)
        }
        val chooserIntent = Intent.createChooser(shareIntent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooserIntent)
    }

    override fun openLink(link: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override fun openEmail(emailData: EmailData) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailData.email))
            putExtra(Intent.EXTRA_SUBJECT, emailData.subject)
            putExtra(Intent.EXTRA_TEXT, emailData.body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
