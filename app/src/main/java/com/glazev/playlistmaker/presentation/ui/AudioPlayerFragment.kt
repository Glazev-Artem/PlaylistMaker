package com.glazev.playlistmaker.presentation.ui

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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

class AudioPlayerFragment : Fragment(R.layout.fragment_audio_player) {

    private val args: AudioPlayerFragmentArgs by navArgs()
    private val track: Track by lazy(LazyThreadSafetyMode.NONE) { args.track }

    private val viewModel: AudioPlayerViewModel by viewModel {
        parametersOf(track)
    }

    private var isViewModelInitialized = false
    private var boundTrackId: Long? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playerViewModel = viewModel
        isViewModelInitialized = true

        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<ImageView>(R.id.play_button).setOnClickListener {
            playerViewModel.onPlaybackControlClicked()
        }

        playerViewModel.screenState.observe(viewLifecycleOwner) { state ->
            render(view, state)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isViewModelInitialized) {
            viewModel.onScreenPaused()
        }
    }

    private fun render(rootView: View, state: PlayerScreenState) {
        if (boundTrackId != state.track.trackId) {
            bindTrack(rootView, state.track)
            boundTrackId = state.track.trackId
        }

        rootView.findViewById<ImageView>(R.id.play_button).apply {
            isEnabled = state.isPlayButtonEnabled
            setImageResource(
                getDrawableResId(
                    if (state.isPlaying) {
                        R.attr.pauseButtonDrawable
                    } else {
                        R.attr.playButtonDrawable
                    }
                )
            )
        }
        rootView.findViewById<TextView>(R.id.playback_time).text = state.playbackTime
    }

    private fun bindTrack(rootView: View, track: Track) {
        rootView.findViewById<TextView>(R.id.track_name).text = track.trackName.orEmpty()
        rootView.findViewById<TextView>(R.id.artist_name).text = track.artistName.orEmpty()
        rootView.findViewById<TextView>(R.id.duration_value).text =
            SimpleDateFormat("mm:ss", Locale.getDefault())
                .format(track.trackTimeMillis ?: 0L)

        val albumGroup = rootView.findViewById<Group>(R.id.album_group)
        if (track.collectionName.isNullOrEmpty()) {
            albumGroup.visibility = View.GONE
        } else {
            rootView.findViewById<TextView>(R.id.album_value).text = track.collectionName
            albumGroup.visibility = View.VISIBLE
        }

        val yearGroup = rootView.findViewById<Group>(R.id.year_group)
        if (track.releaseDate.isNullOrEmpty()) {
            yearGroup.visibility = View.GONE
        } else {
            rootView.findViewById<TextView>(R.id.year_value).text =
                track.releaseDate.take(YEAR_LENGTH)
            yearGroup.visibility = View.VISIBLE
        }

        rootView.findViewById<TextView>(R.id.genre_value).text =
            track.primaryGenreName.orEmpty()
        rootView.findViewById<TextView>(R.id.country_value).text = track.country.orEmpty()

        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.ic_placeholder_album)
            .transform(CenterCrop(), RoundedCorners(dpToPx(ALBUM_COVER_CORNER_RADIUS_DP)))
            .into(rootView.findViewById(R.id.album_cover))
    }

    private fun getDrawableResId(attrId: Int): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(attrId, typedValue, true)
        return typedValue.resourceId
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }

    companion object {
        private const val YEAR_LENGTH = 4
        private const val ALBUM_COVER_CORNER_RADIUS_DP = 8f
    }
}
