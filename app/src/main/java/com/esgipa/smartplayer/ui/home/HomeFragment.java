package com.esgipa.smartplayer.ui.home;

import android.os.Bundle;
import android.util.Log;
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
import com.esgipa.smartplayer.ui.music.MusicFragment;
import com.esgipa.smartplayer.ui.viewmodel.SongSharedViewModel;
import com.esgipa.smartplayer.data.model.Song;
import com.esgipa.smartplayer.utils.UserProfileManager;

import java.util.List;

public class HomeFragment extends Fragment implements SongViewHolder.OnSongListener {
    private SongSharedViewModel songSharedViewModel;
    private MainActivity mainActivity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, final Bundle savedInstanceState) {
        songSharedViewModel = new ViewModelProvider(requireActivity()).get(SongSharedViewModel.class);
        songSharedViewModel.setContext(requireContext());
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        final SongRecyclerViewAdapter songRecyclerViewAdapter = new SongRecyclerViewAdapter(requireContext(), this);
        mainActivity = (MainActivity) requireActivity();
        mainActivity.loadAllMusic(UserProfileManager.getUserInfo(requireContext()).getAuthToken());
        songSharedViewModel.getSongList().observe(getViewLifecycleOwner(), new Observer<List<Song>>() {
            @Override
            public void onChanged(@Nullable List<Song> songList) {
                for (Song song: songList) {
                    Log.i("HomeFragment", "onChanged: " + song.getTitle());
                }
                songRecyclerViewAdapter.setSongList(songList);
                recyclerView.setAdapter(songRecyclerViewAdapter);
            }
        });
        return root;
    }

    @Override
    public void onSongClick(int position) {
        songSharedViewModel.setCurrentSongIndex(position);
        MusicFragment musicFragment = new MusicFragment();
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, musicFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


}
