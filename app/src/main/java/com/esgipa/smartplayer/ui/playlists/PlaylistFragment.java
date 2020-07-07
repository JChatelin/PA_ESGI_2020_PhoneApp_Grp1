package com.esgipa.smartplayer.ui.playlists;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.data.model.Playlist;
import com.esgipa.smartplayer.utils.UserProfileManager;

public class PlaylistFragment extends Fragment {

    private static final String PLAYLIST_POSITION = "playlist_position";

    private int playlistPosition;

    private TextView playlistName, playlistCreator, playlistDescription;

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
        playlistCreator = root.findViewById(R.id.playlist_creator);
        playlistName = root.findViewById(R.id.playlist_name);
        playlistDescription = root.findViewById(R.id.playlist_description);
        //displayInformation();
        return root;
    }

    /*private void displayInformation() {
        Playlist newPlaylist = (Playlist) getArguments().getSerializable(NEW_PLAYLIST);
        playlistName.setText(newPlaylist.getName());
        playlistDescription.setText(newPlaylist.getDescription());
        playlistCreator.setText(newPlaylist.getCreator());
    }*/
}