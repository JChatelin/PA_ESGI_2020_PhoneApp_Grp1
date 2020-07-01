package com.esgipa.smartplayer.server.transfert;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.esgipa.smartplayer.utils.ConnectivityUtils;
import com.esgipa.smartplayer.server.Callback;
import com.esgipa.smartplayer.server.RequestResult;
import com.esgipa.smartplayer.utils.StreamReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UploadTask extends AsyncTask<String, Integer, RequestResult> {
    private final static String lineEnd = "\r\n";
    private final static String twoHyphens = "--";
    private final static String boundary = "===" + System.currentTimeMillis() + "===";
    private final static int maxBufferSize = 20 * 1024;

    private Callback<JSONObject> callback;
    private InputStream musicFileStream;
    private String authToken, fileName;

    public UploadTask(Callback<JSONObject> callback, InputStream musicFileStream, String authToken, String fileName) {
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
                callback.updateUi(ConnectivityUtils.noConnection());
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
                JSONObject jsonResult = uploadMusic(url, musicFileStream, authToken, fileName);
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
        callback.onProgressUpdate(Callback.Progress.PROCESS_OUTPUT_STREAM_IN_PROGRESS, values[0]);
    }

    private JSONObject uploadMusic(URL serverUrl, InputStream musicFileStream,
                                   String authToken, String fileName) throws IOException, JSONException {
        InputStream stream = null;
        HttpURLConnection connection = null;
        JSONObject result = null;
        String fieldName = "audio";
        String musicTitle = "goosebumps";
        String artist = "Travis Scott";
        // create a buffer of maximum size
        byte[] buffer = new byte[maxBufferSize];
        final int maxLength = musicFileStream.available();
        int length, progress = 0;

        try{
            connection = (HttpURLConnection) serverUrl.openConnection();

            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // send file data
            Log.i("StreamReader", "uploadMusic: "+serverUrl+ " " + fileName + " " + authToken);
            connection.setRequestProperty("Authorization", "Bearer " + authToken);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);

            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"audio\";filename=\"" + fileName + "\"" + lineEnd);
            dos.writeBytes("Content-Type: audio/mpeg" + lineEnd + lineEnd);

            // read file and write it into form...
            while ((length = musicFileStream.read(buffer)) != -1) {
                dos.write(buffer, 0, length);
                dos.flush();

                progress += length;
                publishProgress((100 * progress) / maxLength);
            }
            // send multipart form data necessary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"title\"" + lineEnd + lineEnd);
            dos.writeBytes(musicTitle + lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"artistName\"" + lineEnd + lineEnd);
            dos.writeBytes(artist+ lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            dos.flush();
            dos.close();

            connection.connect();
            int responseCode = connection.getResponseCode();
            switch(responseCode) {
                case HttpURLConnection.HTTP_OK:
                    stream = connection.getInputStream();
                    if (stream != null) {
                        result = StreamReader.readStream(stream, 500);
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
}
