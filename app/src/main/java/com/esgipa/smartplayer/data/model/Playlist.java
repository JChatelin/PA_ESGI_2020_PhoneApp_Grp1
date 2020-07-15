package com.esgipa.smartplayer.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Playlist implements Serializable {
    @NonNull
    private String name;
    @NonNull
    private String creator;
    @Nullable
    private String description;

    private List<Song> musicList;

    public Playlist(@NonNull String name, @NonNull String creator, @Nullable String description) {
        this.name = name;
        this.creator = creator;
        this.description = description;
        this.musicList = new ArrayList<>();
    }

    public void addMusic(Song song) {
        musicList.add(song);
    }

    public List<Song> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<Song> musicList) {
        this.musicList = musicList;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getCreator() {
        return creator;
    }

    @Nullable
    public String getDescription() {
        return description;
    }
}
