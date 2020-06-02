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

import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.data.model.Playlist;
import com.esgipa.smartplayer.ui.viewmodel.PlaylistSharedViewModel;

import java.util.List;

public class PlaylistListFragment extends Fragment implements PlaylistViewHolder.OnPlaylistListener {

    private PlaylistSharedViewModel playlistSharedViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, final Bundle savedInstanceState) {
        playlistSharedViewModel = new ViewModelProvider(this).get(PlaylistSharedViewModel.class);
        playlistSharedViewModel.setContext(requireContext());
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        final PlaylistRecyclerViewAdapter playlistRecyclerViewAdapter = new PlaylistRecyclerViewAdapter(requireContext(), this);
        playlistSharedViewModel.getPlaylistList().observe(getViewLifecycleOwner(), new Observer<List<Playlist>>() {
            @Override
            public void onChanged(@Nullable List<Playlist> playlistList) {
                playlistRecyclerViewAdapter.setPlaylistList(playlistList);
                recyclerView.setAdapter(playlistRecyclerViewAdapter);
            }
        });
        return root;
    }

    @Override
    public void onPlaylistClick(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("playlistPosition", position);
        PlaylistFragment playlistFragment = new PlaylistFragment();
        playlistFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, playlistFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


}
