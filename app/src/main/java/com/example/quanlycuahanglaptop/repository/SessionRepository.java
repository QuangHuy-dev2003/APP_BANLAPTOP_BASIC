package com.example.quanlycuahanglaptop.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlycuahanglaptop.data.AppDatabase;

import java.util.UUID;

public class SessionRepository {

    private final AppDatabase appDatabase;

    public SessionRepository(Context context) {
        this.appDatabase = AppDatabase.getInstance(context);
    }

    public long create(long userId, long ttlMillis) {
        long now = System.currentTimeMillis();
        long expiresAt = now + ttlMillis;
        String token = UUID.randomUUID().toString();

        SQLiteDatabase db = appDatabase.getConnection();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("token", token);
        values.put("created_at", now);
        values.put("expires_at", expiresAt);
        return db.insert("Session", null, values);
    }

    public boolean hasValidSession(long userId) {
        SQLiteDatabase db = appDatabase.getConnection();
        long now = System.currentTimeMillis();
        Cursor c = db.rawQuery("SELECT id FROM Session WHERE user_id = ? AND expires_at > ? LIMIT 1",
                new String[]{String.valueOf(userId), String.valueOf(now)});
        try {
            return c.moveToFirst();
        } finally {
            c.close();
        }
    }

    public int deleteByUserId(long userId) {
        SQLiteDatabase db = appDatabase.getConnection();
        return db.delete("Session", "user_id = ?", new String[]{String.valueOf(userId)});
    }

    public int deleteExpired() {
        SQLiteDatabase db = appDatabase.getConnection();
        long now = System.currentTimeMillis();
        return db.delete("Session", "expires_at <= ?", new String[]{String.valueOf(now)});
    }
}


