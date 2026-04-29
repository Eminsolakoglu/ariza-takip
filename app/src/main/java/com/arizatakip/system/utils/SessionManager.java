package com.arizatakip.system.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "ariza_session";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createSession(String username, String role) {
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }
    public String getRole() {
        return prefs.getString(KEY_ROLE, "client");
    }
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}