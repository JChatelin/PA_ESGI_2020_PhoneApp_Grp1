package com.esgipa.smartplayer.ui.music;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.esgipa.smartplayer.MainActivity;
import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.data.model.Song;
import com.esgipa.smartplayer.music.MusicPlayerService;
import com.esgipa.smartplayer.ui.viewmodel.SongSharedViewModel;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MusicFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private SongSharedViewModel songSharedViewModel;
    private MusicPlayerService musicPlayerService;
    private MainActivity mainActivity;
    private long timerStart = 0;
    private long timerEnd = 0;
    private Song currentSong;
    private int currentSongIndex;
    private List<Song> songList;

    private Handler myHandler = new Handler();

    private ImageView albumArt, playPause, previousSong, nextSong;
    private TextView musicTitle, musicArtist, musicAlbumTitle, playTimeStart, playTimeEnd;
    private SeekBar playTimeBar;

    public MusicFragment() {
        // Required empty public constructor
    }

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {
        songSharedViewModel = new ViewModelProvider(requireActivity()).get(SongSharedViewModel.class);
        View root = inflater.inflate(R.layout.fragment_music, container, false);
        /* get the instant of the music player service */
        mainActivity = (MainActivity) requireActivity();
        musicPlayerService = mainActivity.getMusicPlayerService();

        /* music metadata */
        albumArt = root.findViewById(R.id.music_album_art);
        musicTitle = root.findViewById(R.id.music_title);
        musicArtist = root.findViewById(R.id.music_artist);
        musicAlbumTitle = root.findViewById(R.id.music_album_title);

        /* music timer */
        playTimeStart = root.findViewById(R.id.timer_start);
        playTimeEnd = root.findViewById(R.id.timer_end);
        playTimeBar = root.findViewById(R.id.playtime_seekbar);
        playTimeBar.setClickable(false);

        /* player navigation buttons */
        playPause = root.findViewById(R.id.play_pause);
        previousSong = root.findViewById(R.id.previous);
        nextSong = root.findViewById(R.id.next);

        songSharedViewModel.setContext(requireContext());

        songList = songSharedViewModel.getSongList().getValue();

        /* set on click listener to the images */
        playPause.setOnClickListener(this);
        previousSong.setOnClickListener(this);
        nextSong.setOnClickListener(this);
        playTimeBar.setOnSeekBarChangeListener(this);

        songSharedViewModel.getCurrentSongIndex().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                Log.d("songIndex", "onChanged: " + integer);
                currentSong = songSharedViewModel.getSong(integer).getValue();
                displaySongMetadata(currentSong);
                musicPlayerService.setActiveSong(currentSong);
                currentSongIndex = integer;
            }
        });
        return root;
    }

    private void displaySongMetadata(Song song) {
        if (song.getAlbumArt() != null) {
            //albumArt.setImageBitmap(song.getAlbumArt());
        }
        timerEnd = song.getDuration();
        musicTitle.setText(song.getTitle());
        musicArtist.setText(song.getArtist());
        musicAlbumTitle.setText(song.getAlbum());

        playTimeEnd.setText(String.format(Locale.FRENCH, "%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(timerEnd),
                TimeUnit.MILLISECONDS.toSeconds(timerEnd) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timerEnd)))
        );

        playTimeStart.setText(String.format(Locale.FRENCH, "%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(timerStart),
                TimeUnit.MILLISECONDS.toSeconds(timerStart) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timerStart)))
        );
    }

    private void playPauseSong() {
        if (!musicPlayerService.songIsPlaying()) {
            musicPlayerService.playSong();
            updateTimerBar();
            playPause.setImageResource(R.drawable.ic_baseline_pause_24);
        } else {
            musicPlayerService.pauseSong();
            playPause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    private void playPreviousSong() {
        songSharedViewModel.setPreviousSong(currentSongIndex);
        musicPlayerService.skipToPreviousSong(songList, currentSongIndex);
        playPause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
    }

    private void playNextSong() {
        songSharedViewModel.setNextSong(currentSongIndex);
        musicPlayerService.skipToNextSong(songList, currentSongIndex);
        playPause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_pause:
                playPauseSong();
                break;
            case R.id.previous:
                playPreviousSong();
                break;
            case R.id.next:
                playNextSong();
                break;
        }
    }

    // this part handle the music time progress bar
    public void updateTimerBar() {
        timerEnd = currentSong.getDuration();
        timerStart = musicPlayerService.getMusicCurrentPosition();

        playTimeBar.setMax((int) timerEnd);
        playTimeStart.setText(String.format(Locale.FRENCH, "%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(timerStart),
                TimeUnit.MILLISECONDS.toSeconds(timerStart) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timerStart)))
        );

        playTimeEnd.setText(String.format(Locale.FRENCH, "%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(timerEnd),
                TimeUnit.MILLISECONDS.toSeconds(timerEnd) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timerEnd)))
        );
        playTimeBar.setProgress((int) timerStart);
        myHandler.postDelayed(UpdateSongTime, 100);
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            timerStart = musicPlayerService.getMusicCurrentPosition();
            playTimeStart.setText(String.format(Locale.FRENCH, "%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) timerStart),
                    TimeUnit.MILLISECONDS.toSeconds((long) timerStart) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) timerStart)))
            );
            playTimeBar.setProgress((int) timerStart);
            myHandler.postDelayed(this, 100);
        }
    };

    // this part handle music navigation
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser) {
            seekBar.setProgress(progress);
            musicPlayerService.setPlayerToPosition(progress);
            updateTimerBar();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // do nothing
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // do nothing
    }
}
