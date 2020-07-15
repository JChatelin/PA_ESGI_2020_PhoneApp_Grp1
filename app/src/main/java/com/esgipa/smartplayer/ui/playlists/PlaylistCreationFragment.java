package com.esgipa.smartplayer.ui.playlists;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.esgipa.smartplayer.MainActivity;
import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.data.model.Playlist;
import com.esgipa.smartplayer.ui.viewmodel.PlaylistSharedViewModel;
import com.esgipa.smartplayer.utils.UserProfileManager;

public class PlaylistCreationFragment extends Fragment {

    private MainActivity mainActivity;
    private PlaylistSharedViewModel playlistSharedViewModel;
    private EditText playlistName, playlistDescription;
    private Button creationButton;
    private String addPlaylistUrl;
    public PlaylistCreationFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_playlist_creation, container, false);

        addPlaylistUrl = requireContext().getString(R.string.server_url)+"playlist/";
        playlistSharedViewModel = new ViewModelProvider(requireActivity()).get(PlaylistSharedViewModel.class);
        mainActivity = (MainActivity) requireActivity();

        playlistName = root.findViewById(R.id.playlist_name);
        playlistDescription = root.findViewById(R.id.playlist_description);
        creationButton = root.findViewById(R.id.create_playlist);

        creationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPlaylist();
            }
        });
        return root;
    }

    private void createPlaylist() {
        String playlistNameText, playlistDescriptionText;
        playlistNameText = playlistName.getText().toString();
        playlistDescriptionText = playlistDescription.getText().toString();
        Playlist newPlaylist = new Playlist(playlistNameText,
                UserProfileManager.getUserInfo(requireContext()).getUsername(), playlistDescriptionText);
        playlistSharedViewModel.addPlaylist(newPlaylist);
        mainActivity.setUrl(addPlaylistUrl);
        mainActivity.createPlaylist(UserProfileManager.getUserInfo(requireContext()).getAuthToken(), newPlaylist);
        PlaylistListFragment playlistListFragment = new PlaylistListFragment();
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, playlistListFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
