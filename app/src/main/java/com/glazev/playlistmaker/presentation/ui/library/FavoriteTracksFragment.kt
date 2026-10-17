package com.glazev.playlistmaker.presentation.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.glazev.playlistmaker.R
import com.glazev.playlistmaker.domain.models.Track
import com.glazev.playlistmaker.presentation.models.FavoriteTracksScreenState
import com.glazev.playlistmaker.presentation.ui.TrackAdapter
import com.glazev.playlistmaker.presentation.viewmodel.FavoriteTracksViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoriteTracksFragment : Fragment() {

    private val viewModel: FavoriteTracksViewModel by viewModel()
    private val tracks = mutableListOf<Track>()
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_favorite_tracks, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val placeholder = view.findViewById<Group>(R.id.empty_favorite_tracks_group)
        val trackList = view.findViewById<RecyclerView>(R.id.favorite_tracks_list)
        val trackAdapter = TrackAdapter(tracks) { viewModel.onTrackClicked(it) }
        recyclerView = trackList
        trackList.adapter = trackAdapter

        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            val favoriteTracks = (state as? FavoriteTracksScreenState.Content)?.tracks.orEmpty()
            tracks.clear()
            tracks.addAll(favoriteTracks)
            trackAdapter.notifyDataSetChanged()
            placeholder.isVisible = state is FavoriteTracksScreenState.Empty
            trackList.isVisible = state is FavoriteTracksScreenState.Content
        }

        viewModel.openPlayerEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { track ->
                val action = LibraryFragmentDirections.actionLibraryFragmentToAudioPlayerFragment(track)
                findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        recyclerView?.adapter = null
        recyclerView = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = FavoriteTracksFragment()
    }
}
