package com.glazev.playlistmaker.presentation.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.glazev.playlistmaker.R
import com.glazev.playlistmaker.domain.models.Track
import com.glazev.playlistmaker.presentation.models.SearchScreenContent
import com.glazev.playlistmaker.presentation.models.SearchScreenState
import com.glazev.playlistmaker.presentation.viewmodel.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: SearchViewModel by viewModel()

    private val tracks = mutableListOf<Track>()
    private val trackAdapter = TrackAdapter(tracks) { viewModel.onTrackClicked(it) }

    private val historyTracks = mutableListOf<Track>()
    private val historyAdapter = TrackAdapter(historyTracks) { viewModel.onTrackClicked(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputEditText = view.findViewById<EditText>(R.id.input_edit_text)
        val clearButton = view.findViewById<ImageView>(R.id.clear_icon)

        view.findViewById<RecyclerView>(R.id.recyclerView).adapter = trackAdapter
        view.findViewById<RecyclerView>(R.id.history_recycler_view).adapter = historyAdapter

        clearButton.setOnClickListener {
            inputEditText.setText("")
            val inputMethodManager = requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
        }

        view.findViewById<Button>(R.id.refresh_button).setOnClickListener {
            viewModel.onSearchRequested(inputEditText.text.toString())
        }

        view.findViewById<Button>(R.id.clear_history_button).setOnClickListener {
            viewModel.onHistoryClearClicked()
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onSearchRequested(inputEditText.text.toString())
                true
            } else {
                false
            }
        }

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            viewModel.onFocusChanged(hasFocus)
        }

        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                text: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                text: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                viewModel.onQueryChanged(text?.toString().orEmpty())
            }

            override fun afterTextChanged(text: Editable?) = Unit
        })

        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            render(view, state)
        }

        viewModel.openPlayerEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let(::openPlayer)
        }

        viewModel.onFocusChanged(true)
    }

    override fun onDestroyView() {
        view?.findViewById<RecyclerView>(R.id.recyclerView)?.adapter = null
        view?.findViewById<RecyclerView>(R.id.history_recycler_view)?.adapter = null
        super.onDestroyView()
    }

    private fun render(rootView: View, state: SearchScreenState) {
        val clearButton = rootView.findViewById<ImageView>(R.id.clear_icon)
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.recyclerView)
        val placeholderNothingFound = rootView
            .findViewById<LinearLayout>(R.id.placeholder_nothing_found)
        val placeholderError = rootView.findViewById<LinearLayout>(R.id.placeholder_error)
        val historyLayout = rootView.findViewById<ScrollView>(R.id.history_layout)
        val progressBar = rootView.findViewById<ProgressBar>(R.id.progressBar)

        clearButton.visibility = state.isClearButtonVisible.toVisibility()

        recyclerView.visibility = View.GONE
        placeholderNothingFound.visibility = View.GONE
        placeholderError.visibility = View.GONE
        historyLayout.visibility = View.GONE
        progressBar.visibility = View.GONE

        when (val content = state.content) {
            SearchScreenContent.Idle -> Unit
            SearchScreenContent.Loading -> progressBar.visibility = View.VISIBLE
            is SearchScreenContent.Results -> {
                updateTracks(tracks, content.tracks, trackAdapter)
                recyclerView.visibility = View.VISIBLE
            }
            SearchScreenContent.NothingFound -> {
                updateTracks(tracks, emptyList(), trackAdapter)
                placeholderNothingFound.visibility = View.VISIBLE
            }
            SearchScreenContent.Error -> {
                updateTracks(tracks, emptyList(), trackAdapter)
                placeholderError.visibility = View.VISIBLE
            }
            is SearchScreenContent.History -> {
                updateTracks(historyTracks, content.tracks, historyAdapter)
                historyLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun updateTracks(
        target: MutableList<Track>,
        newTracks: List<Track>,
        adapter: TrackAdapter
    ) {
        target.clear()
        target.addAll(newTracks)
        adapter.notifyDataSetChanged()
    }

    private fun openPlayer(track: Track) {
        val arguments = Bundle().apply {
            putSerializable(AudioPlayerFragment.ARG_TRACK, track)
        }
        findNavController().navigate(
            R.id.action_searchFragment_to_audioPlayerFragment,
            arguments
        )
    }

    private fun Boolean.toVisibility(): Int = if (this) View.VISIBLE else View.GONE
}
