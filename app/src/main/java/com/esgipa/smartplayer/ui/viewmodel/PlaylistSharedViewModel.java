package com.esgipa.smartplayer.ui.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.esgipa.smartplayer.data.model.Playlist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSharedViewModel extends ViewModel {

    private MutableLiveData<List<Playlist>> playlistList;
    private MutableLiveData<Playlist> currentPlaylist;
    private List<Playlist> list = new ArrayList<>();
    private List<Playlist> loadedPlaylist = new ArrayList<>();

    public PlaylistSharedViewModel() {
        playlistList = new MutableLiveData<>();
        currentPlaylist = new MutableLiveData<>();
    }

    public void setLoadedPlaylist(List<Playlist> loadedPlaylist) {
        this.loadedPlaylist = loadedPlaylist;
    }

    public LiveData<List<Playlist>> getPlaylistList() {
        loadPlaylists();
        playlistList.postValue(list);
        return playlistList;
    }

    public LiveData<Playlist> getPlaylist(int position) {
        loadPlaylists();
        if(playlistList.getValue() != null) {
            currentPlaylist.setValue(playlistList.getValue().get(position));
        }
        return currentPlaylist;
    }

    public void addPlaylist(Playlist playlist) {
        list.add(playlist);
    }

    private void loadPlaylists() {
        for (Playlist playlist: loadedPlaylist) {
            if(!list.contains(playlist)) {
                list.add(playlist);
            }
        }
    }
}