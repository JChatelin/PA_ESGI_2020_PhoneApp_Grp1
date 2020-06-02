package com.esgipa.smartplayer.ui.playlists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.data.model.Playlist;

import java.util.List;

public class PlaylistRecyclerViewAdapter extends RecyclerView.Adapter<PlaylistViewHolder> {
    private List<Playlist> playlistList;
    private Context context;
    private PlaylistViewHolder.OnPlaylistListener onPlaylistListener;

    public PlaylistRecyclerViewAdapter(List<Playlist> playlistList, Context context, PlaylistViewHolder.OnPlaylistListener onPlaylistListener) {
        this.playlistList = playlistList;
        this.context = context;
        this.onPlaylistListener = onPlaylistListener;
    }

    public PlaylistRecyclerViewAdapter(Context context, PlaylistViewHolder.OnPlaylistListener onPlaylistListener) {
        this.context = context;
        this.onPlaylistListener = onPlaylistListener;
    }

    public void setPlaylistList(List<Playlist> playlistList) {
        this.playlistList = playlistList;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_item, parent, false);

        return new PlaylistViewHolder(itemView, onPlaylistListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        holder.playlistName.setText(playlistList.get(position).getName());
        holder.playlistCreator.setText(playlistList.get(position).getCreator());
        holder.playlistDescription.setText(playlistList.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }
}
