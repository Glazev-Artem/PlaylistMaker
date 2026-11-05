package com.glazev.playlistmaker.presentation.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.glazev.playlistmaker.R
import com.glazev.playlistmaker.domain.models.Playlist
import com.glazev.playlistmaker.presentation.models.PlaylistDetailEvent
import com.glazev.playlistmaker.presentation.models.PlaylistDetailState
import com.glazev.playlistmaker.presentation.viewmodel.PlaylistDetailViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

class PlaylistDetailFragment : Fragment(R.layout.fragment_playlist_detail) {
    private val args: PlaylistDetailFragmentArgs by navArgs()
    private val viewModel: PlaylistDetailViewModel by viewModel { parametersOf(args.playlistId) }
    private var views: DetailViews? = null
    private var adapter: PlaylistTracksAdapter? = null
    private var menuBehavior: BottomSheetBehavior<LinearLayout>? = null
    private var menuCallback: BottomSheetBehavior.BottomSheetCallback? = null
    private var confirmationDialog: AlertDialog? = null
    private var displayedPlaylist: Playlist? = null
    private var coverPath: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val boundViews = DetailViews(
            cover = view.findViewById(R.id.detail_cover),
            name = view.findViewById(R.id.detail_name),
            description = view.findViewById(R.id.detail_description),
            statistics = view.findViewById(R.id.detail_statistics),
            tracks = view.findViewById(R.id.detail_tracks),
            empty = view.findViewById(R.id.detail_empty_tracks),
            menu = view.findViewById(R.id.detail_menu_sheet),
            overlay = view.findViewById(R.id.detail_overlay),
            summary = PlaylistSheetViewHolder(view.findViewById(R.id.detail_menu_summary)),
            share = view.findViewById(R.id.detail_share),
            more = view.findViewById(R.id.detail_more)
        )
        views = boundViews
        adapter = PlaylistTracksAdapter(viewModel::onTrackClicked, viewModel::onTrackLongClicked)
        boundViews.tracks.adapter = adapter
        val behavior = BottomSheetBehavior.from(boundViews.menu)
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        menuBehavior = behavior
        menuCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val visible = newState != BottomSheetBehavior.STATE_HIDDEN
                boundViews.overlay.isVisible = visible
                if (!visible) viewModel.onMenuVisibilityChanged(false)
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        }.also(behavior::addBottomSheetCallback)
        view.findViewById<View>(R.id.detail_back).setOnClickListener { onBack() }
        boundViews.share.setOnClickListener { viewModel.onShareClicked() }
        boundViews.more.setOnClickListener { viewModel.onMenuVisibilityChanged(true) }
        boundViews.overlay.setOnClickListener { viewModel.onMenuVisibilityChanged(false) }
        view.findViewById<View>(R.id.detail_menu_share).setOnClickListener { viewModel.onShareClicked() }
        view.findViewById<View>(R.id.detail_menu_edit).setOnClickListener { viewModel.onEditClicked() }
        view.findViewById<View>(R.id.detail_menu_delete).setOnClickListener { viewModel.onDeleteClicked() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = onBack()
            }
        )
        viewModel.screenState.observe(viewLifecycleOwner, ::render)
        viewModel.events.observe(viewLifecycleOwner) { event ->
            val controller = findNavController()
            when (val content = event.getContentIfNotHandled()) {
                is PlaylistDetailEvent.OpenTrack -> {
                    if (controller.currentDestination?.id == R.id.playlistDetailFragment) {
                        controller.navigate(PlaylistDetailFragmentDirections.actionPlaylistDetailFragmentToAudioPlayerFragment(content.track))
                    }
                }
                is PlaylistDetailEvent.Edit -> {
                    if (controller.currentDestination?.id == R.id.playlistDetailFragment) {
                        controller.navigate(PlaylistDetailFragmentDirections.actionPlaylistDetailFragmentToNewPlaylistFragment(content.playlistId))
                    }
                }
                PlaylistDetailEvent.Close -> {
                    if (controller.currentDestination?.id == R.id.playlistDetailFragment) controller.popBackStack()
                }
                PlaylistDetailEvent.EmptyShare -> showMessage(R.string.playlist_empty_share)
                PlaylistDetailEvent.Error -> showMessage(R.string.playlist_operation_error)
                null -> Unit
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        viewModel.screenState.value?.let(::render)
    }

    private fun render(state: PlaylistDetailState) {
        val boundViews = views ?: return
        val playlist = state.playlist
        boundViews.name.text = playlist?.name.orEmpty()
        boundViews.description.text = playlist?.description.orEmpty()
        boundViews.description.isVisible = !playlist?.description.isNullOrEmpty()
        val count = resources.getQuantityString(R.plurals.playlist_track_count, state.tracks.size, state.tracks.size)
        val minutes = state.durationMinutes.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        boundViews.statistics.text = getString(R.string.playlist_statistics,
            resources.getQuantityString(R.plurals.playlist_minutes, minutes, minutes), count)
        boundViews.share.isEnabled = playlist != null && !state.isBusy
        boundViews.more.isEnabled = playlist != null && !state.isBusy
        boundViews.empty.isVisible = !state.isLoading && state.tracks.isEmpty()
        adapter?.submitList(state.tracks)
        if (coverPath != playlist?.coverPath) {
            coverPath = playlist?.coverPath
            Glide.with(this).load(coverPath?.let(::File))
                .placeholder(R.drawable.playlist_detail_placeholder)
                .error(R.drawable.playlist_detail_placeholder)
                .centerCrop().into(boundViews.cover)
        }
        if (playlist != null && displayedPlaylist != playlist) {
            boundViews.summary.bind(playlist)
            displayedPlaylist = playlist
        }
        val behavior = menuBehavior ?: return
        if (state.isMenuVisible) {
            boundViews.overlay.isVisible = true
            if (behavior.state == BottomSheetBehavior.STATE_HIDDEN) behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else if (behavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            boundViews.overlay.isVisible = false
        }
        if (confirmationDialog == null) {
            when {
                state.trackToRemove != null -> showTrackRemovalDialog()
                state.showDeleteConfirmation -> showPlaylistRemovalDialog()
            }
        }
    }

    private fun showTrackRemovalDialog() {
        showDialog(MaterialAlertDialogBuilder(requireContext(), R.style.PlaylistDetailDialog)
            .setMessage(R.string.playlist_remove_track_message)
            .setNegativeButton(R.string.playlist_remove_no) { _, _ -> viewModel.onTrackRemovalCancelled() }
            .setPositiveButton(R.string.playlist_remove_yes) { _, _ -> viewModel.onTrackRemovalConfirmed() }
            .setOnCancelListener { viewModel.onTrackRemovalCancelled() })
    }

    private fun showPlaylistRemovalDialog() {
        showDialog(MaterialAlertDialogBuilder(requireContext(), R.style.PlaylistDetailDialog)
            .setTitle(R.string.playlist_delete)
            .setMessage(R.string.playlist_delete_message)
            .setNegativeButton(R.string.playlist_delete_cancel) { _, _ -> viewModel.onDeleteCancelled() }
            .setPositiveButton(R.string.playlist_delete_confirm) { _, _ -> viewModel.onDeleteConfirmed() }
            .setOnCancelListener { viewModel.onDeleteCancelled() })
    }

    private fun showDialog(builder: MaterialAlertDialogBuilder) {
        confirmationDialog = builder.create().also { dialog ->
            dialog.setOnDismissListener { confirmationDialog = null }
            dialog.show()
        }
    }

    private fun onBack() {
        val state = viewModel.screenState.value ?: return
        if (state.isBusy) return
        if (state.isMenuVisible) viewModel.onMenuVisibilityChanged(false)
        else findNavController().popBackStack()
    }

    private fun showMessage(message: Int) = Toast.makeText(context, message, Toast.LENGTH_LONG).show()

    override fun onDestroyView() {
        menuCallback?.let { menuBehavior?.removeBottomSheetCallback(it) }
        confirmationDialog?.setOnDismissListener(null)
        confirmationDialog?.dismiss()
        confirmationDialog = null
        views?.tracks?.adapter = null
        views = null
        adapter = null
        menuBehavior = null
        menuCallback = null
        displayedPlaylist = null
        coverPath = null
        super.onDestroyView()
    }

    private data class DetailViews(
        val cover: ImageView,
        val name: TextView,
        val description: TextView,
        val statistics: TextView,
        val tracks: RecyclerView,
        val empty: TextView,
        val menu: LinearLayout,
        val overlay: View,
        val summary: PlaylistSheetViewHolder,
        val share: ImageView,
        val more: ImageView
    )
}
