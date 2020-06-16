package com.esgipa.smartplayer.server;

import android.net.NetworkInfo;

public interface Callback<T> {
    NetworkInfo getActiveNetworkInfo();
    void updateUi(T requestResult);
}
