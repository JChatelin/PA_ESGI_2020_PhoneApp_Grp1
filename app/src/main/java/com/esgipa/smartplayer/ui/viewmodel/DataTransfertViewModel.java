package com.esgipa.smartplayer.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class DataTransfertViewModel extends ViewModel {
    private MutableLiveData<Integer> uploadPercentage;

    public DataTransfertViewModel() {
        uploadPercentage = new MutableLiveData<>();
    }

    public LiveData<Integer> getUploadPercentage() {
        return uploadPercentage;
    }

    public void setUploadPercentage(Integer uploadPercentage) {
        this.uploadPercentage.setValue(uploadPercentage);
    }
}
