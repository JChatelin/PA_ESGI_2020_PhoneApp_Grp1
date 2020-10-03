package com.esgipa.smartplayer.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class DataTransfertViewModel extends ViewModel {
    private MutableLiveData<Integer> uploadPercentage;
    private MutableLiveData<Integer> downloadPercentage;

    public DataTransfertViewModel() {
        uploadPercentage = new MutableLiveData<>();
        downloadPercentage = new MutableLiveData<>();
    }

    public LiveData<Integer> getUploadPercentage() {
        return uploadPercentage;
    }

    public LiveData<Integer> getDownloadPercentage() {
        return downloadPercentage;
    }

    public void setUploadPercentage(Integer uploadPercentage) {
        this.uploadPercentage.setValue(uploadPercentage);
    }

    public void setDownloadPercentage(Integer downloadPercentage) {
        this.downloadPercentage.setValue(downloadPercentage);
    }
}
