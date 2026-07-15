package com.glazev.playlistmaker.presentation.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.glazev.playlistmaker.R
import com.glazev.playlistmaker.creator.Creator
import com.glazev.playlistmaker.domain.models.Track
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
    private lateinit var playButton: ImageView

    private val playerInteractor = Creator.providePlayerInteractor()
    private var playerState = STATE_DEFAULT
    private var mainThreadHandler = Handler(Looper.getMainLooper())
    private val updatePlaybackTimeRunnable = object : Runnable {
        override fun run() {
            if (playerState == STATE_PLAYING) {
                playbackTime.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(playerInteractor.getCurrentPosition())
                mainThreadHandler.postDelayed(this, REFRESH_PLAYBACK_TIME_DELAY_MS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_audio_player)

        val playerRoot = findViewById<View>(R.id.player_root)
        val paddingLeft = playerRoot.paddingLeft
        val paddingTop = playerRoot.paddingTop
        val paddingRight = playerRoot.paddingRight
        val paddingBottom = playerRoot.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(playerRoot) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = paddingLeft + systemBars.left,
                top = paddingTop + systemBars.top,
                right = paddingRight + systemBars.right,
                bottom = paddingBottom + systemBars.bottom
            )
            insets
        }

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
        playButton = findViewById(R.id.play_button)
        playButton.isEnabled = false

        val trackJson = intent.getStringExtra(EXTRA_TRACK)
        val track = Creator.provideGson().fromJson(trackJson, Track::class.java)

        bind(track)
        preparePlayer(track.previewUrl)

        backButton.setOnClickListener {
            finish()
        }

        playButton.setOnClickListener {
            playbackControl()
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerInteractor.releasePlayer()
        mainThreadHandler.removeCallbacks(updatePlaybackTimeRunnable)
    }

    private fun preparePlayer(previewUrl: String?) {
        if (previewUrl == null) return
        playerInteractor.preparePlayer(
            previewUrl,
            onPrepared = {
                playButton.isEnabled = true
                playerState = STATE_PREPARED
            },
            onCompletion = {
                playButton.setImageResource(getDrawableResId(R.attr.playButtonDrawable))
                playerState = STATE_PREPARED
                mainThreadHandler.removeCallbacks(updatePlaybackTimeRunnable)
                playbackTime.text = INITIAL_PLAYBACK_TIME
            }
        )
    }

    private fun startPlayer() {
        playerInteractor.startPlayer()
        playButton.setImageResource(getDrawableResId(R.attr.pauseButtonDrawable))
        playerState = STATE_PLAYING
        mainThreadHandler.post(updatePlaybackTimeRunnable)
    }

    private fun pausePlayer() {
        if (playerState == STATE_PLAYING) {
            playerInteractor.pausePlayer()
            playButton.setImageResource(getDrawableResId(R.attr.playButtonDrawable))
            playerState = STATE_PAUSED
            mainThreadHandler.removeCallbacks(updatePlaybackTimeRunnable)
        }
    }

    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> {
                pausePlayer()
            }
            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer()
            }
        }
    }

    private fun getDrawableResId(attrId: Int): Int {
        val typedValue = android.util.TypedValue()
        theme.resolveAttribute(attrId, typedValue, true)
        return typedValue.resourceId
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
        playbackTime.text = INITIAL_PLAYBACK_TIME

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
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val REFRESH_PLAYBACK_TIME_DELAY_MS = 300L
        private const val INITIAL_PLAYBACK_TIME = "00:00"
    }
}
