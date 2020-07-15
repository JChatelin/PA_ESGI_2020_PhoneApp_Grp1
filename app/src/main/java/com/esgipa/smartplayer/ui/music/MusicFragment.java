package com.esgipa.smartplayer.ui.music;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.print.PrinterId;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.esgipa.smartplayer.MainActivity;
import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.data.model.Playlist;
import com.esgipa.smartplayer.data.model.Song;
import com.esgipa.smartplayer.music.MusicPlayerService;
import com.esgipa.smartplayer.ui.viewmodel.PlaylistSharedViewModel;
import com.esgipa.smartplayer.ui.viewmodel.SongSharedViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MusicFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener,
        MusicPlayerService.OnSongPlayingListener {
    private SongSharedViewModel songSharedViewModel;
    private PlaylistSharedViewModel playlistSharedViewModel;

    private MusicPlayerService musicPlayerService;
    private MainActivity mainActivity;

    private long timerStart = 0;
    private long timerEnd = 0;
    private Song currentSong;
    private int currentSongIndex;
    private List<Song> songList;
    private List<String> playlistNameList;
    private List<Playlist> playlistList;

    private Handler myHandler = new Handler();

    private ImageView albumArt, playPause, previousSong, nextSong, download, add_to_playlist;
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
        musicPlayerService.isStartForPlaylist = false;
        musicPlayerService.setOnSongPlayingListener(this);

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

        download = root.findViewById(R.id.download);
        add_to_playlist = root.findViewById(R.id.add_to_playlist);

        songSharedViewModel.setContext(requireContext());

        songList = songSharedViewModel.getSongList().getValue();
        if (songList != null) {
            /* set on click listener to the images */
            playPause.setOnClickListener(this);
            previousSong.setOnClickListener(this);
            nextSong.setOnClickListener(this);
            playTimeBar.setOnSeekBarChangeListener(this);
            add_to_playlist.setOnClickListener(this);
            download.setOnClickListener(this);

            playlistNameList = new ArrayList<>();

            playlistSharedViewModel = new ViewModelProvider(requireActivity()).get(PlaylistSharedViewModel.class);
            playlistSharedViewModel.getPlaylistList().observe(getViewLifecycleOwner(), new Observer<List<Playlist>>() {
                @Override
                public void onChanged(List<Playlist> playlists) {
                    playlistList = playlists;
                    for (Playlist playlist : playlists) {
                        if (!playlistNameList.contains(playlist.getName())) {
                            playlistNameList.add(playlist.getName());
                        }
                    }
                }
            });

            currentSong = songSharedViewModel.getSong(0).getValue();
            if(currentSong != null) {
                if (currentSong != null) {
                    displaySongMetadata(currentSong);
                    musicPlayerService.setActiveSong(currentSong);
                    currentSongIndex = 0;
                }

                songSharedViewModel.getCurrentSongIndex().observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        currentSong = songSharedViewModel.getSong(integer).getValue();
                        displaySongMetadata(currentSong);
                        musicPlayerService.setActiveSong(currentSong);
                        currentSongIndex = integer;
                    }
                });
            } else {
                playPause.setClickable(false);
                nextSong.setClickable(false);
                previousSong.setClickable(false);
                add_to_playlist.setClickable(false);
                download.setClickable(false);
                playTimeBar.setClickable(false);
                playTimeBar.setOnSeekBarChangeListener(this);
                playTimeStart.setText("0");
                playTimeEnd.setText("0");
                musicTitle.setText("Unknown");
                musicArtist.setText("Unknown");
                musicAlbumTitle.setText("Unknown");
            }
        }
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
            currentSong.isPlaying = true;
            updateTimerBar();
            playPause.setImageResource(R.drawable.ic_baseline_pause_24);
        } else {
            musicPlayerService.pauseSong();
            currentSong.isPlaying = false;
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

    private void addToPlaylist() {
        DialogFragment newFragment = new ChoosePlaylistDialogFragment(currentSong, playlistNameList, playlistList);
        newFragment.show(mainActivity.getSupportFragmentManager(), "Playlists");
    }

    private void downloadMusic() {
        DialogFragment newFragment = new ChooseDirectoryDialogFragment(currentSong);
        newFragment.show(mainActivity.getSupportFragmentManager(), "Folders");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.download:
                musicPlayerService.stopMedia();
                downloadMusic();
                break;
            case R.id.add_to_playlist:
                addToPlaylist();
                break;
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
            if(currentSong != null) {
                seekBar.setProgress(progress);
                musicPlayerService.setPlayerToPosition(progress);
                updateTimerBar();
            }
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

    @Override
    public void onSongPlaying() {
        currentSong.isPlaying = true;
        updateTimerBar();
        playPause.setImageResource(R.drawable.ic_baseline_pause_24);
    }

    @Override
    public void onSongStop() {
    }
}
