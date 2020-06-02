package com.esgipa.smartplayer.ui.playlists;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.esgipa.smartplayer.R;

public class PlaylistFragment extends Fragment {

    private static final String PLAYLIST_POSITION = "playlist_position";

    private int playlistPosition;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }
}