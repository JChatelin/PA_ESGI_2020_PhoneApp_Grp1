package com.esgipa.smartplayer.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class StreamReader {

    public static JSONObject readStream(InputStream stream, int maxReadSize)
            throws IOException, JSONException {
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

    /*private static File getFile(InputStream stream) throws IOException {
        DataInputStream dis = new DataInputStream(stream);
        byte[] buffer = new byte[maxBufferSize];
        int readSize;
        while (((readSize = dis.read(buffer)) != -1)) {
            if (readSize > maxBufferSize) {
                readSize = maxBufferSize;
            }
            buffer = rawBuffer, 0, readSize);
            maxReadSize -= readSize;
        }
    }*/
}

