package com.esgipa.smartplayer.server.filetransfert;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.esgipa.smartplayer.data.model.Song;
import com.esgipa.smartplayer.server.Callback;
import com.esgipa.smartplayer.server.RequestResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class  DownloadTask extends AsyncTask<String, Integer, RequestResult> {
    private Callback<JSONObject> callback;
    private OutputStream musicFileStream;
    private String authToken;
    private Context context;
    private Song currentSong;
    private InputStream fileStream;
    ProgressDialog progressDialog;
    private Handler handler = new Handler();

    public DownloadTask(Callback<JSONObject> callback, OutputStream musicFileStream, String authToken) {
        setCallback(callback);
        this.musicFileStream = musicFileStream;
        this.authToken = authToken;
    }

    void setCallback(Callback<JSONObject> callback) {
        this.callback = callback;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }

    private void showProgressBar() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Downloading...");
        progressDialog.setMessage(currentSong.getTitle());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.show();
        progressDialog.setCancelable(true);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.dismiss();//dismiss dialog
            }
        });
    }
    /**
     * Cancel background network operation if we do not have network connectivity.
     */
    @Override
    protected void onPreExecute() {
        if (callback != null) {
            NetworkInfo networkInfo = callback.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected() ||
                    (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                            && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
                // If no connectivity, cancel task and update Callback with null data.
                callback.updateUi(null);
                cancel(true);
            }
        }
        showProgressBar();
    }

    /**
     * Defines work to perform on the background thread.
     */
    @Override
    protected RequestResult doInBackground(String... urls) {
        RequestResult result = null;
        if (!isCancelled() && urls != null && urls.length > 0) {
            String urlString = urls[0];
            try {
                URL url = new URL(urlString);
                JSONObject jsonResult = downloadMusic(url, musicFileStream, authToken);//downloadUrl(url);
                if (jsonResult != null) {
                    result = new RequestResult(jsonResult);
                } else {
                    throw new IOException("No response received.");
                }
            } catch(Exception e) {
                result = new RequestResult(e);
            }
        }
        return result;
    }

    /**
     * Updates the DownloadCallback with the result.
     */
    @Override
    protected void onPostExecute(RequestResult result) {
        if (result != null && callback != null) {
            if (result.exception != null) {
                callback.updateUi(result.resultValue);
            } else if (result.resultValue != null) {
                callback.updateUi(result.resultValue);
            }
            callback.finishOperation();
        }
    }

    /**
     * Override to add special behavior for cancelled AsyncTask.
     */
    @Override
    protected void onCancelled(RequestResult result) {
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        final int progress;
        if (values[0] > 10) {
            progress = progressDialog.getProgress() + 10;
        } else {
            progress = progressDialog.getProgress() + values[0];
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.setProgress(progress);
            }
        });

    }

    private JSONObject downloadMusic(URL serverUrl, OutputStream musicFileStream, String authToken) throws IOException, JSONException {
        InputStream stream = null;
        HttpURLConnection connection = null;
        JSONObject result = null;

        try {
            connection = (HttpURLConnection) serverUrl.openConnection();

            // Timeout for reading InputStream arbitrarily set to 3000ms.
            connection.setReadTimeout(15000);
            // Timeout for connection.connect() arbitrarily set to 3000ms.
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            connection.setRequestProperty("Authorization", "Bearer " + authToken);
            connection.setRequestProperty("Content-Type", "audio/mpeg");
            connection.setRequestProperty("Accept", "*/*");

            connection.connect();
            int responseCode = connection.getResponseCode();
            switch (responseCode) {
                case HttpURLConnection.HTTP_OK:
                    stream = (InputStream) connection.getContent();
                    byte[] buffer = new byte[connection.getContentLength() / 10];
                    final int maxLength = connection.getContentLength();
                    int length;
                    while ((length = stream.read(buffer)) != -1) {
                        musicFileStream.write(buffer, 0, length);
                        musicFileStream.flush();

                        publishProgress(maxLength / length);
                    }
                    musicFileStream.close();
                    stream.close();
                    progressDialog.dismiss();
                    result = new JSONObject("Download Finished.");
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    Log.i("Download", "downloadMusic: unauthorized");
                    throw new IOException("Access denied.");
                default:
                    Log.i("Download", "default case");
                    throw new IOException("An error occurred.");
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    public void readMusicDataStream(InputStream stream, OutputStream musicFileStream) throws IOException {
        byte[] buffer = new byte[stream.available() / 10];
        final int maxLength = stream.available();
        int length;
        while ((length = stream.read(buffer)) != -1) {
            musicFileStream.write(buffer, 0, length);
            musicFileStream.flush();

            publishProgress( maxLength / length);
        }
        musicFileStream.close();
        stream.close();
    }
}
