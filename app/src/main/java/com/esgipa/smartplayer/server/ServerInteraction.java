package com.esgipa.smartplayer.server;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class ServerInteraction {

    public static JSONObject signUpRequest(URL serverUrl, String name, String username, String email,
                                       String password, ArrayList<String> role) throws IOException {
        InputStream stream = null;
        HttpURLConnection connection = null;
        JSONObject result = null;
        try {
            connection = (HttpURLConnection) serverUrl.openConnection();
            // Timeout for reading InputStream arbitrarily set to 3000ms.
            connection.setReadTimeout(15000);
            // Timeout for connection.connect() arbitrarily set to 3000ms.
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

            Log.i("server interaction", "signInRequest query: "+jsonBody.toString());
            OutputStream os = connection.getOutputStream();
            byte[] input = jsonBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
            os.close();
            // parameters end

            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
            if (stream != null) {
                result = readStream(stream, 500);
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


    public static JSONObject signInRequest(URL serverUrl, String username, String password) throws IOException {
        InputStream stream = null;
        HttpURLConnection connection = null;
        JSONObject result = null;
        try {
            connection = (HttpURLConnection) serverUrl.openConnection();
            // Timeout for reading InputStream arbitrarily set to 3000ms.
            connection.setReadTimeout(15000);
            // Timeout for connection.connect() arbitrarily set to 3000ms.
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; UTF-8");
            connection.setRequestProperty("Accept", "application/json");

            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Add parameters to the post request
            JSONObject jsonBody = new JSONObject()
                    .put("username", username)
                    .put("password", password);

            Log.i("server interaction", "signInRequest query: "+jsonBody.toString());
            OutputStream os = connection.getOutputStream();
            byte[] input = jsonBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
            os.close();
            // parameters end

            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
            if (stream != null) {
                result = readStream(stream, 500);
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

    private static JSONObject readStream(InputStream stream, int maxReadSize)
            throws IOException, UnsupportedEncodingException, JSONException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] rawBuffer = new char[maxReadSize];
        int readSize;
        StringBuilder buffer = new StringBuilder();
        while (((readSize = reader.read(rawBuffer)) != -1) && maxReadSize > 0) {
            if (readSize > maxReadSize) {
                readSize = maxReadSize;
            }
            buffer.append(rawBuffer, 0, readSize);
            maxReadSize -= readSize;
        }
        return new JSONObject(buffer.toString());
    }
}

