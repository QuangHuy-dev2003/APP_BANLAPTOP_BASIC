package com.example.quanlycuahanglaptop.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SQLiteOpenHelper quản lý cơ sở dữ liệu cục bộ của ứng dụng.
 * - Tạo các bảng theo schema được yêu cầu
 * - Bật ràng buộc khóa ngoại để đảm bảo toàn vẹn dữ liệu
 * - Cung cấp phương thức getConnection() để lấy kết nối SQLiteDatabase
 */
public final class AppDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "store.db";
    private static final int DATABASE_VERSION = 7;

    private static volatile AppDatabase instance;

    private AppDatabase(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Lấy instance theo Singleton để tái sử dụng kết nối và tránh rò rỉ context.
     */
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = new AppDatabase(context);
                }
            }
        }
        return instance;
    }

    /** Trả về đường dẫn tuyệt đối tới file CSDL, ví dụ: /data/data/<pkg>/databases/store.db */
    public static String getDatabaseAbsolutePath(Context context) {
        return context.getApplicationContext().getDatabasePath(DATABASE_NAME).getAbsolutePath();
    }

    /** Bật khóa ngoại cho SQLite. */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Bảng User
        db.execSQL("CREATE TABLE IF NOT EXISTS User (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "phone TEXT," +
                "role TEXT NOT NULL CHECK(role IN ('admin','user','ADMIN','USER'))" +
                ")");

        // Bảng Product
        db.execSQL("CREATE TABLE IF NOT EXISTS Product (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "description TEXT," +
                "price REAL NOT NULL," +
                "quantity INTEGER NOT NULL," +
                "image TEXT" +
                ")");

        // Bảng Order (sử dụng tên "Order" có trích dẫn do từ khóa SQL)
        db.execSQL("CREATE TABLE IF NOT EXISTS \"Order\" (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "total_price REAL NOT NULL," +
                "address TEXT," +
                "phone TEXT," +
                "status TEXT DEFAULT 'RECEIVED' CHECK(status IN ('RECEIVED','SHIPPING','DELIVERED','CANCELLED'))," +
                "created_at TEXT," +
                "FOREIGN KEY(user_id) REFERENCES User(id) ON DELETE SET NULL" +
                ")");

        // Bảng OrderItem
        db.execSQL("CREATE TABLE IF NOT EXISTS OrderItem (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER NOT NULL," +
                "product_id INTEGER NOT NULL," +
                "quantity INTEGER NOT NULL," +
                "price REAL NOT NULL," +
                "FOREIGN KEY(order_id) REFERENCES \"Order\"(id)," +
                "FOREIGN KEY(product_id) REFERENCES Product(id)" +
                ")");

        // Bảng CartItem
        db.execSQL("CREATE TABLE IF NOT EXISTS CartItem (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "product_id INTEGER NOT NULL," +
                "quantity INTEGER NOT NULL," +
                "added_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(user_id) REFERENCES User(id)," +
                "FOREIGN KEY(product_id) REFERENCES Product(id)" +
                ")");

        // Bảng Session: lưu phiên đăng nhập và hết hạn (expires_at, millis since epoch)
        db.execSQL("CREATE TABLE IF NOT EXISTS Session (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "token TEXT NOT NULL," +
                "created_at INTEGER NOT NULL," +
                "expires_at INTEGER NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES User(id) ON DELETE CASCADE" +
                ")");

        // Indexes để tối ưu truy vấn
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_user_email ON User(email)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_order_user_id ON \"Order\"(user_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_cartitem_user_id ON CartItem(user_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_session_user_id ON Session(user_id)");

        // Seed tài khoản ADMIN mặc định nếu chưa tồn tại (dựa trên unique email)
        final String adminName = "Admin";
        final String adminEmail = "admin@gmail.com";
        final String adminPasswordHashed = sha256("admin123");
        ContentValues values = new ContentValues();
        values.put("name", adminName);
        values.put("email", adminEmail);
        values.put("password", adminPasswordHashed);
        values.put("role", "ADMIN");
        db.insertWithOnConflict("User", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nâng cấp: v2 cho phép Order.user_id nullable + ON DELETE SET NULL và thêm indexes
        if (oldVersion < 2) {
            db.beginTransaction();
            try {
                // Đổi tên bảng Order cũ
                db.execSQL("ALTER TABLE \"Order\" RENAME TO Order_old");
                // Tạo bảng Order mới với schema mới
                db.execSQL("CREATE TABLE IF NOT EXISTS \"Order\" (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER," +
                        "total_price REAL NOT NULL," +
                        "created_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY(user_id) REFERENCES User(id) ON DELETE SET NULL" +
                        ")");
                
                db.execSQL("INSERT INTO \"Order\"(id, user_id, total_price, created_at) " +
                        "SELECT id, user_id, total_price, created_at FROM Order_old");
                // Xoá bảng cũ
                db.execSQL("DROP TABLE IF EXISTS Order_old");

                // Thêm indexes nếu chưa có
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_user_email ON User(email)");
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_order_user_id ON \"Order\"(user_id)");
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_cartitem_user_id ON CartItem(user_id)");

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        // v3: thêm bảng Session và index tương ứng
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE IF NOT EXISTS Session (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "token TEXT NOT NULL," +
                    "created_at INTEGER NOT NULL," +
                    "expires_at INTEGER NOT NULL," +
                    "FOREIGN KEY(user_id) REFERENCES User(id) ON DELETE CASCADE" +
                    ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_session_user_id ON Session(user_id)");
        }

        // v4: thêm trường address vào bảng Order
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE \"Order\" ADD COLUMN address TEXT");
        }

        // v5: thêm trường phone vào bảng User và Order
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE User ADD COLUMN phone TEXT");
            db.execSQL("ALTER TABLE \"Order\" ADD COLUMN phone TEXT");
        }

        // v6: thêm trường status vào bảng Order
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE \"Order\" ADD COLUMN status TEXT DEFAULT 'RECEIVED'");
            // Cập nhật tất cả đơn hàng hiện tại có status mặc định
            db.execSQL("UPDATE \"Order\" SET status = 'RECEIVED' WHERE status IS NULL");
        }

        // v7: loại bỏ DEFAULT CURRENT_TIMESTAMP từ created_at trong bảng Order
        if (oldVersion < 7) {
            db.beginTransaction();
            try {
                // Tạo bảng Order mới không có DEFAULT CURRENT_TIMESTAMP
                db.execSQL("CREATE TABLE IF NOT EXISTS \"Order_new\" (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER," +
                        "total_price REAL NOT NULL," +
                        "address TEXT," +
                        "phone TEXT," +
                        "status TEXT DEFAULT 'RECEIVED' CHECK(status IN ('RECEIVED','SHIPPING','DELIVERED','CANCELLED'))," +
                        "created_at TEXT," +
                        "FOREIGN KEY(user_id) REFERENCES User(id) ON DELETE SET NULL" +
                        ")");
                
                // Copy dữ liệu từ bảng cũ sang bảng mới
                db.execSQL("INSERT INTO \"Order_new\"(id, user_id, total_price, address, phone, status, created_at) " +
                        "SELECT id, user_id, total_price, address, phone, status, created_at FROM \"Order\"");
                
                // Xóa bảng cũ và đổi tên bảng mới
                db.execSQL("DROP TABLE IF EXISTS \"Order\"");
                db.execSQL("ALTER TABLE \"Order_new\" RENAME TO \"Order\"");
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    /**
     * Trả về một kết nối ghi (writable) tới cơ sở dữ liệu.
     * Dùng cho cả truy vấn đọc/ghi; nếu chỉ đọc, có thể dùng getReadableDatabase().
     */
    public SQLiteDatabase getConnection() {
        return getWritableDatabase();
    }

    /**
     * Băm SHA-256 cho mật khẩu (mã hoá nhẹ, không dùng cho sản xuất quy mô lớn).
     */
    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
           
            return input;
        }
    }
}
