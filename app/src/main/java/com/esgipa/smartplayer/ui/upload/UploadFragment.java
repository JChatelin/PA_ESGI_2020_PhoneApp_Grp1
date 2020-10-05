package com.esgipa.smartplayer.ui.upload;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.esgipa.smartplayer.MainActivity;
import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.music.MusicPlayerService;
import com.esgipa.smartplayer.ui.viewmodel.DataTransfertViewModel;

public class UploadFragment extends Fragment {
    public static final String TAG = "UploadFragment";
    private static final String uploadPath = "file/upload";

    private Button uploadButton;
    private static ProgressBar progressBar;
    private static TextView percentUpload;

    private MainActivity mainActivity;
    private MusicPlayerService musicPlayerService;

    private static Handler hdlr = new Handler();

    private DataTransfertViewModel dataTransfertViewModel;

    private int progress;
    public static int maxProgress;

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_upload, container, false);
        String uploadUrl = getResources().getString(R.string.server_url)+uploadPath;
        uploadButton = root.findViewById(R.id.upload_button);
        percentUpload = root.findViewById(R.id.progress_text);
        progressBar = root.findViewById(R.id.progressBar);
        dataTransfertViewModel = new ViewModelProvider(requireActivity()).get(DataTransfertViewModel.class);
        mainActivity = (MainActivity) requireActivity();
        musicPlayerService = mainActivity.getMusicPlayerService();
        mainActivity.setUrl(uploadUrl);
        progressBar.setMax(100);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicPlayerService.isRunning) {
                    musicPlayerService.stopMedia();
                }
                selectMusicFile();
            }
        });

        return root;
    }

    private void selectMusicFile() {
        progressBar.setProgress(0);
        progress = 0;
        percentUpload.setText("0%");
        mainActivity.pickUpMusic();
    }

    public static void updateProgressBar(final int percentComplete) {
        new Thread(new Runnable() {
            public void run() {
                // Update the progress bar and display the current value in text view
                hdlr.post(new Runnable() {
                    public void run() {
                        try {
                            final int progress = progressBar.getProgress() + percentComplete;
                            progressBar.setProgress(progress);
                            if (progress > 100) {
                                percentUpload.setText("100%");
                            } else {
                                percentUpload.setText(progress + "%");
                            }
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }
}
