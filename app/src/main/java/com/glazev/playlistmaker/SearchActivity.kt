package com.glazev.playlistmaker

import android.content.Context
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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import android.widget.ScrollView

class SearchActivity : AppCompatActivity() {

    private var searchText: String = ""

    private val retrofit = Retrofit.Builder()
        .baseUrl(ITUNES_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val iTunesService = retrofit.create(ITunesApi::class.java)

    private val tracks = mutableListOf<Track>()
    private val trackAdapter = TrackAdapter(tracks) {
        searchHistory.add(it)
    }

    private val historyTracks = mutableListOf<Track>()
    private val historyAdapter = TrackAdapter(historyTracks) {
        searchHistory.add(it)
        refreshHistory()
    }

    private lateinit var searchHistory: SearchHistory

    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var placeholderNothingFound: LinearLayout
    private lateinit var placeholderError: LinearLayout
    private lateinit var refreshButton: Button
    private lateinit var historyLayout: ScrollView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var clearHistoryButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val sharedPrefs = getSharedPreferences(App.PLAYLIST_MAKER_PREFERENCES, MODE_PRIVATE)
        searchHistory = SearchHistory(sharedPrefs)

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
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
        }

        refreshButton.setOnClickListener {
            searchRequest()
        }

        clearHistoryButton.setOnClickListener {
            searchHistory.clear()
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
            historyLayout.visibility = if (hasFocus && inputEditText.text.isEmpty() && searchHistory.get().isNotEmpty()) {
                refreshHistory()
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s.toString()
                clearButton.visibility = clearButtonVisibility(s)
                
                historyLayout.visibility = if (s?.isEmpty() == true && searchHistory.get().isNotEmpty()) {
                    refreshHistory()
                    View.VISIBLE
                } else {
                    View.GONE
                }
                
                if (s?.isNotEmpty() == true) {
                    placeholderNothingFound.visibility = View.GONE
                    placeholderError.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)

        if (inputEditText.text.isEmpty() && searchHistory.get().isNotEmpty()) {
            refreshHistory()
            historyLayout.visibility = View.VISIBLE
        }
    }

    private fun refreshHistory() {
        historyTracks.clear()
        historyTracks.addAll(searchHistory.get())
        historyAdapter.notifyDataSetChanged()
    }

    private fun searchRequest() {
        if (inputEditText.text.isNotEmpty()) {
            // Скрываем старые результаты и ошибки перед новым запросом
            placeholderNothingFound.visibility = View.GONE
            placeholderError.visibility = View.GONE
            
            iTunesService.search(inputEditText.text.toString()).enqueue(object : Callback<TracksResponse> {
                override fun onResponse(call: Call<TracksResponse>, response: Response<TracksResponse>) {
                    if (response.isSuccessful) {
                        tracks.clear()
                        val results = response.body()?.results
                        if (results?.isNotEmpty() == true) {
                            tracks.addAll(results)
                            trackAdapter.notifyDataSetChanged()
                            showSearchResults()
                        } else {
                            showNothingFound()
                        }
                    } else {
                        showError()
                    }
                }

                override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                    showError()
                }
            })
        }
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
        private const val ITUNES_BASE_URL = "https://itunes.apple.com"
    }
}
