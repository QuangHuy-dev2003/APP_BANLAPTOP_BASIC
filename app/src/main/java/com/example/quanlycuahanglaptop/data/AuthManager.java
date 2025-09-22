package com.example.quanlycuahanglaptop.data;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {

    private static final String PREF_NAME = "app_auth_pref";
    private static final String KEY_LOGGED_IN = "key_logged_in";

    private final SharedPreferences sharedPreferences;

    public AuthManager(Context context) {
        this.sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean loggedIn) {
        sharedPreferences.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply();
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
}


