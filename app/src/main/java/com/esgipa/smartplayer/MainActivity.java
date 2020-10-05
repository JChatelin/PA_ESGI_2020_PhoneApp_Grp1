package com.esgipa.smartplayer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import com.esgipa.smartplayer.data.model.Playlist;
import com.esgipa.smartplayer.data.model.Song;
import com.esgipa.smartplayer.music.MetaDataExtractor;
import com.esgipa.smartplayer.ui.upload.UploadFragment;
import com.esgipa.smartplayer.ui.viewmodel.PlaylistSharedViewModel;
import com.esgipa.smartplayer.ui.viewmodel.SongSharedViewModel;
import com.esgipa.smartplayer.utils.UserProfileManager;
import com.esgipa.smartplayer.music.MusicPlayerService;
import com.esgipa.smartplayer.server.Callback;
import com.esgipa.smartplayer.server.NetworkFragment;
import com.esgipa.smartplayer.ui.viewmodel.DataTransfertViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Callback<JSONObject> {

    private static final int REQUEST_CODE_UPLOAD = 10;
    private final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE=1;
    private final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE=2;
    private final int REQUEST_PERMISSION_INTERNET=3;
    private final int REQUEST_PERMISSION_ACCESS_NETWORK_STATE=4;
    private final int REQUEST_PERMISSION_PHONE_STATE=5;

    private MusicPlayerService musicPlayerService;
    public boolean musicServiceBound = false;
    public boolean uploading = false;
    public boolean downloading = false;
    private int uploadFileSize;

    private NetworkFragment networkFragment;

    private DataTransfertViewModel dataTransfertViewModel;
    private SongSharedViewModel songSharedViewModel;
    private PlaylistSharedViewModel playlistSharedViewModel;

    private MetaDataExtractor metaDataExtractor;

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
                R.id.navigation_home, R.id.navigation_music, R.id.navigation_playlists, R.id.navigation_upload, R.id.navigation_profile)
                .build();
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        String musicUrl = getResources().getString(R.string.server_url)+"file";
        metaDataExtractor = new MetaDataExtractor(this);
        dataTransfertViewModel = new ViewModelProvider(this).get(DataTransfertViewModel.class);
        songSharedViewModel = new ViewModelProvider(this).get(SongSharedViewModel.class);
        playlistSharedViewModel = new ViewModelProvider(this).get(PlaylistSharedViewModel.class);
        networkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), musicUrl);
        networkFragment.setContext(this);
        showInternetPermission();
        showNetworkStatePermission();
        showPhoneStatePermission();
        showReadStoragePermission();
        showWriteStoragePermission();
    }

    public void setUrl(String url) {
        networkFragment.changeUrl(url);
    }

    public void setDownloadingSong(Song song) {
        networkFragment.setDownloadSong(song);
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
                        String url = musicList.get(i).toString().replace("\"", "");
                        url = url.replace("download", "read");
                        Log.i("Mainactivity", "updateUi: " +url);
                        list.add(url);
                    }
                    songSharedViewModel.setMusicListUrl(list);
                }
                if(requestResult.has("playlistList")) {
                    Log.i("MainActivity", "loading playlist" );
                    JSONArray playlists = requestResult.getJSONArray("playlistList");
                    ArrayList<Playlist> list = new ArrayList<>();
                    int len = playlists.length();
                    for (int i = 0; i < len; i++) {
                        list.add(getPlaylistsFromJson((JSONObject) playlists.get(i)));
                    }
                    playlistSharedViewModel.setLoadedPlaylist(list);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Playlist getPlaylistsFromJson(JSONObject playlist) throws JSONException {
        String playlistName = playlist.getString("name");
        JSONObject user = playlist.getJSONObject("user");
        String playlistCreator = user.getString("username");
        String playlistDescription = playlist.getString("description");
        List<Song> musicList = new ArrayList<>();

        Log.i("MainActivity", "getPlaylistsFromJson: playlistName : " + playlistName);
        if(playlist.has("musicList")) {
            Log.i("MainActivity", "getPlaylistsFromJson: loading playlist music ");
            JSONArray  musicListJson = playlist.getJSONArray("musicList");
            int len = musicListJson.length();
            for (int i = 0; i < len; i++) {
                String url = getResources().getString(R.string.server_url);
                url += "file/read/";
                url += musicListJson.get(i).toString();
                try {
                    Log.i("MainActivity", "getPlaylistsFromJson: music url : "+url);
                    musicList.add(metaDataExtractor.extract(url));
                } catch (Exception e) {
                    Toast.makeText(this, "An error occured during the loading of the Music in playlist",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        Playlist playlist1 = new Playlist(playlistName, playlistCreator, playlistDescription);
        playlist1.setMusicList(musicList);
        return playlist1;
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
            case Progress.PROCESS_OUTPUT_STREAM_IN_PROGRESS:
                Toast.makeText(this, "Fichier en cours d'upload " + percentComplete, Toast.LENGTH_SHORT).show();
                Log.i("MainActivity", "onProgressUpdate: " + percentComplete);
                UploadFragment.updateProgressBar(percentComplete);
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

    public void downloadMusic(OutputStream musicFileStream, String authToken) {
        networkFragment.downloadMusic(musicFileStream, authToken);
    }

    public void createPlaylist(String authToken, Playlist playlist) {
        networkFragment.createPlaylist(authToken, playlist);
    }

    public void loadAllMusic(String authToken) {
        networkFragment.loadAllMusic(authToken);
    }

    public void loadAllPlaylist(String authToken) {
        networkFragment.loadAllPlaylist(authToken);
    }

    public void addMusicToPlaylist(String authToken, String playlistName, String musicTitle) {
        networkFragment.addMusicToPlaylist(authToken, playlistName, musicTitle);
    }

    public void pickUpMusic() {
        Intent chooseMusicFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseMusicFile.setType("*/*");
        startActivityForResult(chooseMusicFile, REQUEST_CODE_UPLOAD);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_UPLOAD) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    String musicPath = data.getData().getPath();
                    if (musicPath != null) {
                        InputStream musicFileStream = null;
                        try {
                            Uri uri = data.getData();
                            musicFileStream = getContentResolver().openInputStream(uri);
                            if (musicFileStream != null) {
                                uploadFileSize = musicFileStream.available();
                            }
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

    private void showPhoneStatePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
                showExplanation("Permission Needed", "Rationale", Manifest.permission.READ_PHONE_STATE, REQUEST_PERMISSION_PHONE_STATE);
            } else {
                requestPermission(Manifest.permission.READ_PHONE_STATE, REQUEST_PERMISSION_PHONE_STATE);
            }
        } else {
            Toast.makeText(MainActivity.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showReadStoragePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showExplanation("Permission Needed", "Rationale", Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
            } else {
                requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
            }
        } else {
            Toast.makeText(MainActivity.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showWriteStoragePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showExplanation("Permission Needed", "Rationale", Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
            } else {
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            Toast.makeText(MainActivity.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
        }
    }


    private void showInternetPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.INTERNET);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.INTERNET)) {
                showExplanation("Permission Needed", "Rationale", Manifest.permission.INTERNET, REQUEST_PERMISSION_INTERNET);
            } else {
                requestPermission(Manifest.permission.INTERNET, REQUEST_PERMISSION_INTERNET);
            }
        } else {
            Toast.makeText(MainActivity.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNetworkStatePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_NETWORK_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_NETWORK_STATE)) {
                showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_NETWORK_STATE, REQUEST_PERMISSION_ACCESS_NETWORK_STATE);
            } else {
                requestPermission(Manifest.permission.ACCESS_NETWORK_STATE, REQUEST_PERMISSION_ACCESS_NETWORK_STATE);
            }
        } else {
            Toast.makeText(MainActivity.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_PHONE_STATE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

}
