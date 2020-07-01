package com.esgipa.smartplayer.server;

import android.net.NetworkInfo;

public interface Callback<T> {
    NetworkInfo getActiveNetworkInfo();

    void updateUi(T requestResult);

    interface Progress {
        int ERROR = -1;
        int CONNECT_SUCCESS = 0;
        int GET_INPUT_STREAM_SUCCESS = 1;
        int PROCESS_INPUT_STREAM_IN_PROGRESS = 2;
        int PROCESS_OUTPUT_STREAM_IN_PROGRESS = 3;
        int PROCESS_INPUT_STREAM_SUCCESS = 4;
    }

    /**
     * Indicate to callback handler any progress update.
     * @param progressCode must be one of the constants defined in DownloadCallback.Progress.
     * @param percentComplete must be 0-100.
     */
    void onProgressUpdate(int progressCode, int percentComplete);

    /**
     * Indicates that the download or upload operation has finished. This method is called even if the
     * download or upload hasn't completed successfully.
     */
    void finishOperation();
}
