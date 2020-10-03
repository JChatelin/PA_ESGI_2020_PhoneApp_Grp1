package com.esgipa.smartplayer.ui.music;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.esgipa.smartplayer.MainActivity;
import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.data.model.Song;
import com.esgipa.smartplayer.ui.download.DownloadFragment;
import com.esgipa.smartplayer.ui.viewmodel.DataTransfertViewModel;
import com.esgipa.smartplayer.utils.UserProfileManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.DIRECTORY_MUSIC;

public class ChooseDirectoryDialogFragment extends DialogFragment {
    private String selectedItem;
    private MainActivity mainActivity;
    private String downloadMusicUrl;
    private Song currentSong;
    private List<String> directoryNames;
    private static ProgressDialog progressDialog;
    private DataTransfertViewModel dataTransfertViewModel;

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
        dataTransfertViewModel = new ViewModelProvider(requireActivity()).get(DataTransfertViewModel.class);

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
                                directoryPath = Environment.getExternalStoragePublicDirectory(DIRECTORY_MUSIC).getPath();
                                break;
                            case "Download Folder":
                                directoryPath = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath();
                                break;
                        }
                        downloadMusic(directoryPath);
                        showProgessBar();
                        dismiss();
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

    public static void updateProgressBar(final int progressPercentage) {
        int progress = progressDialog.getProgress();
        while (progressDialog.getProgress() < progressDialog.getMax()) {
            try {
                // Sleep for 100 milliseconds to show the progress slowly.
                progress += progressPercentage;
                progressDialog.setProgress(progress);
                Thread.sleep(10);
                if (progressDialog.getProgress() >= progressDialog.getMax()) {
                    progressDialog.dismiss();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void showProgessBar() {
        progressDialog = new ProgressDialog(mainActivity);
        progressDialog.setTitle("Downloading...");
        progressDialog.setMessage(currentSong.getTitle());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.show();
        progressDialog.setCancelable(false);
    }
}
