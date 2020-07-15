package com.esgipa.smartplayer.server.authentication;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.esgipa.smartplayer.utils.ConnectivityUtils;
import com.esgipa.smartplayer.server.Callback;
import com.esgipa.smartplayer.server.RequestResult;
import com.esgipa.smartplayer.utils.StreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SignupTask extends AsyncTask<String, Integer, RequestResult> {
    /**
     * Implementation of AsyncTask designed to fetch data from the network.
     */
    private Callback<JSONObject> callback;
    private String name, username, email, password;
    private ArrayList<String> role;

    public SignupTask(Callback<JSONObject> callback, String name, String username, String email,
                      String password, ArrayList<String> role) {
        setCallback(callback);
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
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
                JSONObject resultJson = signUpRequest(url, name, username, email, password, role);
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
            } else {
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

    private JSONObject signUpRequest(URL serverUrl, String name, String username, String email,
                                           String password, ArrayList<String> role) throws IOException {
        InputStream stream = null;
        HttpURLConnection connection = null;
        JSONObject result = null;
        try {
            connection = (HttpURLConnection) serverUrl.openConnection();
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; UTF-8");
            connection.setRequestProperty("Accept", "application/json");

            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            connection.setDoInput(true);
            connection.setDoOutput(true);

            JSONObject jsonBody = new JSONObject()
                    .put("name", name)
                    .put("username", username)
                    .put("email", email)
                    .put("password", password)
                    .put("role", new JSONArray(role));

            Log.i("server interaction", "signUpRequest query: "+jsonBody.toString());
            OutputStream os = connection.getOutputStream();
            byte[] input = jsonBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
            os.close();
            // parameters end

            connection.connect();
            int responseCode = connection.getResponseCode();
            switch(responseCode) {
                case 200:
                    stream = connection.getInputStream();
                    if (stream != null) {
                        result = StreamReader.readStream(stream, 500);
                    }
                    break;
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
