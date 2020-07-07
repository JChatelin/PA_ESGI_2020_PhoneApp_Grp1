package com.esgipa.smartplayer.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

public class Playlist implements Serializable {
    @NonNull
    private String name;
    @NonNull
    private String creator;
    @Nullable
    private String description;

    public Playlist(@NonNull String name, @NonNull String creator, @Nullable String description) {
        this.name = name;
        this.creator = creator;
        this.description = description;
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
