package com.glazev.playlistmaker.presentation.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.glazev.playlistmaker.R
import com.glazev.playlistmaker.creator.Creator
import com.glazev.playlistmaker.domain.api.SearchHistoryInteractor
import com.glazev.playlistmaker.domain.api.TracksInteractor
import com.glazev.playlistmaker.domain.models.Track

import android.widget.ScrollView
import android.widget.ProgressBar
import android.os.Handler
import android.os.Looper

class SearchActivity : AppCompatActivity() {

    private var searchText: String = ""

    private val tracksInteractor = Creator.provideTracksInteractor()
    private lateinit var searchHistoryInteractor: SearchHistoryInteractor

    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { searchRequest() }

    private val tracks = mutableListOf<Track>()
    private val trackAdapter = TrackAdapter(tracks) {
        if (clickDebounce()) {
            searchHistoryInteractor.add(it)
            openPlayer(it)
        }
    }

    private val historyTracks = mutableListOf<Track>()
    private val historyAdapter = TrackAdapter(historyTracks) {
        if (clickDebounce()) {
            searchHistoryInteractor.add(it)
            refreshHistory()
            openPlayer(it)
        }
    }

    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var placeholderNothingFound: LinearLayout
    private lateinit var placeholderError: LinearLayout
    private lateinit var refreshButton: Button
    private lateinit var historyLayout: ScrollView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var clearHistoryButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

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

        searchHistoryInteractor = Creator.provideSearchHistoryInteractor(this)

        val backButton = findViewById<ImageView>(R.id.back_button)
        inputEditText = findViewById(R.id.input_edit_text)
        clearButton = findViewById(R.id.clear_icon)
        recyclerView = findViewById(R.id.recyclerView)
        placeholderNothingFound = findViewById(R.id.placeholder_nothing_found)
        placeholderError = findViewById(R.id.placeholder_error)
        refreshButton = findViewById(R.id.refresh_button)
        historyLayout = findViewById(R.id.history_layout)
        historyRecyclerView = findViewById(R.id.history_recycler_view)
        clearHistoryButton = findViewById(R.id.clear_history_button)
        progressBar = findViewById(R.id.progressBar)

        recyclerView.adapter = trackAdapter
        historyRecyclerView.adapter = historyAdapter

        backButton.setOnClickListener {
            finish()
        }

        clearButton.setOnClickListener {
            inputEditText.setText("")
            tracks.clear()
            trackAdapter.notifyDataSetChanged()
            recyclerView.visibility = View.GONE
            placeholderNothingFound.visibility = View.GONE
            placeholderError.visibility = View.GONE
            progressBar.visibility = View.GONE
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
        }

        refreshButton.setOnClickListener {
            searchRequest()
        }

        clearHistoryButton.setOnClickListener {
            searchHistoryInteractor.clear()
            historyLayout.visibility = View.GONE
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchRequest()
                true
            } else {
                false
            }
        }

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            showHistoryIfRequired(hasFocus)
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s.toString()
                clearButton.visibility = clearButtonVisibility(s)
                
                if (s.isNullOrEmpty()) {
                    handler.removeCallbacks(searchRunnable)
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    placeholderNothingFound.visibility = View.GONE
                    placeholderError.visibility = View.GONE
                    showHistoryIfRequired(inputEditText.hasFocus())
                } else {
                    historyLayout.visibility = View.GONE
                    placeholderNothingFound.visibility = View.GONE
                    placeholderError.visibility = View.GONE
                    searchDebounce()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)

        showHistoryIfRequired(true)
    }

    private fun showHistoryIfRequired(hasFocus: Boolean) {
        if (hasFocus && inputEditText.text.isEmpty() && searchHistoryInteractor.get().isNotEmpty()) {
            refreshHistory()
            historyLayout.visibility = View.VISIBLE
        } else {
            historyLayout.visibility = View.GONE
        }
    }

    private fun refreshHistory() {
        historyTracks.clear()
        historyTracks.addAll(searchHistoryInteractor.get())
        historyAdapter.notifyDataSetChanged()
    }

    private fun openPlayer(track: Track) {
        val intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra(AudioPlayerActivity.EXTRA_TRACK, Creator.provideGson().toJson(track))
        startActivity(intent)
    }

    private fun searchRequest() {
        if (inputEditText.text.isNotEmpty()) {
            placeholderNothingFound.visibility = View.GONE
            placeholderError.visibility = View.GONE
            recyclerView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            
            tracksInteractor.searchTracks(inputEditText.text.toString(), object : TracksInteractor.TracksConsumer {
                override fun consume(foundTracks: List<Track>?, errorMessage: String?) {
                    handler.post {
                        progressBar.visibility = View.GONE
                        if (inputEditText.text.isEmpty()) return@post

                        if (foundTracks != null) {
                            tracks.clear()
                            if (foundTracks.isNotEmpty()) {
                                tracks.addAll(foundTracks)
                                trackAdapter.notifyDataSetChanged()
                                showSearchResults()
                            } else {
                                showNothingFound()
                            }
                        } else {
                            showError()
                        }
                    }
                }
            })
        }
    }

    private fun clickDebounce() : Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun showSearchResults() {
        recyclerView.visibility = View.VISIBLE
        placeholderNothingFound.visibility = View.GONE
        placeholderError.visibility = View.GONE
    }

    private fun showNothingFound() {
        tracks.clear()
        trackAdapter.notifyDataSetChanged()
        recyclerView.visibility = View.GONE
        placeholderNothingFound.visibility = View.VISIBLE
        placeholderError.visibility = View.GONE
    }

    private fun showError() {
        tracks.clear()
        trackAdapter.notifyDataSetChanged()
        recyclerView.visibility = View.GONE
        placeholderNothingFound.visibility = View.GONE
        placeholderError.visibility = View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT, searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchText = savedInstanceState.getString(SEARCH_TEXT, "")
        findViewById<EditText>(R.id.input_edit_text).setText(searchText)
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    companion object {
        const val SEARCH_TEXT = "SEARCH_TEXT"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}
