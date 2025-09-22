package com.example.quanlycuahanglaptop.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlycuahanglaptop.data.AppDatabase;
import com.example.quanlycuahanglaptop.domain.CartItem;
import com.example.quanlycuahanglaptop.domain.Product;

import java.util.ArrayList;
import java.util.List;

public class CartItemRepository {

    private final AppDatabase appDatabase;

    public CartItemRepository(Context context) {
        this.appDatabase = AppDatabase.getInstance(context);
    }

    public int deleteByUserId(long userId) {
        SQLiteDatabase db = appDatabase.getConnection();
        return db.delete("CartItem", "user_id = ?", new String[]{String.valueOf(userId)});
    }

    public int countItemsByUserId(long userId) {
        SQLiteDatabase db = appDatabase.getConnection();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM CartItem WHERE user_id = ?", new String[]{String.valueOf(userId)});
        try {
            if (c.moveToFirst()) return c.getInt(0);
            return 0;
        } finally {
            c.close();
        }
    }

    /** Thêm vào giỏ: nếu đã tồn tại thì tăng số lượng, nếu chưa thì tạo mới. */
    public void addOrIncreaseQuantity(long userId, long productId, int quantity) {
        if (quantity < 1) quantity = 1;
        SQLiteDatabase db = appDatabase.getConnection();
        Cursor c = db.rawQuery(
                "SELECT id, quantity FROM CartItem WHERE user_id = ? AND product_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(productId)}
        );
        try {
            if (c.moveToFirst()) {
                long id = c.getLong(0);
                int current = c.getInt(1);
                android.content.ContentValues values = new android.content.ContentValues();
                values.put("quantity", current + quantity);
                db.update("CartItem", values, "id = ?", new String[]{String.valueOf(id)});
            } else {
                android.content.ContentValues values = new android.content.ContentValues();
                values.put("user_id", userId);
                values.put("product_id", productId);
                values.put("quantity", quantity);
                db.insert("CartItem", null, values);
            }
        } finally {
            c.close();
        }
    }

    /** Lấy danh sách sản phẩm trong giỏ hàng với thông tin sản phẩm */
    public List<CartItemWithProduct> getCartItemsWithProducts(long userId) {
        SQLiteDatabase db = appDatabase.getConnection();
        String query = "SELECT " +
                "ci.id, ci.user_id, ci.product_id, ci.quantity, ci.added_at, " +
                "p.name, p.description, p.price, p.quantity as stock_quantity, p.image " +
                "FROM CartItem ci " +
                "JOIN Product p ON ci.product_id = p.id " +
                "WHERE ci.user_id = ? " +
                "ORDER BY ci.added_at DESC";
        
        Cursor c = db.rawQuery(query, new String[]{String.valueOf(userId)});
        List<CartItemWithProduct> items = new ArrayList<>();
        
        try {
            while (c.moveToNext()) {
                CartItem cartItem = new CartItem(
                    c.getLong(0), // id
                    c.getLong(1), // user_id
                    c.getLong(2), // product_id
                    c.getInt(3),  // quantity
                    c.getString(4) // added_at
                );
                
                Product product = new Product(
                    c.getLong(2), // product_id
                    c.getString(5), // name
                    c.getString(6), // description
                    c.getDouble(7), // price
                    c.getInt(8), // stock_quantity
                    c.getString(9) // image
                );
                
                items.add(new CartItemWithProduct(cartItem, product));
            }
        } finally {
            c.close();
        }
        
        return items;
    }

    /** Cập nhật số lượng sản phẩm trong giỏ hàng */
    public boolean updateQuantity(long cartItemId, int newQuantity) {
        if (newQuantity < 1) return false;
        SQLiteDatabase db = appDatabase.getConnection();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put("quantity", newQuantity);
        int rows = db.update("CartItem", values, "id = ?", new String[]{String.valueOf(cartItemId)});
        return rows > 0;
    }

    /** Xóa một sản phẩm khỏi giỏ hàng */
    public boolean deleteCartItem(long cartItemId) {
        SQLiteDatabase db = appDatabase.getConnection();
        int rows = db.delete("CartItem", "id = ?", new String[]{String.valueOf(cartItemId)});
        return rows > 0;
    }

    /** Xóa tất cả sản phẩm khỏi giỏ hàng của user */
    public int deleteAllByUserId(long userId) {
        SQLiteDatabase db = appDatabase.getConnection();
        return db.delete("CartItem", "user_id = ?", new String[]{String.valueOf(userId)});
    }

    /** Lớp wrapper để chứa CartItem và Product */
    public static class CartItemWithProduct {
        private final CartItem cartItem;
        private final Product product;

        public CartItemWithProduct(CartItem cartItem, Product product) {
            this.cartItem = cartItem;
            this.product = product;
        }

        public CartItem getCartItem() {
            return cartItem;
        }

        public Product getProduct() {
            return product;
        }
    }
}


