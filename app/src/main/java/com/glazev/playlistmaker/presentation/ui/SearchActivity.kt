package com.glazev.playlistmaker.presentation.ui

import android.content.Context
import android.content.Intent
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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.glazev.playlistmaker.R
import com.glazev.playlistmaker.creator.Creator
import com.glazev.playlistmaker.domain.models.Track
import com.glazev.playlistmaker.presentation.models.SearchScreenContent
import com.glazev.playlistmaker.presentation.models.SearchScreenState
import com.glazev.playlistmaker.presentation.viewmodel.SearchViewModel

class SearchActivity : AppCompatActivity() {

    private lateinit var viewModel: SearchViewModel

    private val tracks = mutableListOf<Track>()
    private val trackAdapter = TrackAdapter(tracks) { viewModel.onTrackClicked(it) }

    private val historyTracks = mutableListOf<Track>()
    private val historyAdapter = TrackAdapter(historyTracks) { viewModel.onTrackClicked(it) }

    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var placeholderNothingFound: LinearLayout
    private lateinit var placeholderError: LinearLayout
    private lateinit var historyLayout: ScrollView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        applyWindowInsets()
        bindViews()

        viewModel = ViewModelProvider(
            this,
            Creator.provideSearchViewModelFactory(this)
        )[SearchViewModel::class.java]

        recyclerView.adapter = trackAdapter
        findViewById<RecyclerView>(R.id.history_recycler_view).adapter = historyAdapter

        setClickListeners()
        setInputListeners()
        observeViewModel()

        // История должна быть видна сразу при открытии экрана с пустым запросом.
        viewModel.onFocusChanged(true)
    }

    private fun applyWindowInsets() {
        val searchRoot = findViewById<View>(R.id.search_root)
        val paddingLeft = searchRoot.paddingLeft
        val paddingTop = searchRoot.paddingTop
        val paddingRight = searchRoot.paddingRight
        val paddingBottom = searchRoot.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(searchRoot) { view, insets ->
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
        inputEditText = findViewById(R.id.input_edit_text)
        clearButton = findViewById(R.id.clear_icon)
        recyclerView = findViewById(R.id.recyclerView)
        placeholderNothingFound = findViewById(R.id.placeholder_nothing_found)
        placeholderError = findViewById(R.id.placeholder_error)
        historyLayout = findViewById(R.id.history_layout)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setClickListeners() {
        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        clearButton.setOnClickListener {
            inputEditText.setText("")
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
        }

        findViewById<Button>(R.id.refresh_button).setOnClickListener {
            viewModel.onSearchRequested(inputEditText.text.toString())
        }

        findViewById<Button>(R.id.clear_history_button).setOnClickListener {
            viewModel.onHistoryClearClicked()
        }
    }

    private fun setInputListeners() {
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
    }

    private fun observeViewModel() {
        viewModel.screenState.observe(this) { state ->
            render(state)
        }

        viewModel.openPlayerEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let(::openPlayer)
        }
    }

    private fun render(state: SearchScreenState) {
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
        val intent = Intent(this, AudioPlayerActivity::class.java).apply {
            putExtra(AudioPlayerActivity.EXTRA_TRACK, track)
        }
        startActivity(intent)
    }

    private fun Boolean.toVisibility(): Int = if (this) View.VISIBLE else View.GONE
}
