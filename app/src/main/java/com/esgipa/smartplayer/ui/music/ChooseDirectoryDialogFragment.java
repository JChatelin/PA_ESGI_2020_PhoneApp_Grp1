package com.esgipa.smartplayer.ui.music;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.esgipa.smartplayer.MainActivity;
import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.data.model.Song;
import com.esgipa.smartplayer.utils.UserProfileManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ChooseDirectoryDialogFragment extends DialogFragment {
    private String selectedItem;
    private MainActivity mainActivity;
    private String downloadMusicUrl;
    private Song currentSong;
    private List<String> directoryNames;

    public ChooseDirectoryDialogFragment(Song currentSong) {
        this.currentSong = currentSong;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        mainActivity = (MainActivity) requireActivity();
        directoryNames = new ArrayList<>();
        directoryNames.add("Music Folder");
        directoryNames.add("Download Folder");
        downloadMusicUrl = getContext().getResources().getString(R.string.server_url);
        downloadMusicUrl += "download"+currentSong.getFileName();
        mainActivity.setUrl(downloadMusicUrl);
        CharSequence[] cs = directoryNames.toArray(new CharSequence[directoryNames.size()]);

        builder.setTitle("Folder picker")
                .setSingleChoiceItems(cs, 0, null)
                .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                        String directoryPath = null;
                        selectedItem = directoryNames.get(selectedPosition);
                        switch (selectedItem) {
                            case "Music Folder":
                                directoryPath = mainActivity.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getPath();
                                break;
                            case "Download Folder":
                                directoryPath = mainActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath();
                                break;
                        }
                        downloadMusic(directoryPath);
                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ChooseDirectoryDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    private void downloadMusic(String directoryPath) {
        String musicPath = directoryPath+currentSong.getFileName();
        File newMusicFile = new File(musicPath);
        try {
            OutputStream musicFileStream = new FileOutputStream(newMusicFile);
            mainActivity.downloadMusic(musicFileStream, UserProfileManager
                    .getUserInfo(requireContext()).getAuthToken());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
