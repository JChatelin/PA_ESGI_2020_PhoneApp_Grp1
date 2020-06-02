package com.esgipa.smartplayer.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.data.model.Song;

import java.util.List;

public class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongViewHolder> {

    private List<Song> songList;
    private Context context;
    private SongViewHolder.OnSongListener onSongListener;

    public SongRecyclerViewAdapter(List<Song> songList, Context context, SongViewHolder.OnSongListener onSongListener) {
        this.songList = songList;
        this.context = context;
        this.onSongListener = onSongListener;
    }

    public SongRecyclerViewAdapter(Context context, SongViewHolder.OnSongListener onSongListener) {
        this.context = context;
        this.onSongListener = onSongListener;
    }

    public void setSongList(List<Song> songList) {
        this.songList = songList;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_item, parent, false);

        return new SongViewHolder(itemView, onSongListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        holder.musicTitle.setText(songList.get(position).getTitle());
        holder.musicArtist.setText(songList.get(position).getArtist());
        holder.musicAlbumTitle.setText(songList.get(position).getAlbum());
        if(songList.get(position).getAlbumArt() != null) {
            //holder.musicalbumArt.setImageBitmap(songList.get(position).getAlbumArt());
        }
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }
}
