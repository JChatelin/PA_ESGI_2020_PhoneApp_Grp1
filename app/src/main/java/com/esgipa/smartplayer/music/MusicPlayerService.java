package com.esgipa.smartplayer.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.session.MediaButtonReceiver;

import com.esgipa.smartplayer.MainActivity;
import com.esgipa.smartplayer.data.model.Song;

import java.io.IOException;
import java.util.List;

public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    public static final String NOTIFICATION_CHANNEL_ID = "music_player_channel";
    public boolean isRunning = false;
    private int resumePosition = 0;
    private final IBinder iBinder = new LocalBinder();

    /* media player variable */
    private MediaPlayer mediaPlayer;
    private MediaSessionCompat mediaSession;
    private Song activeSong;
    private Context context;


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        mediaSession = new MediaSessionCompat(context, "MediaSession");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Song activeSong = (Song)intent.getSerializableExtra("activeSong");
        if(activeSong == null) stopSelf();
        createNotification(activeSong);
        isRunning = true;
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        if(mediaPlayer != null) mediaPlayer.release();
        super.onDestroy();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.stop();
    }

    public class LocalBinder extends Binder {
        public MusicPlayerService getServiceInstance() {
            return MusicPlayerService.this;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Music playing";
            String description = "Show the current playing music";
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            NotificationManagerCompat notificationManager = getSystemService(NotificationManagerCompat.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotification(Song activeSong) {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Music Player Service")
                .setContentText("playing : " + activeSong.getTitle())

                // Stop the service when the notification is swiped away
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        PlaybackStateCompat.ACTION_STOP))
                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE)

                // Apply the media style template
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1)
                        .setMediaSession(mediaSession.getSessionToken()))
                .setContentIntent(pendingIntent);
        NotificationManagerCompat.from(MusicPlayerService.this).notify(1, notification.build());
        startForeground(1, notification.build());
    }

    public boolean songIsPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void setActiveSong(Song song) {
        this.activeSong = song;
        try {
            initMediaPlayer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initMediaPlayer() throws IOException {
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(context, Uri.parse(activeSong.getDataSource()));
        mediaPlayer.prepareAsync();
    }

    public int getMusicCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void playSong() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            if(resumePosition != 0) {
                mediaPlayer.seekTo(resumePosition);
            }
            mediaPlayer.start();
        }
    }

    public void setPlayerToPosition(int position) {
        if(mediaPlayer != null) {
            mediaPlayer.seekTo(position);
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public void pauseSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    public void skipToNextSong(List<Song> songList, int songIndex) {
        resumePosition = 0;
        if (songIndex == songList.size() - 1) {
            songIndex = 0;
            stopMedia();
            setActiveSong(songList.get(songIndex));
        } else {
            stopMedia();
            setActiveSong(songList.get(++songIndex));
        }
    }

    public void skipToPreviousSong(List<Song> songList, int songIndex) {
        resumePosition = 0;
        if (songIndex == 0) {
            songIndex = songList.size() - 1;
            stopMedia();
            setActiveSong(songList.get(songIndex));
        } else {
            stopMedia();
            setActiveSong(songList.get(--songIndex));
        }
    }
}
