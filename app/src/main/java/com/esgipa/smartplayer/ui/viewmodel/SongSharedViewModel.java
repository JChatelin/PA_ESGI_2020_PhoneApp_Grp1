package com.esgipa.smartplayer.ui.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.esgipa.smartplayer.MainActivity;
import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.data.model.Song;
import com.esgipa.smartplayer.music.MetaDataExtractor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SongSharedViewModel extends ViewModel {
    private MetaDataExtractor metaDataExtractor;
    private MutableLiveData<List<Song>> songList;
    private MutableLiveData<Song> currentSong;
    private MutableLiveData<Integer> currentSongIndex;
    private ArrayList<String> musicListUrl;
    private Context context;

    public SongSharedViewModel() {
        songList = new MutableLiveData<>();
        currentSong = new MutableLiveData<>();
        currentSongIndex = new MutableLiveData<>();
    }

    public void setContext(Context context) {
        metaDataExtractor = new MetaDataExtractor(context);
        this.context = context;
    }

    public LiveData<List<Song>> getSongList() {
        loadSongs();
        return songList;
    }

    public LiveData<Song> getSong(int position) {
        if(songList.getValue() != null) {
            if(songList.getValue().size() > 0) {
                currentSong.setValue(songList.getValue().get(position));
            } else {
                currentSong.setValue(null);
            }
        }
        return currentSong;
    }

    public void setMusicListUrl(ArrayList<String> musicListUrl) {
        this.musicListUrl = musicListUrl;
        loadSongs();
    }

    public LiveData<Integer> getCurrentSongIndex() {
        return currentSongIndex;
    }

    public void setCurrentSongIndex(int position) {
        currentSongIndex.postValue(position);
    }

    public void setNextSong(int position) {
        if (position == songList.getValue().size() - 1) {
            setCurrentSongIndex(0);
        } else {
            setCurrentSongIndex(++position);
        }
    }

    public void setPreviousSong(int position) {
        if (position == 0) {
            setCurrentSongIndex(songList.getValue().size() - 1);
        } else {
            setCurrentSongIndex(--position);
        }
    }

    private void loadSongs() {
        List<Song> localSongList = new ArrayList<>();
        if(musicListUrl != null) {
            for(String url: musicListUrl) {
                Log.i("View Model", "loadSongs: " + url);
                //localSongList.add(metaDataExtractor.extract("http://infinityandroid.com/music/good_times.mp3"));
                try {
                    localSongList.add(metaDataExtractor.extract(url));
                } catch (Exception e) {
                    Toast.makeText(context, "An error during song loading", Toast.LENGTH_SHORT).show();
                }
            }
        }
        songList.setValue(localSongList);
    }
}