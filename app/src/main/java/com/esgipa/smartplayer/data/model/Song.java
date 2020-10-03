package com.esgipa.smartplayer.data.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class Song implements Serializable {
    private String dataSource;
    private String artist;
    private String title;
    @Nullable
    private String album;
    @Nullable
    private byte[] albumArt;

    private String fileName;

    private long duration;

    public boolean isPlaying = false;

    public Song(String dataSource, String artist, String title, @Nullable String album, @Nullable byte[] albumArt, long duration) {
        this.dataSource = dataSource;
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.albumArt = albumArt;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Nullable
    public String getAlbum() {
        return album;
    }

    public String getDataSource() {
        return dataSource;
    }

    @Nullable
    public Bitmap getAlbumArt() {
        return BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
    }

    public long getDuration() {
        return duration;
    }
}
