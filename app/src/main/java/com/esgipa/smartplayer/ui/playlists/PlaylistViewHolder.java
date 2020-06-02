package com.esgipa.smartplayer.ui.playlists;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.esgipa.smartplayer.R;

public class PlaylistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    TextView playlistName;
    TextView playlistCreator;
    TextView playlistDescription;

    OnPlaylistListener onPlaylistListener;

    public PlaylistViewHolder(View itemView, OnPlaylistListener onPlaylistListener) {
        super(itemView);
        this.playlistName = itemView.findViewById(R.id.playlist_name);
        this.playlistCreator = itemView.findViewById(R.id.playlist_creator);
        this.playlistDescription = itemView.findViewById(R.id.playlist_description);
        this.onPlaylistListener = onPlaylistListener;

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        onPlaylistListener.onPlaylistClick(getAdapterPosition());
    }

    public interface OnPlaylistListener {
        void onPlaylistClick(int position);
    }
}
