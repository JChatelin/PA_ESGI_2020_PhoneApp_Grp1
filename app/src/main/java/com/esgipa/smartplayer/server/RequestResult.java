package com.esgipa.smartplayer.server;

import org.json.JSONObject;

/**
 * Wrapper class that serves as a union of a result value and an exception. When the download
 * task has completed, either the result value or exception can be a non-null value.
 * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
 */
public class RequestResult {
    public JSONObject resultValue;
    public Exception exception;
    public RequestResult(JSONObject resultValue) {
        this.resultValue = resultValue;
    }
    public RequestResult(Exception exception) {
        this.exception = exception;
    }
}
