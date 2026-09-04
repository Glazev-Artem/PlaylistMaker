package com.glazev.playlistmaker.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2
import com.glazev.playlistmaker.R
import com.glazev.playlistmaker.presentation.ui.library.LibraryPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LibraryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_library)

        val libraryRoot = findViewById<View>(R.id.library_root)
        val paddingLeft = libraryRoot.paddingLeft
        val paddingTop = libraryRoot.paddingTop
        val paddingRight = libraryRoot.paddingRight
        val paddingBottom = libraryRoot.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(libraryRoot) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = paddingLeft + systemBars.left,
                top = paddingTop + systemBars.top,
                right = paddingRight + systemBars.right,
                bottom = paddingBottom + systemBars.bottom
            )
            insets
        }

        findViewById<View>(R.id.back_button).setOnClickListener {
            finish()
        }

        val viewPager = findViewById<ViewPager2>(R.id.library_view_pager)
        viewPager.adapter = LibraryPagerAdapter(this)

        if (savedInstanceState == null) {
            viewPager.setCurrentItem(LibraryPagerAdapter.FAVORITE_TRACKS_POSITION, false)
        }

        val tabs = findViewById<TabLayout>(R.id.library_tabs)
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = getString(
                when (position) {
                    LibraryPagerAdapter.FAVORITE_TRACKS_POSITION -> R.string.library_favorite_tracks
                    else -> R.string.library_playlists
                }
            )
        }.attach()
    }
}
