package com.esgipa.smartplayer.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class ConnectivityUtils {
    public  static JSONObject noConnection() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Error", "Aucune connection Wifi ou cellulaire.");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
