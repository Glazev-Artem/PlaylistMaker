package com.glazev.playlistmaker.presentation.ui.library

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class LibraryPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = PAGE_COUNT

    override fun createFragment(position: Int): Fragment = when (position) {
        FAVORITE_TRACKS_POSITION -> FavoriteTracksFragment.newInstance()
        PLAYLISTS_POSITION -> PlaylistsFragment.newInstance()
        else -> throw IllegalArgumentException("Unknown library page position: $position")
    }

    companion object {
        const val FAVORITE_TRACKS_POSITION = 0
        const val PLAYLISTS_POSITION = 1
        private const val PAGE_COUNT = 2
    }
}
