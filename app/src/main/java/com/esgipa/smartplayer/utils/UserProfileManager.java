package com.esgipa.smartplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.esgipa.smartplayer.data.model.User;

public class UserProfileManager {
    private static final String PREFERENCES_NAME = "auth_user_profile";
    private static final String LOGGED = "logged";
    private static final String EMAIL = "email";
    private static final String USERNAME = "username";
    private static final String NAME = "name";
    private static final String TOKEN = "token";

    public static void saveUserInfo(Context context, User userInfo) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCES_NAME, Context.MODE_PRIVATE);

        sp.edit()
                .putBoolean(LOGGED, userInfo.isLogged())
                .putString(TOKEN, userInfo.getAuthToken())
                .putString(NAME, userInfo.getName())
                .putString(EMAIL, userInfo.getEmail())
                .putString(USERNAME, userInfo.getUsername())
                .apply();
    }

    public static User getUserInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCES_NAME, Context.MODE_PRIVATE);

        return new User(sp.getString(TOKEN, null),sp.getString(NAME, null),
                sp.getString(USERNAME, null), sp.getString(EMAIL, null), sp.getBoolean(LOGGED, false));
    }


    public static void deleteUserInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCES_NAME, Context.MODE_PRIVATE);

        sp.edit()
                .putBoolean(LOGGED, false)
                .putString(TOKEN, null)
                .putString(NAME, null)
                .putString(EMAIL, null)
                .putString(USERNAME, null)
                .apply();
    }
}
