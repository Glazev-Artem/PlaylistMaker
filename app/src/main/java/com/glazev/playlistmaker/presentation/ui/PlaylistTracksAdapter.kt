package com.glazev.playlistmaker.presentation.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.glazev.playlistmaker.R
import com.glazev.playlistmaker.domain.models.Track

class PlaylistTracksAdapter(
    private val onClick: (Track) -> Unit,
    private val onLongClick: (Track) -> Unit
) : ListAdapter<Track, TrackViewHolder>(TrackDiff) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TrackViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.track_view, parent, false)
    )

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = getItem(position)
        holder.bind(track)
        holder.itemView.setOnClickListener { onClick(track) }
        holder.itemView.setOnLongClickListener { onLongClick(track); true }
    }

    private object TrackDiff : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track) = oldItem.trackId == newItem.trackId
        override fun areContentsTheSame(oldItem: Track, newItem: Track) = oldItem == newItem
    }
}
