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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.esgipa.smartplayer.MainActivity;
import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.ui.viewmodel.DataTransfertViewModel;

public class UploadFragment extends Fragment {
    public static final String TAG = "UploadFragment";
    private static final String uploadUrl = "http://192.168.0.14:8082/file/upload";

    private Button uploadButton;
    private ProgressBar progressBar;
    private TextView percentUpload;

    private MainActivity mainActivity;

    private DataTransfertViewModel dataTransfertViewModel;

    private Handler hdlr = new Handler();
    private int progress;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_upload, container, false);
        uploadButton = root.findViewById(R.id.upload_button);
        percentUpload = root.findViewById(R.id.progress_text);
        progressBar = root.findViewById(R.id.progressBar);
        dataTransfertViewModel = new ViewModelProvider(requireActivity()).get(DataTransfertViewModel.class);
        mainActivity = (MainActivity) requireActivity();
        mainActivity.setUrl(uploadUrl);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectMusicFile();
            }
        });
        dataTransfertViewModel.getUploadPercentage().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                updateProgressBar(integer);
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

    private void updateProgressBar(final int percentComplete) {
        progress = progressBar.getProgress();
        new Thread(new Runnable() {
            public void run() {
                while (progress < 100) {
                    progress += percentComplete;
                    // Update the progress bar and display the current value in text view
                    hdlr.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progress);
                            if (progress >= 100) {
                                percentUpload.setText("100%");
                            } else {
                                percentUpload.setText(progress + "%");
                            }
                        }
                    });
                    try {
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
