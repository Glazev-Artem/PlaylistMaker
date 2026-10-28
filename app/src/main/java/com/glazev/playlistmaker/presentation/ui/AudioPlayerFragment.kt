package com.glazev.playlistmaker.presentation.ui

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.glazev.playlistmaker.R
import com.glazev.playlistmaker.domain.models.Track
import com.glazev.playlistmaker.presentation.models.PlayerScreenState
import com.glazev.playlistmaker.presentation.models.PlaylistSheetEvent
import com.glazev.playlistmaker.presentation.models.PlaylistSheetState
import com.glazev.playlistmaker.presentation.viewmodel.AudioPlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
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
    private var playButton: ImageView? = null
    private var likeButton: ImageView? = null
    private var playbackTime: TextView? = null
    private var sheetViews: PlaylistSheetViews? = null
    private var sheetBehavior: BottomSheetBehavior<LinearLayout>? = null
    private var sheetCallback: BottomSheetBehavior.BottomSheetCallback? = null
    private var sheetBackCallback: OnBackPressedCallback? = null
    private var playlistAdapter: PlaylistSheetAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playerViewModel = viewModel
        isViewModelInitialized = true
        playButton = view.findViewById(R.id.play_button)
        likeButton = view.findViewById(R.id.like_button)
        playbackTime = view.findViewById(R.id.playback_time)
        setupPlaylistSheet(view, playerViewModel)

        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            if (sheetBackCallback?.isEnabled == true) {
                playerViewModel.onPlaylistSheetHidden()
            } else {
                findNavController().popBackStack()
            }
        }

        playButton?.setOnClickListener {
            playerViewModel.onPlaybackControlClicked()
        }

        likeButton?.setOnClickListener {
            playerViewModel.onFavoriteClicked()
        }

        view.findViewById<ImageView>(R.id.add_button).apply {
            contentDescription = getString(R.string.playlist_add_title)
            setOnClickListener { playerViewModel.onAddToPlaylistClicked() }
        }

        playerViewModel.screenState.observe(viewLifecycleOwner) { state ->
            render(view, state)
        }
        playerViewModel.playlistSheetState.observe(viewLifecycleOwner, ::renderPlaylistSheet)
        playerViewModel.playlistSheetEvents.observe(viewLifecycleOwner) { event ->
            when (val content = event.getContentIfNotHandled()) {
                is PlaylistSheetEvent.Added -> showPlaylistMessage(R.string.playlist_track_added, content.playlistName)
                is PlaylistSheetEvent.AlreadyAdded -> showPlaylistMessage(R.string.playlist_track_already_added, content.playlistName)
                PlaylistSheetEvent.CreatePlaylist -> findNavController().navigate(
                    AudioPlayerFragmentDirections.actionAudioPlayerFragmentToNewPlaylistFragment()
                )
                PlaylistSheetEvent.Error -> Toast.makeText(context, R.string.playlist_add_error, Toast.LENGTH_SHORT).show()
                null -> Unit
            }
        }
    }

    private fun setupPlaylistSheet(view: View, playerViewModel: AudioPlayerViewModel) {
        val boundViews = PlaylistSheetViews(
            container = view.findViewById(R.id.playlists_bottom_sheet),
            overlay = view.findViewById(R.id.playlist_sheet_overlay),
            list = view.findViewById(R.id.player_playlists_list),
            newPlaylist = view.findViewById(R.id.player_new_playlist_button)
        )
        sheetViews = boundViews
        playlistAdapter = PlaylistSheetAdapter(playerViewModel::onPlaylistClicked)
        boundViews.list.adapter = playlistAdapter
        val behavior = BottomSheetBehavior.from(boundViews.container)
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        sheetBehavior = behavior
        sheetBackCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() = playerViewModel.onPlaylistSheetHidden()
        }.also { requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, it) }
        sheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val isVisible = newState != BottomSheetBehavior.STATE_HIDDEN
                boundViews.overlay.isVisible = isVisible
                sheetBackCallback?.isEnabled = isVisible
                if (!isVisible) playerViewModel.onPlaylistSheetHidden()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        }.also(behavior::addBottomSheetCallback)
        boundViews.overlay.setOnClickListener { playerViewModel.onPlaylistSheetHidden() }
        boundViews.newPlaylist.setOnClickListener { playerViewModel.onNewPlaylistClicked() }
    }

    private fun renderPlaylistSheet(state: PlaylistSheetState) {
        val boundViews = sheetViews ?: return
        val behavior = sheetBehavior ?: return
        playlistAdapter?.submitList(state.playlists)
        boundViews.newPlaylist.isEnabled = !state.isAdding
        if (state.isVisible) {
            boundViews.overlay.isVisible = true
            sheetBackCallback?.isEnabled = true
            if (behavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        } else if (behavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun showPlaylistMessage(message: Int, playlistName: String) {
        Toast.makeText(context, getString(message, playlistName), Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        if (isViewModelInitialized) {
            viewModel.onScreenPaused()
        }
    }

    override fun onDestroyView() {
        sheetCallback?.let { sheetBehavior?.removeBottomSheetCallback(it) }
        sheetViews?.list?.adapter = null
        sheetViews = null
        sheetBehavior = null
        sheetCallback = null
        sheetBackCallback = null
        playlistAdapter = null
        playButton = null
        likeButton = null
        playbackTime = null
        boundTrackId = null
        super.onDestroyView()
    }

    private fun render(rootView: View, state: PlayerScreenState) {
        if (boundTrackId != state.track.trackId) {
            bindTrack(rootView, state.track)
            boundTrackId = state.track.trackId
        }

        playButton?.apply {
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
        playbackTime?.text = state.playbackTime
        likeButton?.apply {
            isEnabled = state.isFavoriteButtonEnabled
            isSelected = state.isFavorite
            setImageResource(
                if (state.isFavorite) R.drawable.button__like else R.drawable.button__dislike
            )
            contentDescription = getString(
                if (state.isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites
            )
        }
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

    private data class PlaylistSheetViews(
        val container: LinearLayout,
        val overlay: View,
        val list: RecyclerView,
        val newPlaylist: MaterialButton
    )
}
