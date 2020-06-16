package com.esgipa.smartplayer.server.authentication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.auth0.android.jwt.JWT;
import com.esgipa.smartplayer.data.model.User;
import com.esgipa.smartplayer.data.utils.UserProfileManager;
import com.esgipa.smartplayer.server.Callback;
import com.esgipa.smartplayer.server.RequestResult;
import com.esgipa.smartplayer.server.ServerInteraction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class SigninTask extends AsyncTask<String, Integer, RequestResult>{
    /**
     * Implementation of AsyncTask designed to fetch data from the network.
     */
    private Callback<JSONObject> callback;
    private String username, password;

    public SigninTask(Callback<JSONObject> callback, String username, String password) {
        setCallback(callback);
        this.username = username;
        this.password = password;
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
                JSONObject resultJson = ServerInteraction.signInRequest(url, username, password);
                if (resultJson != null) {
                    result = new RequestResult(resultJson);
                } else {
                    throw new IOException("No response received.");
                }
            } catch(Exception e) {
                result = new RequestResult(e);
            }
        }
        Log.i("Signin Task", "signInRequest result: "+result.resultValue);
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
}
