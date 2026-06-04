package com.glazev.playlistmaker

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var albumCover: ImageView
    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var durationValue: TextView
    private lateinit var albumValue: TextView
    private lateinit var yearValue: TextView
    private lateinit var genreValue: TextView
    private lateinit var countryValue: TextView
    private lateinit var playbackTime: TextView
    private lateinit var albumGroup: androidx.constraintlayout.widget.Group
    private lateinit var yearGroup: androidx.constraintlayout.widget.Group

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        backButton = findViewById(R.id.back_button)
        albumCover = findViewById(R.id.album_cover)
        trackName = findViewById(R.id.track_name)
        artistName = findViewById(R.id.artist_name)
        durationValue = findViewById(R.id.duration_value)
        albumValue = findViewById(R.id.album_value)
        yearValue = findViewById(R.id.year_value)
        genreValue = findViewById(R.id.genre_value)
        countryValue = findViewById(R.id.country_value)
        playbackTime = findViewById(R.id.playback_time)
        albumGroup = findViewById(R.id.album_group)
        yearGroup = findViewById(R.id.year_group)

        val trackJson = intent.getStringExtra(EXTRA_TRACK)
        val track = Gson().fromJson(trackJson, Track::class.java)

        bind(track)

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun bind(track: Track) {
        trackName.text = track.trackName
        artistName.text = track.artistName
        durationValue.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis ?: 0L)
        
        if (track.collectionName.isNullOrEmpty()) {
            albumGroup.visibility = View.GONE
        } else {
            albumValue.text = track.collectionName
            albumGroup.visibility = View.VISIBLE
        }
        
        if (track.releaseDate.isNullOrEmpty()) {
            yearGroup.visibility = View.GONE
        } else {
            yearValue.text = track.releaseDate.take(4)
            yearGroup.visibility = View.VISIBLE
        }

        genreValue.text = track.primaryGenreName
        countryValue.text = track.country
        playbackTime.text = "0:00" // Initial value as per requirements for now

        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.ic_placeholder_album)
            .transform(com.bumptech.glide.load.resource.bitmap.CenterCrop(), RoundedCorners(dpToPx(8f)))
            .into(albumCover)
    }

    private fun dpToPx(dp: Float): Int {
        return android.util.TypedValue.applyDimension(
            android.util.TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }

    companion object {
        const val EXTRA_TRACK = "extra_track"
    }
}
