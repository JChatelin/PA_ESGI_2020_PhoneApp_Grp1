package com.esgipa.smartplayer.ui.playlists;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.esgipa.smartplayer.R;

public class PlaylistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    TextView playlistName;
    TextView playlistCreator;
    TextView playlistDescription;

    OnPlaylistClickListener onPlaylistClickListener;

    public PlaylistViewHolder(View itemView, OnPlaylistClickListener onPlaylistClickListener) {
        super(itemView);
        this.playlistName = itemView.findViewById(R.id.playlist_name);
        this.playlistCreator = itemView.findViewById(R.id.playlist_creator);
        this.playlistDescription = itemView.findViewById(R.id.playlist_description);
        this.onPlaylistClickListener = onPlaylistClickListener;

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        onPlaylistClickListener.onPlaylistClick(getAdapterPosition());
    }

    public interface OnPlaylistClickListener {
        void onPlaylistClick(int position);
    }
}
