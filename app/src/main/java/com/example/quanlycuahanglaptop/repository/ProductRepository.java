package com.example.quanlycuahanglaptop.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlycuahanglaptop.data.AppDatabase;
import com.example.quanlycuahanglaptop.domain.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    private final AppDatabase appDatabase;

    public ProductRepository(Context context) {
        this.appDatabase = AppDatabase.getInstance(context);
    }

    public long create(Product product) {
        SQLiteDatabase db = appDatabase.getConnection();
        ContentValues values = new ContentValues();
        values.put("name", product.getName());
        values.put("description", product.getDescription());
        values.put("price", product.getPrice());
        values.put("quantity", product.getQuantity());
        values.put("image", product.getImage());
        return db.insert("Product", null, values);
    }

    public int update(Product product) {
        SQLiteDatabase db = appDatabase.getConnection();
        ContentValues values = new ContentValues();
        values.put("name", product.getName());
        values.put("description", product.getDescription());
        values.put("price", product.getPrice());
        values.put("quantity", product.getQuantity());
        values.put("image", product.getImage());
        return db.update("Product", values, "id = ?", new String[]{String.valueOf(product.getId())});
    }

    public int deleteById(long id) {
        SQLiteDatabase db = appDatabase.getConnection();
        return db.delete("Product", "id = ?", new String[]{String.valueOf(id)});
    }

    public Product findById(long id) {
        SQLiteDatabase db = appDatabase.getConnection();
        Cursor c = db.rawQuery("SELECT id, name, description, price, quantity, image FROM Product WHERE id = ?",
                new String[]{String.valueOf(id)});
        try {
            if (c.moveToFirst()) {
                return mapRow(c);
            }
            return null;
        } finally {
            c.close();
        }
    }

    public List<Product> findPageOrderedByIdDesc(int page, int pageSize) {
        int offset = Math.max(0, (page - 1) * pageSize);
        SQLiteDatabase db = appDatabase.getConnection();
        Cursor c = db.rawQuery("SELECT id, name, description, price, quantity, image FROM Product ORDER BY id DESC LIMIT ? OFFSET ?",
                new String[]{String.valueOf(pageSize), String.valueOf(offset)});
        try {
            List<Product> list = new ArrayList<>();
            while (c.moveToNext()) {
                list.add(mapRow(c));
            }
            return list;
        } finally {
            c.close();
        }
    }

    public int countAll() {
        SQLiteDatabase db = appDatabase.getConnection();
        Cursor c = db.rawQuery("SELECT COUNT(1) FROM Product", null);
        try {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        } finally {
            c.close();
        }
    }

    public List<Product> searchByNameOrderedDesc(String keyword, int page, int pageSize) {
        int offset = Math.max(0, (page - 1) * pageSize);
        SQLiteDatabase db = appDatabase.getConnection();
        String like = "%" + keyword + "%";
        Cursor c = db.rawQuery(
                "SELECT id, name, description, price, quantity, image FROM Product WHERE name LIKE ? ORDER BY id DESC LIMIT ? OFFSET ?",
                new String[]{like, String.valueOf(pageSize), String.valueOf(offset)});
        try {
            List<Product> list = new ArrayList<>();
            while (c.moveToNext()) {
                list.add(mapRow(c));
            }
            return list;
        } finally {
            c.close();
        }
    }

    public int countByName(String keyword) {
        SQLiteDatabase db = appDatabase.getConnection();
        String like = "%" + keyword + "%";
        Cursor c = db.rawQuery("SELECT COUNT(1) FROM Product WHERE name LIKE ?", new String[]{like});
        try {
            if (c.moveToFirst()) return c.getInt(0);
            return 0;
        } finally {
            c.close();
        }
    }

    public List<Product> findPageOrderedByPriceAsc(int page, int pageSize) {
        int offset = Math.max(0, (page - 1) * pageSize);
        SQLiteDatabase db = appDatabase.getConnection();
        Cursor c = db.rawQuery("SELECT id, name, description, price, quantity, image FROM Product ORDER BY price ASC LIMIT ? OFFSET ?",
                new String[]{String.valueOf(pageSize), String.valueOf(offset)});
        try {
            List<Product> list = new ArrayList<>();
            while (c.moveToNext()) {
                list.add(mapRow(c));
            }
            return list;
        } finally {
            c.close();
        }
    }

    public List<Product> findPageOrderedByPriceDesc(int page, int pageSize) {
        int offset = Math.max(0, (page - 1) * pageSize);
        SQLiteDatabase db = appDatabase.getConnection();
        Cursor c = db.rawQuery("SELECT id, name, description, price, quantity, image FROM Product ORDER BY price DESC LIMIT ? OFFSET ?",
                new String[]{String.valueOf(pageSize), String.valueOf(offset)});
        try {
            List<Product> list = new ArrayList<>();
            while (c.moveToNext()) {
                list.add(mapRow(c));
            }
            return list;
        } finally {
            c.close();
        }
    }

    public List<Product> findPageOrderedByNameAsc(int page, int pageSize) {
        int offset = Math.max(0, (page - 1) * pageSize);
        SQLiteDatabase db = appDatabase.getConnection();
        Cursor c = db.rawQuery("SELECT id, name, description, price, quantity, image FROM Product ORDER BY name ASC LIMIT ? OFFSET ?",
                new String[]{String.valueOf(pageSize), String.valueOf(offset)});
        try {
            List<Product> list = new ArrayList<>();
            while (c.moveToNext()) {
                list.add(mapRow(c));
            }
            return list;
        } finally {
            c.close();
        }
    }

    public List<Product> findRandomProducts(int limit) {
        SQLiteDatabase db = appDatabase.getConnection();
        Cursor c = db.rawQuery("SELECT id, name, description, price, quantity, image FROM Product ORDER BY RANDOM() LIMIT ?",
                new String[]{String.valueOf(limit)});
        try {
            List<Product> list = new ArrayList<>();
            while (c.moveToNext()) {
                list.add(mapRow(c));
            }
            return list;
        } finally {
            c.close();
        }
    }

    private static Product mapRow(Cursor c) {
        Product p = new Product();
        p.setId(c.getLong(0));
        p.setName(c.getString(1));
        p.setDescription(c.getString(2));
        p.setPrice(c.getDouble(3));
        p.setQuantity(c.getInt(4));
        p.setImage(c.getString(5));
        return p;
    }
}


