package com.esgipa.smartplayer.ui.playlists;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.esgipa.smartplayer.MainActivity;
import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.data.model.Playlist;
import com.esgipa.smartplayer.data.model.Song;
import com.esgipa.smartplayer.music.MusicPlayerService;
import com.esgipa.smartplayer.ui.viewmodel.PlaylistSharedViewModel;
import com.esgipa.smartplayer.utils.UserProfileManager;

import java.io.IOException;
import java.util.List;

public class PlaylistFragment extends Fragment implements SongViewHolder.OnSongListener {

    private static final String PLAYLIST_POSITION = "playlist_position";

    private PlaylistSharedViewModel playlistSharedViewModel;
    private List<Song> musicList;

    private TextView playlistName, playlistCreator, playlistDescription;
    private Button playButton;

    private MusicPlayerService musicPlayerService;
    private MainActivity mainActivity;

    private int currentMusicPosition;

    public PlaylistFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static PlaylistFragment newInstance(int playlistPosition) {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        args.putInt(PLAYLIST_POSITION, playlistPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_playlist, container, false);
        playlistSharedViewModel = new ViewModelProvider(requireActivity()).get(PlaylistSharedViewModel.class);
        final RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        final SongRecyclerViewAdapter songRecyclerViewAdapter = new SongRecyclerViewAdapter(this);

        mainActivity = (MainActivity) requireActivity();
        musicPlayerService = mainActivity.getMusicPlayerService();

        playlistCreator = root.findViewById(R.id.playlist_creator);
        playlistName = root.findViewById(R.id.playlist_name);
        playlistDescription = root.findViewById(R.id.playlist_description);

        playButton = root.findViewById(R.id.play);
        if (getArguments() != null) {
            int playListPosition = getArguments().getInt(PLAYLIST_POSITION);

            playlistSharedViewModel.getPlaylist(playListPosition).observe(getViewLifecycleOwner(), new Observer<Playlist>() {
                @Override
                public void onChanged(Playlist playlist) {
                    musicList = playlist.getMusicList();
                    displayInformation(playlist);
                    songRecyclerViewAdapter.setSongList(playlist.getMusicList());
                    recyclerView.setAdapter(songRecyclerViewAdapter);
                }
            });
        }

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic(musicList, 0);
            }
        });
        return root;
    }

    private void displayInformation(Playlist playlist) {
        playlistName.setText(playlist.getName());
        playlistDescription.setText(playlist.getDescription());
        playlistCreator.setText(playlist.getCreator());
    }

    private void playMusic(List<Song> musicList, int position) {
        if(musicList != null) {
            musicPlayerService.isStartForPlaylist = true;
            musicPlayerService.setPlaylist(musicList, position);
            musicPlayerService.setActiveSong(musicList.get(position));
            musicPlayerService.playSong();
        }
    }

    @Override
    public void onSongClick(int position) {
        playMusic(musicList, position);
    }
}