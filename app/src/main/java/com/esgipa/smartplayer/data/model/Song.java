package com.esgipa.smartplayer.data.model;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class Song implements Serializable {
    private String dataSource;
    private String artist;
    private String title;
    @Nullable
    private String album;
    @Nullable
    private String albumArt;
    private long duration;

    public Song(String dataSource, String artist, String title, @Nullable String album, @Nullable String albumArt, long duration) {
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


    @Nullable
    public String getAlbum() {
        return album;
    }

    public String getDataSource() {
        return dataSource;
    }

    @Nullable
    public String getAlbumArt() {
        return albumArt;
    }

    public long getDuration() {
        return duration;
    }
}
