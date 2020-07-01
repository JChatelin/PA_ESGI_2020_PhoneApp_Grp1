package com.esgipa.smartplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.OpenableColumns;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.util.Log;
import android.widget.Toast;

import com.auth0.android.jwt.JWT;
import com.esgipa.smartplayer.data.model.User;
import com.esgipa.smartplayer.ui.authentication.SigninActivity;
import com.esgipa.smartplayer.ui.viewmodel.SongSharedViewModel;
import com.esgipa.smartplayer.utils.UserProfileManager;
import com.esgipa.smartplayer.music.MusicPlayerService;
import com.esgipa.smartplayer.server.Callback;
import com.esgipa.smartplayer.server.NetworkFragment;
import com.esgipa.smartplayer.ui.viewmodel.DataTransfertViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Callback<JSONObject> {

    private static final String musicUrl = "http://192.168.0.14:8082/file";

    private static final int REQUEST_CODE = 10;

    private MusicPlayerService musicPlayerService;
    public boolean musicServiceBound = false;
    public boolean uploading = false;
    public boolean downloading = false;

    private NetworkFragment networkFragment;

    private DataTransfertViewModel dataTransfertViewModel;
    private SongSharedViewModel songSharedViewModel;

    /* return a instance of the music player service */
    public MusicPlayerService getMusicPlayerService() {
        return this.musicPlayerService;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_playlists, R.id.navigation_upload, R.id.navigation_profile)
                .build();
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        dataTransfertViewModel = new ViewModelProvider(this).get(DataTransfertViewModel.class);
        songSharedViewModel = new ViewModelProvider(this).get(SongSharedViewModel.class);
        networkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), musicUrl);
    }

    public void setUrl(String url) {
        networkFragment.changeUrl(url);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService();
    }

    ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Toast.makeText(MainActivity.this, "Service is disconnected", Toast.LENGTH_SHORT).show();
            musicServiceBound = false;
            musicPlayerService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Toast.makeText(MainActivity.this, "Service is connected", Toast.LENGTH_SHORT).show();
            musicServiceBound = true;
            MusicPlayerService.LocalBinder mLocalBinder = (MusicPlayerService.LocalBinder)service;
            musicPlayerService = mLocalBinder.getServiceInstance();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(musicServiceBound) {
            unbindService();
        }
    }

    private void bindService() {
        Intent serviceIntent = new Intent(this, MusicPlayerService.class);
        bindService(serviceIntent, musicServiceConnection, BIND_AUTO_CREATE);
    }

    private void unbindService() {
        unbindService(musicServiceConnection);
        musicServiceBound = false;
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null) {
            return connectivityManager.getActiveNetworkInfo();
        } else {
            return null;
        }
    }

    @Override
    public void updateUi(JSONObject requestResult) {
        try {
            if(requestResult != null) {
                if (requestResult.has("Error")) {
                    Toast.makeText(this, requestResult.getString("Error"), Toast.LENGTH_SHORT).show();
                }
                if (requestResult.has("files link")) {
                    JSONArray musicList = requestResult.getJSONArray("files link");
                    ArrayList<String> list = new ArrayList<>();
                    int len = musicList.length();
                    for (int i = 0; i < len; i++) {
                        list.add(musicList.get(i).toString().replace("\"", ""));
                    }
                    songSharedViewModel.setMusicListUrl(list);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        switch(progressCode) {
            // You can add UI behavior for progress updates here.
            case Progress.ERROR:
                break;
            case Progress.CONNECT_SUCCESS:
                break;
            case Progress.GET_INPUT_STREAM_SUCCESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                break;
            case Progress.PROCESS_OUTPUT_STREAM_IN_PROGRESS:
                dataTransfertViewModel.setUploadPercentage(percentComplete);
                break;
            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                break;
        }
    }

    @Override
    public void finishOperation() {
        uploading = false;
        downloading = false;
        if (networkFragment != null) {
            networkFragment.cancelUpload();
        }
    }

    private void uploadMusic(InputStream musicFileStream, String authToken, String fileName) {
        networkFragment.uplaodMusic(musicFileStream, authToken, fileName);
    }

    public void loadAllMusic(String authToken) {
        networkFragment.loadAllMusic(authToken);
    }

    public void pickUpMusic() {
        Intent chooseMusicFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseMusicFile.setType("*/*");
        startActivityForResult(chooseMusicFile, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    String musicPath = data.getData().getPath();
                    if (musicPath != null) {
                        InputStream musicFileStream = null;
                        try {
                            Uri uri = data.getData();
                            musicFileStream = getContentResolver().openInputStream(uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        uploadMusic(musicFileStream, UserProfileManager.getUserInfo(this).getAuthToken(), getFileName(data.getData()));
                    } else {
                        Toast.makeText(this, "No music selected.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    /*public File importFile(Uri uri) throws IOException {
        String fileName = getFileName(uri);
        Log.i("FileImport", "importFile: "+fileName);
        File tempFile = File.createTempFile(fileName.replace(".mp3", ""), ".mp3");
        return copyToTempFile(uri, tempFile);
    }*/

    private String getFileName(Uri uri) throws IllegalArgumentException {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor.getCount() <= 0) {
            cursor.close();
            throw new IllegalArgumentException("Can't obtain file name, cursor is empty");
        }

        cursor.moveToFirst();

        String fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));

        cursor.close();

        return fileName;
    }

    /*private File copyToTempFile(Uri uri, File tempFile) throws IOException {
        // Obtain an input stream from the uri
        InputStream inputStream = getContentResolver().openInputStream(uri);
        byte[] buffer = new byte[1024];
        int count;

        if (inputStream == null) {
            throw new IOException("Unable to obtain input stream from URI");
        }

        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
        // Copy the stream to the temp file
        while((count = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, count);
        }

        return tempFile;
    }*/
}
