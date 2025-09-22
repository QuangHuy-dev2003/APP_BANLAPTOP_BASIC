package com.example.quanlycuahanglaptop.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlycuahanglaptop.data.AppDatabase;
import com.example.quanlycuahanglaptop.domain.Order;
import com.example.quanlycuahanglaptop.domain.OrderItem;
import com.example.quanlycuahanglaptop.domain.OrderStatus;
import com.example.quanlycuahanglaptop.domain.Product;
import com.example.quanlycuahanglaptop.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Service xử lý logic đặt hàng và quản lý đơn hàng.
 */
public class OrderService {
    private final AppDatabase appDatabase;

    public OrderService(Context context) {
        this.appDatabase = AppDatabase.getInstance(context);
    }

    /**
     * Tạo đơn hàng mới
     */
    public long createOrder(Order order) {
        SQLiteDatabase db = appDatabase.getConnection();
        ContentValues values = new ContentValues();
        values.put("user_id", order.getUserId());
        values.put("total_price", order.getTotalPrice());
        values.put("address", order.getAddress());
        values.put("phone", order.getPhone());
        values.put("status", order.getStatus() != null ? order.getStatus().toString() : OrderStatus.RECEIVED.toString());
        
        // Lưu thời gian tạo đơn hàng
        String vietnamTime = getCurrentVietnamTime();
        values.put("created_at", vietnamTime);
        
        return db.insert("\"Order\"", null, values);
    }
    
    /**
     * Lấy thời gian hiện tại theo định dạng database
     * Format: yyyy-MM-dd HH:mm:ss
     */
    private String getCurrentVietnamTime() {
        // Lấy thời gian hiện tại (đã đúng giờ Việt Nam)
        Date now = new Date();
        
        // Format theo định dạng database
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dbFormat.format(now);
    }

    /**
     * Tạo các item trong đơn hàng
     */
    public void createOrderItems(List<OrderItem> orderItems) {
        SQLiteDatabase db = appDatabase.getConnection();
        
        for (OrderItem item : orderItems) {
            ContentValues values = new ContentValues();
            values.put("order_id", item.getOrderId());
            values.put("product_id", item.getProductId());
            values.put("quantity", item.getQuantity());
            values.put("price", item.getPrice());
            
            db.insert("OrderItem", null, values);
        }
    }

    /**
     * Lấy danh sách đơn hàng của user
     */
    public List<Order> getOrdersByUserId(long userId) {
        SQLiteDatabase db = appDatabase.getConnection();
        String query = "SELECT * FROM \"Order\" WHERE user_id = ? ORDER BY created_at DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        List<Order> orders = new java.util.ArrayList<>();
        
        try {
            while (cursor.moveToNext()) {
                Order order = new Order();
                order.setId(cursor.getLong(0));
                order.setUserId(cursor.getLong(1));
                order.setTotalPrice(cursor.getDouble(2));
                order.setCreatedAt(cursor.getString(3)); // created_at ở index 3
                order.setAddress(cursor.getString(4));   // address ở index 4
                order.setPhone(cursor.getString(5));     // phone ở index 5
                order.setStatus(OrderStatus.fromString(cursor.getString(6))); // status ở index 6
                orders.add(order);
            }
        } finally {
            cursor.close();
        }
        
        return orders;
    }

    /**
     * Lấy N đơn hàng mới nhất (mọi user) để hiển thị nhanh.
     */
    public List<Order> getLatestOrders(int limit) {
        SQLiteDatabase db = appDatabase.getConnection();
        String query = "SELECT * FROM \"Order\" ORDER BY created_at DESC LIMIT ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(limit)});
        List<Order> orders = new java.util.ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                Order order = new Order();
                order.setId(cursor.getLong(0));
                order.setUserId(cursor.getLong(1));
                order.setTotalPrice(cursor.getDouble(2));
                order.setCreatedAt(cursor.getString(3));
                order.setAddress(cursor.getString(4));
                order.setPhone(cursor.getString(5));
                order.setStatus(OrderStatus.fromString(cursor.getString(6)));
                orders.add(order);
            }
        } finally {
            cursor.close();
        }
        return orders;
    }

    /**
     * Lấy chi tiết đơn hàng với các item
     */
    public List<OrderItem> getOrderItemsByOrderId(long orderId) {
        SQLiteDatabase db = appDatabase.getConnection();
        String query = "SELECT * FROM OrderItem WHERE order_id = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});
        List<OrderItem> orderItems = new java.util.ArrayList<>();
        
        try {
            while (cursor.moveToNext()) {
                OrderItem item = new OrderItem();
                item.setId(cursor.getLong(0));
                item.setOrderId(cursor.getLong(1));
                item.setProductId(cursor.getLong(2));
                item.setQuantity(cursor.getInt(3));
                item.setPrice(cursor.getDouble(4));
                orderItems.add(item);
            }
        } finally {
            cursor.close();
        }
        
        return orderItems;
    }

    /**
     * Lấy danh sách đơn hàng của user theo trạng thái
     */
    public List<Order> getOrdersByUserIdAndStatus(long userId, OrderStatus status) {
        SQLiteDatabase db = appDatabase.getConnection();
        String query = "SELECT * FROM \"Order\" WHERE user_id = ? AND status = ? ORDER BY created_at DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), status.toString()});
        List<Order> orders = new java.util.ArrayList<>();
        
        try {
            while (cursor.moveToNext()) {
                Order order = new Order();
                order.setId(cursor.getLong(0));
                order.setUserId(cursor.getLong(1));
                order.setTotalPrice(cursor.getDouble(2));
                order.setCreatedAt(cursor.getString(3)); // created_at ở index 3
                order.setAddress(cursor.getString(4));   // address ở index 4
                order.setPhone(cursor.getString(5));     // phone ở index 5
                order.setStatus(OrderStatus.fromString(cursor.getString(6))); // status ở index 6
                orders.add(order);
            }
        } finally {
            cursor.close();
        }
        
        return orders;
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    public boolean updateOrderStatus(long orderId, OrderStatus newStatus) {
        SQLiteDatabase db = appDatabase.getConnection();
        ContentValues values = new ContentValues();
        values.put("status", newStatus.toString());
        
        int rowsAffected = db.update("\"Order\"", values, "id = ?", new String[]{String.valueOf(orderId)});
        return rowsAffected > 0;
    }

    /**
     * Cập nhật toàn bộ thông tin đơn hàng
     */
    public boolean updateOrder(Order order) {
        SQLiteDatabase db = appDatabase.getConnection();
        ContentValues values = new ContentValues();
        values.put("user_id", order.getUserId());
        values.put("total_price", order.getTotalPrice());
        values.put("address", order.getAddress());
        values.put("phone", order.getPhone());
        values.put("status", order.getStatus() != null ? order.getStatus().toString() : OrderStatus.RECEIVED.toString());
        // Không cập nhật created_at vì đây là thời gian tạo ban đầu
        
        int rowsAffected = db.update("\"Order\"", values, "id = ?", new String[]{String.valueOf(order.getId())});
        return rowsAffected > 0;
    }

    /**
     * Lấy đơn hàng theo ID
     */
    public Order getOrderById(long orderId) {
        SQLiteDatabase db = appDatabase.getConnection();
        String query = "SELECT * FROM \"Order\" WHERE id = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});
        Order order = null;
        
        try {
            if (cursor.moveToFirst()) {
                order = new Order();
                order.setId(cursor.getLong(0));
                order.setUserId(cursor.getLong(1));
                order.setTotalPrice(cursor.getDouble(2));
                order.setCreatedAt(cursor.getString(3)); // created_at ở index 3
                order.setAddress(cursor.getString(4));   // address ở index 4
                order.setPhone(cursor.getString(5));     // phone ở index 5
                order.setStatus(OrderStatus.fromString(cursor.getString(6))); // status ở index 6
                
            }
        } finally {
            cursor.close();
        }
        
        return order;
    }

    /**
     * ĐẾM tổng số đơn hàng (toàn bộ)
     */
    public int countAll() {
        SQLiteDatabase db = appDatabase.getConnection();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM \"Order\"", null);
        try {
            if (c.moveToFirst()) return c.getInt(0);
            return 0;
        } finally {
            c.close();
        }
    }

    /**
     * Đếm theo keyword đơn giản: khớp id (chuỗi) hoặc phone
     */
    public int countByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return countAll();
        String key = "%" + keyword.trim() + "%";
        SQLiteDatabase db = appDatabase.getConnection();
        String sql = "SELECT COUNT(*) FROM \"Order\" WHERE CAST(id AS TEXT) LIKE ? OR LOWER(phone) LIKE LOWER(?)";
        Cursor c = db.rawQuery(sql, new String[]{key, key});
        try {
            if (c.moveToFirst()) return c.getInt(0);
            return 0;
        } finally {
            c.close();
        }
    }

    public int countByKeywordAndStatus(String keyword, com.example.quanlycuahanglaptop.domain.OrderStatus status) {
        SQLiteDatabase db = appDatabase.getConnection();
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus = status != null;
        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM \"Order\" WHERE 1=1");
        java.util.ArrayList<String> args = new java.util.ArrayList<>();
        if (hasKeyword) {
            sb.append(" AND (CAST(id AS TEXT) LIKE ? OR LOWER(phone) LIKE LOWER(?))");
            String key = "%" + keyword.trim() + "%";
            args.add(key); args.add(key);
        }
        if (hasStatus) {
            sb.append(" AND status = ?");
            args.add(status.toString());
        }
        Cursor c = db.rawQuery(sb.toString(), args.toArray(new String[0]));
        try {
            if (c.moveToFirst()) return c.getInt(0);
            return 0;
        } finally { c.close(); }
    }

    /**
     * Phân trang toàn bộ đơn hàng (mới nhất trước)
     */
    public List<Order> findPage(int page, int pageSize) {
        int offset = Math.max(0, (page - 1) * pageSize);
        SQLiteDatabase db = appDatabase.getConnection();
        String sql = "SELECT * FROM \"Order\" ORDER BY created_at DESC LIMIT ? OFFSET ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(pageSize), String.valueOf(offset)});
        List<Order> orders = new java.util.ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                Order order = new Order();
                order.setId(cursor.getLong(0));
                order.setUserId(cursor.getLong(1));
                order.setTotalPrice(cursor.getDouble(2));
                order.setCreatedAt(cursor.getString(3));
                order.setAddress(cursor.getString(4));
                order.setPhone(cursor.getString(5));
                order.setStatus(com.example.quanlycuahanglaptop.domain.OrderStatus.fromString(cursor.getString(6)));
                orders.add(order);
            }
        } finally {
            cursor.close();
        }
        return orders;
    }

    /**
     * Tìm kiếm theo keyword (id/phone) kèm phân trang
     */
    public List<Order> searchByKeyword(String keyword, int page, int pageSize) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findPage(page, pageSize);
        }
        int offset = Math.max(0, (page - 1) * pageSize);
        String key = "%" + keyword.trim() + "%";
        SQLiteDatabase db = appDatabase.getConnection();
        String sql = "SELECT * FROM \"Order\" WHERE CAST(id AS TEXT) LIKE ? OR LOWER(phone) LIKE LOWER(?) ORDER BY created_at DESC LIMIT ? OFFSET ?";
        Cursor cursor = db.rawQuery(sql, new String[]{key, key, String.valueOf(pageSize), String.valueOf(offset)});
        List<Order> orders = new java.util.ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                Order order = new Order();
                order.setId(cursor.getLong(0));
                order.setUserId(cursor.getLong(1));
                order.setTotalPrice(cursor.getDouble(2));
                order.setCreatedAt(cursor.getString(3));
                order.setAddress(cursor.getString(4));
                order.setPhone(cursor.getString(5));
                order.setStatus(com.example.quanlycuahanglaptop.domain.OrderStatus.fromString(cursor.getString(6)));
                orders.add(order);
            }
        } finally {
            cursor.close();
        }
        return orders;
    }

    public List<Order> searchByKeywordAndStatus(String keyword, com.example.quanlycuahanglaptop.domain.OrderStatus status, int page, int pageSize) {
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus = status != null;
        if (!hasKeyword && !hasStatus) return findPage(page, pageSize);
        int offset = Math.max(0, (page - 1) * pageSize);
        StringBuilder sb = new StringBuilder("SELECT * FROM \"Order\" WHERE 1=1");
        java.util.ArrayList<String> args = new java.util.ArrayList<>();
        if (hasKeyword) {
            sb.append(" AND (CAST(id AS TEXT) LIKE ? OR LOWER(phone) LIKE LOWER(?))");
            String key = "%" + keyword.trim() + "%";
            args.add(key); args.add(key);
        }
        if (hasStatus) {
            sb.append(" AND status = ?");
            args.add(status.toString());
        }
        sb.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        args.add(String.valueOf(pageSize));
        args.add(String.valueOf(offset));
        SQLiteDatabase db = appDatabase.getConnection();
        Cursor cursor = db.rawQuery(sb.toString(), args.toArray(new String[0]));
        List<Order> orders = new java.util.ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                Order order = new Order();
                order.setId(cursor.getLong(0));
                order.setUserId(cursor.getLong(1));
                order.setTotalPrice(cursor.getDouble(2));
                order.setCreatedAt(cursor.getString(3));
                order.setAddress(cursor.getString(4));
                order.setPhone(cursor.getString(5));
                order.setStatus(com.example.quanlycuahanglaptop.domain.OrderStatus.fromString(cursor.getString(6)));
                orders.add(order);
            }
        } finally { cursor.close(); }
        return orders;
    }

    /**
     * Doanh thu dự kiến trong năm: tổng total_price các đơn KHÔNG bị huỷ.
     */
    public double getProjectedRevenueByYear(int year) {
        SQLiteDatabase db = appDatabase.getConnection();
        String sql = "SELECT IFNULL(SUM(total_price), 0) FROM \"Order\" " +
                "WHERE ( (length(created_at)=19 AND substr(created_at,1,4)=?) " +
                "OR (length(created_at)>10 AND strftime('%Y', datetime(CAST(created_at AS INTEGER)/1000,'unixepoch')) = ?) ) " +
                "AND status <> ?";
        String y = String.valueOf(year);
        Cursor c = db.rawQuery(sql, new String[]{y, y, OrderStatus.CANCELLED.toString()});
        try { if (c.moveToFirst()) return c.getDouble(0); } finally { c.close(); }
        return 0d;
    }

    /**
     * Doanh thu thực trong năm: tổng total_price các đơn ĐÃ GIAO.
     */
    public double getRealRevenueByYear(int year) {
        SQLiteDatabase db = appDatabase.getConnection();
        String sql = "SELECT IFNULL(SUM(total_price), 0) FROM \"Order\" " +
                "WHERE ( (length(created_at)=19 AND substr(created_at,1,4)=?) " +
                "OR (length(created_at)>10 AND strftime('%Y', datetime(CAST(created_at AS INTEGER)/1000,'unixepoch')) = ?) ) " +
                "AND status = ?";
        String y = String.valueOf(year);
        Cursor c = db.rawQuery(sql, new String[]{y, y, OrderStatus.DELIVERED.toString()});
        try { if (c.moveToFirst()) return c.getDouble(0); } finally { c.close(); }
        return 0d;
    }

    /**
     * Đếm số đơn theo nhóm trong năm: Delivered, Cancelled, Other.
     */
    public CountByStatus countOrdersByYearGrouped(int year) {
        SQLiteDatabase db = appDatabase.getConnection();
        CountByStatus result = new CountByStatus();
        String y = String.valueOf(year);
        String yearFilter = "( (length(created_at)=19 AND substr(created_at,1,4)=?) OR (length(created_at)>10 AND strftime('%Y', datetime(CAST(created_at AS INTEGER)/1000,'unixepoch')) = ?) )";
        String deliveredSql = "SELECT COUNT(*) FROM \"Order\" WHERE " + yearFilter + " AND status = ?";
        String cancelledSql = "SELECT COUNT(*) FROM \"Order\" WHERE " + yearFilter + " AND status = ?";
        String otherSql = "SELECT COUNT(*) FROM \"Order\" WHERE " + yearFilter + " AND status NOT IN (?, ?)";
        Cursor c1 = db.rawQuery(deliveredSql, new String[]{y, y, OrderStatus.DELIVERED.toString()});
        try { if (c1.moveToFirst()) result.delivered = c1.getInt(0); } finally { c1.close(); }
        Cursor c2 = db.rawQuery(cancelledSql, new String[]{y, y, OrderStatus.CANCELLED.toString()});
        try { if (c2.moveToFirst()) result.cancelled = c2.getInt(0); } finally { c2.close(); }
        Cursor c3 = db.rawQuery(otherSql, new String[]{y, y, OrderStatus.DELIVERED.toString(), OrderStatus.CANCELLED.toString()});
        try { if (c3.moveToFirst()) result.other = c3.getInt(0); } finally { c3.close(); }
        return result;
    }

    /**
     * Top 3 sản phẩm bán chạy theo số lượng trong năm (chỉ tính đơn ĐÃ GIAO).
     */
    public List<TopProductStat> getTop3ProductsByYear(int year) {
        SQLiteDatabase db = appDatabase.getConnection();
        String sql = "SELECT oi.product_id, IFNULL(p.name, 'SP ' || oi.product_id) AS name, SUM(oi.quantity) AS qty " +
                "FROM OrderItem oi " +
                "JOIN \"Order\" o ON oi.order_id = o.id " +
                "LEFT JOIN Product p ON oi.product_id = p.id " +
                "WHERE ( (length(o.created_at)=19 AND substr(o.created_at,1,4)=?) OR (length(o.created_at)>10 AND strftime('%Y', datetime(CAST(o.created_at AS INTEGER)/1000,'unixepoch')) = ?) ) " +
                "AND o.status = ? " +
                "GROUP BY oi.product_id, name " +
                "ORDER BY qty DESC LIMIT 3";
        String y = String.valueOf(year);
        Cursor cursor = db.rawQuery(sql, new String[]{y, y, OrderStatus.DELIVERED.toString()});
        List<TopProductStat> list = new java.util.ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                TopProductStat s = new TopProductStat();
                s.productId = cursor.getLong(0);
                s.productName = cursor.getString(1);
                s.totalQuantity = cursor.getInt(2);
                list.add(s);
            }
        } finally { cursor.close(); }
        return list;
    }

    /**
     * Doanh thu theo tháng (1..12) trong năm cho đơn ĐÃ GIAO.
     * Trả về mảng 12 phần tử (index 0 -> tháng 1).
     */
    public float[] getMonthlyRevenueRealByYear(int year) {
        SQLiteDatabase db = appDatabase.getConnection();
        float[] months = new float[12];
        String sql = "SELECT " +
                "CASE WHEN length(created_at)=19 THEN CAST(substr(created_at,6,2) AS INTEGER) " +
                "ELSE CAST(strftime('%m', datetime(CAST(created_at AS INTEGER)/1000,'unixepoch')) AS INTEGER) END AS m, " +
                "IFNULL(SUM(total_price),0) AS rev " +
                "FROM \"Order\" " +
                "WHERE ( (length(created_at)=19 AND substr(created_at,1,4)=?) OR (length(created_at)>10 AND strftime('%Y', datetime(CAST(created_at AS INTEGER)/1000,'unixepoch')) = ?) ) " +
                "AND status = ? GROUP BY m";
        String y = String.valueOf(year);
        Cursor c = db.rawQuery(sql, new String[]{y, y, OrderStatus.DELIVERED.toString()});
        try {
            while (c.moveToNext()) {
                int m = c.getInt(0); // 1..12
                float rev = (float) c.getDouble(1);
                if (m >= 1 && m <= 12) months[m - 1] = rev;
            }
        } finally { c.close(); }
        return months;
    }

    public static class CountByStatus {
        public int delivered;
        public int cancelled;
        public int other;
    }

    public static class TopProductStat {
        public long productId;
        public String productName;
        public int totalQuantity;
    }
}
