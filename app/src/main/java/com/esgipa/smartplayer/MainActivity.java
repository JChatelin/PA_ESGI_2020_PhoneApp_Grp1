package com.esgipa.smartplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.esgipa.smartplayer.music.MusicPlayerService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    private MusicPlayerService musicPlayerService;
    public boolean musicServiceBound = false;

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
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService();
    }

    ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "Service is disconnected", Toast.LENGTH_SHORT).show();
            musicServiceBound = false;
            musicPlayerService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(MainActivity.this, "Service is connected", Toast.LENGTH_SHORT).show();
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

}
