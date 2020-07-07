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

    public PlaylistSharedViewModel() {
        playlistList = new MutableLiveData<>();
        currentPlaylist = new MutableLiveData<>();
    }

    public LiveData<List<Playlist>> getPlaylistList() {
        //loadPlaylists();
        return playlistList;
    }

    public LiveData<Playlist> getPlaylist(int position) {
        //loadPlaylists();
        if(playlistList.getValue() != null) {
            currentPlaylist.setValue(playlistList.getValue().get(position));
        }
        return currentPlaylist;
    }

    public void addPlaylist(Playlist playlist) {
        List<Playlist> list = playlistList.getValue();
        list.add(playlist);
        playlistList.setValue(list);
    }

    private void loadPlaylists() {
        List<Playlist> localPlaylistList = new ArrayList<>();
        playlistList.setValue(localPlaylistList);
    }
}