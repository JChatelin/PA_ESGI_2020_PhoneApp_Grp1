package com.esgipa.smartplayer.ui.playlists;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.esgipa.smartplayer.MainActivity;
import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.data.model.Playlist;
import com.esgipa.smartplayer.ui.viewmodel.PlaylistSharedViewModel;
import com.esgipa.smartplayer.utils.UserProfileManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class PlaylistListFragment extends Fragment implements PlaylistViewHolder.OnPlaylistClickListener {

    private static final String NEW_PLAYLIST = "new_playlist";

    private PlaylistSharedViewModel playlistSharedViewModel;
    private FloatingActionButton createPlaylist;
    private String loadPlaylistUrl;
    private Thread loadingThread;

    private MainActivity mainActivity;

    public static PlaylistListFragment newInstance(Playlist newPlaylist) {
        PlaylistListFragment fragment = new PlaylistListFragment();
        Bundle args = new Bundle();
        args.putSerializable(NEW_PLAYLIST, newPlaylist);
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, final Bundle savedInstanceState) {
        playlistSharedViewModel = new ViewModelProvider(requireActivity()).get(PlaylistSharedViewModel.class);
        View root = inflater.inflate(R.layout.fragment_playlist_list, container, false);
        loadPlaylistUrl = requireContext().getResources().getString(R.string.server_url);
        loadPlaylistUrl += "playlist";

        mainActivity = (MainActivity) requireActivity();
        mainActivity.setUrl(loadPlaylistUrl);
        mainActivity.loadAllPlaylist(UserProfileManager.getUserInfo(requireContext()).getAuthToken());
        final RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        final PlaylistRecyclerViewAdapter playlistRecyclerViewAdapter = new PlaylistRecyclerViewAdapter(requireContext(), this);
        createPlaylist = root.findViewById(R.id.newPlaylist);
        createPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlaylistCreationForm();
            }
        });
        loadPlaylistAsync(playlistSharedViewModel, playlistRecyclerViewAdapter, recyclerView);
        return root;
    }

    private void loadPlaylistAsync(final PlaylistSharedViewModel playlistSharedViewModel,
                                   final PlaylistRecyclerViewAdapter playlistRecyclerViewAdapter,
                                   final RecyclerView recyclerView) {
        loadingThread = new Thread( new Runnable(){
            @Override
            public void run() {
                playlistSharedViewModel.getPlaylistList().observe(getViewLifecycleOwner(), new Observer<List<Playlist>>() {
                    @Override
                    public void onChanged(@Nullable List<Playlist> playlistList) {
                        playlistRecyclerViewAdapter.setPlaylistList(playlistList);
                        recyclerView.setAdapter(playlistRecyclerViewAdapter);
                    }
                });
            }
        });
        loadingThread.start();
    }

    private void openPlaylistCreationForm() {
        PlaylistCreationFragment playlistCreationFragment = new PlaylistCreationFragment();
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, playlistCreationFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onPlaylistClick(int position) {
        PlaylistFragment playlistFragment = PlaylistFragment.newInstance(position);
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, playlistFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}
