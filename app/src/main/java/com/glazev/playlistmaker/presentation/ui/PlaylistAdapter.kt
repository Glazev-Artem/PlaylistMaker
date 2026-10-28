package com.glazev.playlistmaker.presentation.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.glazev.playlistmaker.R
import com.glazev.playlistmaker.domain.models.Playlist
import java.io.File

class PlaylistGridAdapter : ListAdapter<Playlist, PlaylistGridViewHolder>(PlaylistDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistGridViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_grid_item, parent, false)
        return PlaylistGridViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistGridViewHolder, position: Int) =
        holder.bind(getItem(position))
}

class PlaylistGridViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val cover: ImageView = view.findViewById(R.id.playlist_item_cover)
    private val name: TextView = view.findViewById(R.id.playlist_item_name)
    private val count: TextView = view.findViewById(R.id.playlist_item_count)

    fun bind(playlist: Playlist) {
        name.text = playlist.name
        count.text = itemView.resources.getQuantityString(
            R.plurals.playlist_track_count, playlist.trackCount, playlist.trackCount
        )
        Glide.with(itemView)
            .load(playlist.coverPath?.let(::File))
            .placeholder(R.drawable.ic_placeholder_album)
            .error(R.drawable.ic_placeholder_album)
            .centerCrop()
            .into(cover)
    }
}

class PlaylistSheetAdapter(
    private val onClick: (Playlist) -> Unit
) : ListAdapter<Playlist, PlaylistSheetViewHolder>(PlaylistDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistSheetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_sheet_item, parent, false)
        return PlaylistSheetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistSheetViewHolder, position: Int) {
        val playlist = getItem(position)
        holder.bind(playlist)
        holder.itemView.setOnClickListener { onClick(playlist) }
    }
}

class PlaylistSheetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val cover: ImageView = view.findViewById(R.id.playlist_item_cover)
    private val name: TextView = view.findViewById(R.id.playlist_item_name)
    private val count: TextView = view.findViewById(R.id.playlist_item_count)

    fun bind(playlist: Playlist) {
        name.text = playlist.name
        count.text = itemView.resources.getQuantityString(
            R.plurals.playlist_track_count, playlist.trackCount, playlist.trackCount
        )
        Glide.with(itemView)
            .load(playlist.coverPath?.let(::File))
            .placeholder(R.drawable.ic_placeholder_album)
            .error(R.drawable.ic_placeholder_album)
            .centerCrop()
            .into(cover)
    }
}

private object PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {
    override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean = oldItem == newItem
}
