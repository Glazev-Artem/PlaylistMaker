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
    private var searchViews: SearchViews? = null

    private val tracks = mutableListOf<Track>()
    private val trackAdapter = TrackAdapter(tracks) { viewModel.onTrackClicked(it) }

    private val historyTracks = mutableListOf<Track>()
    private val historyAdapter = TrackAdapter(historyTracks) { viewModel.onTrackClicked(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val views = SearchViews(
            inputEditText = view.findViewById(R.id.input_edit_text),
            clearButton = view.findViewById(R.id.clear_icon),
            recyclerView = view.findViewById(R.id.recyclerView),
            historyRecyclerView = view.findViewById(R.id.history_recycler_view),
            refreshButton = view.findViewById(R.id.refresh_button),
            clearHistoryButton = view.findViewById(R.id.clear_history_button),
            placeholderNothingFound = view.findViewById(R.id.placeholder_nothing_found),
            placeholderError = view.findViewById(R.id.placeholder_error),
            historyLayout = view.findViewById(R.id.history_layout),
            progressBar = view.findViewById(R.id.progressBar)
        )
        searchViews = views

        views.recyclerView.adapter = trackAdapter
        views.historyRecyclerView.adapter = historyAdapter

        views.clearButton.setOnClickListener {
            views.inputEditText.setText("")
            val inputMethodManager = requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(views.inputEditText.windowToken, 0)
        }

        views.refreshButton.setOnClickListener {
            viewModel.onSearchRequested(views.inputEditText.text.toString())
        }

        views.clearHistoryButton.setOnClickListener {
            viewModel.onHistoryClearClicked()
        }

        views.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onSearchRequested(views.inputEditText.text.toString())
                true
            } else {
                false
            }
        }

        views.inputEditText.setOnFocusChangeListener { _, hasFocus ->
            viewModel.onFocusChanged(hasFocus)
        }

        views.inputEditText.addTextChangedListener(object : TextWatcher {
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
            render(state)
        }

        viewModel.openPlayerEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let(::openPlayer)
        }

        viewModel.onFocusChanged(true)
    }

    override fun onDestroyView() {
        searchViews?.recyclerView?.adapter = null
        searchViews?.historyRecyclerView?.adapter = null
        searchViews = null
        super.onDestroyView()
    }

    private fun render(state: SearchScreenState) {
        val views = searchViews ?: return

        views.clearButton.visibility = state.isClearButtonVisible.toVisibility()

        views.recyclerView.visibility = View.GONE
        views.placeholderNothingFound.visibility = View.GONE
        views.placeholderError.visibility = View.GONE
        views.historyLayout.visibility = View.GONE
        views.progressBar.visibility = View.GONE

        when (val content = state.content) {
            SearchScreenContent.Idle -> Unit
            SearchScreenContent.Loading -> views.progressBar.visibility = View.VISIBLE
            is SearchScreenContent.Results -> {
                updateTracks(tracks, content.tracks, trackAdapter)
                views.recyclerView.visibility = View.VISIBLE
            }
            SearchScreenContent.NothingFound -> {
                updateTracks(tracks, emptyList(), trackAdapter)
                views.placeholderNothingFound.visibility = View.VISIBLE
            }
            SearchScreenContent.Error -> {
                updateTracks(tracks, emptyList(), trackAdapter)
                views.placeholderError.visibility = View.VISIBLE
            }
            is SearchScreenContent.History -> {
                updateTracks(historyTracks, content.tracks, historyAdapter)
                views.historyLayout.visibility = View.VISIBLE
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
        val action = SearchFragmentDirections
            .actionSearchFragmentToAudioPlayerFragment(track)
        findNavController().navigate(action)
    }

    private fun Boolean.toVisibility(): Int = if (this) View.VISIBLE else View.GONE

    private data class SearchViews(
        val inputEditText: EditText,
        val clearButton: ImageView,
        val recyclerView: RecyclerView,
        val historyRecyclerView: RecyclerView,
        val refreshButton: Button,
        val clearHistoryButton: Button,
        val placeholderNothingFound: LinearLayout,
        val placeholderError: LinearLayout,
        val historyLayout: ScrollView,
        val progressBar: ProgressBar
    )
}
