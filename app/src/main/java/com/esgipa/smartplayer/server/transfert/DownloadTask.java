package com.esgipa.smartplayer.server.transfert;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.esgipa.smartplayer.server.Callback;
import com.esgipa.smartplayer.server.RequestResult;
import com.esgipa.smartplayer.utils.StreamReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class  DownloadTask extends AsyncTask<String, Integer, RequestResult> {
    private Callback<JSONObject> callback;
    private OutputStream musicFileStream;
    private String authToken, fileName;
    private final static int maxBufferSize = 20 * 1024;

    public DownloadTask(Callback<JSONObject> callback, OutputStream musicFileStream, String authToken, String fileName) {
        setCallback(callback);
        this.musicFileStream = musicFileStream;
        this.authToken = authToken;
        this.fileName = fileName;
    }

    void setCallback(Callback<JSONObject> callback) {
        this.callback = callback;
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
            connection.setRequestProperty("Content-Type", "application/json; UTF-8");
            connection.setRequestProperty("Accept", "application/json");

            connection.connect();
            int responseCode = connection.getResponseCode();
            switch (responseCode) {
                case HttpURLConnection.HTTP_OK:
                    stream = connection.getInputStream();
                    if (stream != null) {
                        readMusicDataStream(stream, musicFileStream);
                        result = new JSONObject("Download Finished.");
                    }
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    throw new IOException("Access denied.");
                default:
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

    public static void readMusicDataStream(InputStream stream, OutputStream musicFileStream) throws IOException {
        byte[] buffer = new byte[maxBufferSize];
        int length, progress = 0;
        while ((length = stream.read(buffer)) != -1) {
            musicFileStream.write(buffer, 0, length);
            musicFileStream.flush();

            progress += length;
            //publishProgress((100 * progress) / maxLength);
        }
    }
}
