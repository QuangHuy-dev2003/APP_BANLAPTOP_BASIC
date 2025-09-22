package com.example.quanlycuahanglaptop.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlycuahanglaptop.data.AppDatabase;
import com.example.quanlycuahanglaptop.domain.Role;
import com.example.quanlycuahanglaptop.domain.User;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private final AppDatabase appDatabase;

    public UserRepository(Context context) {
        this.appDatabase = AppDatabase.getInstance(context);
    }

    public long create(User user) {
        SQLiteDatabase db = appDatabase.getConnection();
        ContentValues values = new ContentValues();
        values.put("name", user.getName());
        values.put("email", user.getEmail());
        values.put("password", user.getPassword());
        values.put("phone", user.getPhone());
        values.put("role", user.getRole() != null ? user.getRole().name() : Role.USER.name());
        return db.insertOrThrow("User", null, values);
    }

    public int update(User user) {
        SQLiteDatabase db = appDatabase.getConnection();
        ContentValues values = new ContentValues();
        values.put("name", user.getName());
        values.put("email", user.getEmail());
        if (user.getPassword() != null) {
            values.put("password", user.getPassword());
        }
        values.put("phone", user.getPhone());
        if (user.getRole() != null) {
            values.put("role", user.getRole().name());
        }
        return db.update("User", values, "id = ?", new String[]{String.valueOf(user.getId())});
    }

    public int deleteById(long userId) {
        SQLiteDatabase db = appDatabase.getConnection();
        return db.delete("User", "id = ?", new String[]{String.valueOf(userId)});
    }

    public User findById(long userId) {
        SQLiteDatabase db = appDatabase.getConnection();
        Cursor cursor = db.query("User", new String[]{"id","name","email","password","phone","role"},
                "id = ?", new String[]{String.valueOf(userId)}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return mapRow(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public User findByEmail(String email) {
        SQLiteDatabase db = appDatabase.getConnection();
        Cursor cursor = db.query("User", new String[]{"id","name","email","password","phone","role"},
                "email = ?", new String[]{email}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return mapRow(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public List<User> findAll() {
        SQLiteDatabase db = appDatabase.getConnection();
        Cursor cursor = db.query("User", new String[]{"id","name","email","password","phone","role"},
                null, null, null, null, "id DESC");
        List<User> users = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                users.add(mapRow(cursor));
            }
            return users;
        } finally {
            cursor.close();
        }
    }

    private User mapRow(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
        String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
        String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
        String roleStr = cursor.getString(cursor.getColumnIndexOrThrow("role"));
        Role role;
        try {
            role = Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException ex) {
            role = Role.USER;
        }
        return new User(id, name, email, password, phone, role);
    }
}


