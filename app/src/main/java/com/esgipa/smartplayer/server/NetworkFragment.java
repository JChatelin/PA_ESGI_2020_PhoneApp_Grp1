package com.esgipa.smartplayer.server;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.esgipa.smartplayer.server.authentication.SigninTask;
import com.esgipa.smartplayer.server.authentication.SignupTask;
import com.esgipa.smartplayer.server.transfert.LoadMusicTask;
import com.esgipa.smartplayer.server.transfert.UploadTask;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;


public class NetworkFragment extends Fragment {
    public static final String TAG = "NetworkFragment";
    private static final String URL_KEY = "UrlKey";

    private Callback<JSONObject> callback;
    private SigninTask signinTask;
    private SignupTask signupTask;
    private UploadTask uploadTask;
    private LoadMusicTask loadMusicTask;
    private String urlString;

    public static NetworkFragment getInstance(FragmentManager fragmentManager, String url) {
        NetworkFragment networkFragment = (NetworkFragment) fragmentManager
                .findFragmentByTag(NetworkFragment.TAG);
        if (networkFragment == null) {
            networkFragment = new NetworkFragment();
            Bundle args = new Bundle();
            args.putString(URL_KEY, url);
            networkFragment.setArguments(args);
            fragmentManager.beginTransaction().add(networkFragment, TAG).commit();
        }
        return networkFragment;
    }

    public void changeUrl(String urlString) {
        this.urlString = urlString;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        urlString = getArguments().getString(URL_KEY);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Host Activity will handle callbacks from task.
        callback = (Callback<JSONObject>) context;
        Log.i(TAG, "onAttach: " + callback.toString());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Clear reference to host Activity to avoid memory leak.
        callback = null;
    }

    @Override
    public void onDestroy() {
        // Cancel task when Fragment is destroyed.
        super.onDestroy();
    }

    public void sendSigninRequest(String username, String password) {
        signinTask = new SigninTask(callback, username, password);
        signinTask.execute(urlString);
    }

    public void sendSignupRequest(String name, String username, String email, String password, ArrayList<String> role) {
        signupTask = new SignupTask(callback, name, username, email, password, role);
        signupTask.execute(urlString);
    }

    public void loadAllMusic(String authToken) {
        loadMusicTask = new LoadMusicTask(callback, authToken);
        loadMusicTask.execute(urlString);
    }

    public void uplaodMusic(InputStream musicFileStream, String authToken, String fileName) {
        cancelUpload();
        uploadTask = new UploadTask(callback, musicFileStream, authToken, fileName);
        uploadTask.execute(urlString);
        Log.i(TAG, "uplaodMusic: upload starting");
    }

    public void cancelUpload() {
        if (uploadTask != null) {
            uploadTask.cancel(true);
        }
    }
}
