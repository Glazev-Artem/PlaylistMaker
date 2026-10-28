package com.glazev.playlistmaker.presentation.ui.library

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glazev.playlistmaker.R
import com.glazev.playlistmaker.presentation.ui.PlaylistGridAdapter
import com.glazev.playlistmaker.presentation.viewmodel.PlaylistsViewModel
import com.google.android.material.button.MaterialButton
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private val viewModel: PlaylistsViewModel by viewModel()
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_playlists, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val placeholder = view.findViewById<Group>(R.id.empty_playlists_group)
        val list = view.findViewById<RecyclerView>(R.id.playlists_grid)
        val adapter = PlaylistGridAdapter()
        recyclerView = list
        list.layoutManager = GridLayoutManager(requireContext(), 2)
        list.addItemDecoration(
            PlaylistSpacing(
                resources.getDimensionPixelSize(R.dimen.playlist_grid_gap),
                resources.getDimensionPixelSize(R.dimen.playlist_grid_vertical_gap)
            )
        )
        list.adapter = adapter
        view.findViewById<MaterialButton>(R.id.new_playlist_button).setOnClickListener {
            findNavController().navigate(LibraryFragmentDirections.actionLibraryFragmentToNewPlaylistFragment())
        }
        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.playlists)
            list.isVisible = state.playlists.isNotEmpty()
            placeholder.isVisible = state.playlists.isEmpty()
        }
    }

    override fun onDestroyView() {
        recyclerView?.adapter = null
        recyclerView = null
        super.onDestroyView()
    }

    private class PlaylistSpacing(private val gap: Int, private val verticalGap: Int) :
        RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) return
            if (position % 2 == 0) outRect.right = gap / 2 else outRect.left = gap - gap / 2
            outRect.bottom = verticalGap
        }
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}
