package com.glazev.playlistmaker.presentation.ui.library

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.glazev.playlistmaker.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LibraryFragment : Fragment(R.layout.fragment_library) {

    private var tabLayoutMediator: TabLayoutMediator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = view.findViewById<ViewPager2>(R.id.library_view_pager)
        viewPager.adapter = LibraryPagerAdapter(this)

        if (savedInstanceState == null) {
            viewPager.setCurrentItem(LibraryPagerAdapter.FAVORITE_TRACKS_POSITION, false)
        }

        val tabs = view.findViewById<TabLayout>(R.id.library_tabs)
        tabLayoutMediator = TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = getString(
                when (position) {
                    LibraryPagerAdapter.FAVORITE_TRACKS_POSITION -> {
                        R.string.library_favorite_tracks
                    }
                    else -> R.string.library_playlists
                }
            )
        }.also { mediator ->
            mediator.attach()
        }
    }

    override fun onDestroyView() {
        tabLayoutMediator?.detach()
        view?.findViewById<ViewPager2>(R.id.library_view_pager)?.adapter = null
        tabLayoutMediator = null
        super.onDestroyView()
    }
}
