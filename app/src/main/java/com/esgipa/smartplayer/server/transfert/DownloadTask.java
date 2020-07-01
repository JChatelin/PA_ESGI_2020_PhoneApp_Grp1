package com.esgipa.smartplayer.server.transfert;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.esgipa.smartplayer.server.Callback;
import com.esgipa.smartplayer.server.RequestResult;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class  DownloadTask extends AsyncTask<String, Integer, RequestResult> {
    private Callback<JSONObject> callback;

    DownloadTask(Callback<JSONObject> callback) {
        setCallback(callback);
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
                JSONObject jsonResult = null;//downloadUrl(url);
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
}
