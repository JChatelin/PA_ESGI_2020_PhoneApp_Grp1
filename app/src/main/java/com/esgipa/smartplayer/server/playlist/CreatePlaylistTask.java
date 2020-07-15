package com.esgipa.smartplayer.server.playlist;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.esgipa.smartplayer.data.model.Playlist;
import com.esgipa.smartplayer.server.Callback;
import com.esgipa.smartplayer.server.RequestResult;
import com.esgipa.smartplayer.utils.ConnectivityUtils;
import com.esgipa.smartplayer.utils.StreamReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CreatePlaylistTask extends AsyncTask<String, Integer, RequestResult> {
    /**
     * Implementation of AsyncTask designed to fetch data from the network.
     */
    private Callback<JSONObject> callback;
    private String authToken;
    private Playlist playlist;

    public CreatePlaylistTask(Callback<JSONObject> callback, String authToken, Playlist playlist) {
        setCallback(callback);
        this.playlist = playlist;
        this.authToken = authToken;
    }

    public void setCallback(Callback<JSONObject> callback) {
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
                JSONObject resultJson = createPlaylist(url, authToken, playlist);
                if (resultJson != null) {
                    result = new RequestResult(resultJson);
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
     * Updates the Callback with the result.
     */
    @Override
    protected void onPostExecute(RequestResult result) {
        if (result != null && callback != null) {
            if (result.exception != null) {
                JSONObject jsonError = new JSONObject();
                try {
                    jsonError.put("Error", result.exception.getMessage());
                    callback.updateUi(jsonError);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (result.resultValue != null) {
                callback.updateUi(result.resultValue);
            }
        }
    }

    /**
     * Override to add special behavior for cancelled AsyncTask.
     */
    @Override
    protected void onCancelled(RequestResult result) {
    }

    private JSONObject createPlaylist(URL serverUrl, String authToken, Playlist playlist) throws IOException {
        InputStream stream = null;
        HttpURLConnection connection = null;
        JSONObject result = null;
        try {
            connection = (HttpURLConnection) serverUrl.openConnection();
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + authToken);
            connection.setRequestProperty("Content-Type", "application/json; UTF-8");
            connection.setRequestProperty("Accept", "application/json");

            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            connection.setDoOutput(true);

            // Add parameters to the post request
            JSONObject jsonBody = new JSONObject()
                    .put("name", playlist.getName())
                    .put("user", playlist.getCreator())
                    .put("description", playlist.getDescription());

            Log.i("createPlaylist", "createPlaylist: "+serverUrl.toString());
            OutputStream os = connection.getOutputStream();
            byte[] input = jsonBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
            os.close();
            // parameters end

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
                    throw new IOException("Invalid username and/or password.");
                default:
                    throw new IOException("An error occurred.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
