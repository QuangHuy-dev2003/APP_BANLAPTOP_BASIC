package com.example.quanlycuahanglaptop.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "app_session_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_ROLE = "user_role";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(long id, String name, String email, String phone, String role) {
        prefs.edit()
                .putLong(KEY_USER_ID, id)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_PHONE, phone)
                .putString(KEY_USER_ROLE, role)
                .apply();
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public String getUserPhone() {
        return prefs.getString(KEY_USER_PHONE, null);
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, null);
    }

    public boolean isLoggedIn() {
        return getUserId() > 0;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}


