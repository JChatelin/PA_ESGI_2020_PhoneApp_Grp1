package com.esgipa.smartplayer.server;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.esgipa.smartplayer.data.model.Playlist;
import com.esgipa.smartplayer.data.model.Song;
import com.esgipa.smartplayer.server.authentication.SigninTask;
import com.esgipa.smartplayer.server.authentication.SignupTask;
import com.esgipa.smartplayer.server.playlist.AddMusicToPlaylistTask;
import com.esgipa.smartplayer.server.playlist.CreatePlaylistTask;
import com.esgipa.smartplayer.server.filetransfert.DownloadTask;
import com.esgipa.smartplayer.server.filetransfert.LoadMusicTask;
import com.esgipa.smartplayer.server.filetransfert.UploadTask;
import com.esgipa.smartplayer.server.playlist.LoadPlaylistsTask;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class NetworkFragment extends Fragment {
    public static final String TAG = "NetworkFragment";
    private static final String URL_KEY = "UrlKey";

    private Callback<JSONObject> callback;
    private SigninTask signinTask;
    private SignupTask signupTask;
    private UploadTask uploadTask;
    private DownloadTask downloadTask;
    private LoadMusicTask loadMusicTask;
    private LoadPlaylistsTask loadPlaylistsTask;
    private CreatePlaylistTask createPlaylistTask;
    private AddMusicToPlaylistTask addMusicToPlaylistTask;
    private Context ctxt;
    private Song downloadSong;
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

    public void setContext(Context context) {
        this.ctxt = context;
    }

    public void setDownloadSong(Song downloadSong) {
        this.downloadSong = downloadSong;
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

    public void loadAllPlaylist(String authToken) {
        loadPlaylistsTask = new LoadPlaylistsTask(callback, authToken);
        loadPlaylistsTask.execute(urlString);
    }

    public void createPlaylist(String authToken, Playlist playlist) {
        createPlaylistTask = new CreatePlaylistTask(callback, authToken, playlist);
        createPlaylistTask.execute(urlString);
    }

    public void addMusicToPlaylist(String authToekn, String playlistName, String musicTitle) {
        addMusicToPlaylistTask = new AddMusicToPlaylistTask(callback, authToekn, playlistName, musicTitle);
        addMusicToPlaylistTask.execute(urlString);
    }

    public void uplaodMusic(InputStream musicFileStream, String authToken, String fileName) {
        cancelUpload();
        uploadTask = new UploadTask(callback, musicFileStream, authToken, fileName);
        uploadTask.execute(urlString);
        Log.i(TAG, "uplaodMusic: upload started");
    }

    public void downloadMusic(OutputStream musicFileStream, String authToken) {
        cancelDownload();
        downloadTask = new DownloadTask(callback, musicFileStream, authToken);
        downloadTask.setContext(ctxt);
        downloadTask.setCurrentSong(downloadSong);
        downloadTask.execute(urlString);
        Log.i(TAG, "downloadMusic: download started");
    }

    public void cancelUpload() {
        if (uploadTask != null) {
            uploadTask.cancel(true);
        }
    }

    public void cancelDownload() {
        if (downloadTask != null) {
            downloadTask.cancel(true);
        }
    }
}
