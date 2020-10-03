package com.esgipa.smartplayer.ui.download;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.esgipa.smartplayer.R;;
import com.esgipa.smartplayer.ui.viewmodel.DataTransfertViewModel;

public class DownloadFragment extends Fragment {
    public static final String TAG = "DownloadFragment";

    private ProgressBar progressBar;
    private TextView percentDownload;
    private TextView musicTitle;

    private DataTransfertViewModel dataTransfertViewModel;

    private int progress;

    public static DownloadFragment newInstance(String musicTitle) {
        DownloadFragment fragment = new DownloadFragment();
        Bundle args = new Bundle();
        args.putString("music_title", musicTitle);
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_upload, container, false);
        percentDownload = root.findViewById(R.id.progress_text);
        musicTitle = root.findViewById(R.id.music_title);
        progressBar = root.findViewById(R.id.progressBar);
        assert getArguments() != null;
        musicTitle.setText(getArguments().getString("Music title"));
        return root;
    }

    private void updateProgressBar(final int percentComplete) {
        progress = progressBar.getProgress();
        new Thread(new Runnable() {
            public void run() {
                while (progress < 100) {
                    try {
                        progressBar.setProgress(progress);
                        if (progress >= 100) {
                            percentDownload.setText("100%");
                        } else {
                            percentDownload.setText(progress + "%");
                        }
                        progress += percentComplete;
                        // Sleep for 100 milliseconds to show the progress slowly.
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
