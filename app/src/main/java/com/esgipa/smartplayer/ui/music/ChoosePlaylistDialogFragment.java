package com.esgipa.smartplayer.ui.music;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.esgipa.smartplayer.MainActivity;
import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.data.model.Playlist;
import com.esgipa.smartplayer.data.model.Song;
import com.esgipa.smartplayer.utils.UserProfileManager;

import java.util.List;

public class ChoosePlaylistDialogFragment extends DialogFragment {

    private List<Playlist> playlistList;
    private Playlist selectedItem;
    private List<String> playlistNameList;
    private Song currentSong;
    private MainActivity mainActivity;
    private String addMusicUrl;

    public ChoosePlaylistDialogFragment(Song currentSong, List<String> playlistNameList, List<Playlist> playlistList) {
        this.currentSong = currentSong;
        this.playlistNameList = playlistNameList;
        this.playlistList = playlistList;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        mainActivity = (MainActivity) requireActivity();
        addMusicUrl = getContext().getResources().getString(R.string.server_url);
        addMusicUrl += "playlist/music";
        CharSequence[] cs = playlistNameList.toArray(new CharSequence[playlistNameList.size()]);

        builder.setTitle("Playlist picker")
                .setSingleChoiceItems(cs, 0, null)
                .setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                        selectedItem = playlistList.get(selectedPosition);
                        addMusic();
                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ChoosePlaylistDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    private void addMusic() {
        mainActivity.setUrl(addMusicUrl);
        selectedItem.addMusic(currentSong);
        mainActivity.addMusicToPlaylist(UserProfileManager.getUserInfo(requireContext()).getAuthToken(),
                selectedItem.getName(), currentSong.getTitle());
    }
}
