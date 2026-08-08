package com.glazev.playlistmaker.presentation.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.glazev.playlistmaker.R
import com.glazev.playlistmaker.domain.models.Track
import com.glazev.playlistmaker.presentation.models.PlayerScreenState
import com.glazev.playlistmaker.presentation.viewmodel.AudioPlayerViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var trackForViewModel: Track
    private val viewModel: AudioPlayerViewModel by viewModel {
        parametersOf(trackForViewModel)
    }
    private var isViewModelInitialized = false

    private lateinit var albumCover: ImageView
    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var durationValue: TextView
    private lateinit var albumValue: TextView
    private lateinit var yearValue: TextView
    private lateinit var genreValue: TextView
    private lateinit var countryValue: TextView
    private lateinit var playbackTime: TextView
    private lateinit var albumGroup: Group
    private lateinit var yearGroup: Group
    private lateinit var playButton: ImageView
    private var boundTrackId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val track = getTrackFromIntent()
        if (track == null) {
            finish()
            return
        }
        trackForViewModel = track

        enableEdgeToEdge()
        setContentView(R.layout.activity_audio_player)

        applyWindowInsets()
        bindViews()

        val playerViewModel = viewModel
        isViewModelInitialized = true

        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }
        playButton.setOnClickListener {
            playerViewModel.onPlaybackControlClicked()
        }

        playerViewModel.screenState.observe(this) { state ->
            render(state)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isViewModelInitialized) {
            viewModel.onScreenPaused()
        }
    }

    private fun applyWindowInsets() {
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
    }

    private fun bindViews() {
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
    }

    private fun render(state: PlayerScreenState) {
        if (boundTrackId != state.track.trackId) {
            bindTrack(state.track)
            boundTrackId = state.track.trackId
        }

        playButton.isEnabled = state.isPlayButtonEnabled
        playButton.setImageResource(
            getDrawableResId(
                if (state.isPlaying) R.attr.pauseButtonDrawable else R.attr.playButtonDrawable
            )
        )
        playbackTime.text = state.playbackTime
    }

    private fun bindTrack(track: Track) {
        trackName.text = track.trackName.orEmpty()
        artistName.text = track.artistName.orEmpty()
        durationValue.text = SimpleDateFormat("mm:ss", Locale.getDefault())
            .format(track.trackTimeMillis ?: 0L)

        if (track.collectionName.isNullOrEmpty()) {
            albumGroup.visibility = View.GONE
        } else {
            albumValue.text = track.collectionName
            albumGroup.visibility = View.VISIBLE
        }

        if (track.releaseDate.isNullOrEmpty()) {
            yearGroup.visibility = View.GONE
        } else {
            yearValue.text = track.releaseDate.take(YEAR_LENGTH)
            yearGroup.visibility = View.VISIBLE
        }

        genreValue.text = track.primaryGenreName.orEmpty()
        countryValue.text = track.country.orEmpty()

        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.ic_placeholder_album)
            .transform(CenterCrop(), RoundedCorners(dpToPx(ALBUM_COVER_CORNER_RADIUS_DP)))
            .into(albumCover)
    }

    private fun getTrackFromIntent(): Track? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_TRACK, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_TRACK) as? Track
        }
    }

    private fun getDrawableResId(attrId: Int): Int {
        val typedValue = android.util.TypedValue()
        theme.resolveAttribute(attrId, typedValue, true)
        return typedValue.resourceId
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
        private const val YEAR_LENGTH = 4
        private const val ALBUM_COVER_CORNER_RADIUS_DP = 8f
    }
}
