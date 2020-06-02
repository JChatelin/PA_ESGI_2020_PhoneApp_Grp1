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

    public void setContext(Context context) { }
    public LiveData<List<Playlist>> getPlaylistList() {
        loadPlaylists();
        return playlistList;
    }

    public LiveData<Playlist> getPlaylist(int position) {
        loadPlaylists();
        if(playlistList.getValue() != null) {
            currentPlaylist.setValue(playlistList.getValue().get(position));
        }
        return currentPlaylist;
    }

    private void loadPlaylists() {
        List<Playlist> localPlaylistList = new ArrayList<>();
        /*Field[] fields = R.raw.class.getFields();

        for (Field field : fields) {
            try {
                int resourceId = field.getInt(field);
                localPlaylistList.add(metaDataExtractor.extract(resourceId));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }*/
        playlistList.setValue(localPlaylistList);
    }
}