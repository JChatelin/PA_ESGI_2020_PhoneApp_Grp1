package com.esgipa.smartplayer.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.esgipa.smartplayer.data.model.Song;

public class CurrentPlayingSongViewModel extends ViewModel {
    MutableLiveData<Boolean> isPlaying;
    MutableLiveData<Integer> resumePosition;
    MutableLiveData<Song> currentSong;

    public CurrentPlayingSongViewModel() {}

    public void setCurrentSong(Song currentSong) {
        this.currentSong.setValue(currentSong);
    }

    public void getResumePosition(int position) {
        this.resumePosition.setValue(position);
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying.setValue(isPlaying);
    }

    public LiveData<Boolean> getPlayingState() {
        return isPlaying;
    }

    public LiveData<Integer> getResumePosition() {
        return resumePosition;
    }

    public LiveData<Song> getCurrentSong() {
        return currentSong;
    }
}
