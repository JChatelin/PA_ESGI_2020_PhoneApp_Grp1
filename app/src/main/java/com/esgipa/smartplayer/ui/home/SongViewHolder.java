package com.esgipa.smartplayer.ui.home;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.esgipa.smartplayer.R;

public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    TextView musicTitle;
    TextView musicArtist;
    TextView musicAlbumTitle;
    //TextView musicDuration;
    ImageView musicalbumArt;
    OnSongListener onSongListener;

    public SongViewHolder(View itemView, OnSongListener onSongListener) {
        super(itemView);
        this.musicTitle = itemView.findViewById(R.id.music_title);
        this.musicArtist = itemView.findViewById(R.id.music_artist);
        this.musicAlbumTitle = itemView.findViewById(R.id.music_album_title);
        //this.musicDuration = itemView.findViewById(R.id.music_duration);
        this.musicalbumArt = itemView.findViewById(R.id.music_album_art);
        this.onSongListener = onSongListener;

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        onSongListener.onSongClick(getAdapterPosition());
    }

    public interface OnSongListener {
        void onSongClick(int position);
    }
}
