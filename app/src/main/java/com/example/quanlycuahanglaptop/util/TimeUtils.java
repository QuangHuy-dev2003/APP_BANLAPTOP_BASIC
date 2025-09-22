package com.example.quanlycuahanglaptop.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility class để xử lý thời gian
 */
public class TimeUtils {
    
    // Múi giờ Việt Nam (UTC+7)
    private static final TimeZone VIETNAM_TIMEZONE = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    
    static {
        // Set múi giờ Việt Nam cho tất cả formatter
        DATE_FORMAT.setTimeZone(VIETNAM_TIMEZONE);
        DATETIME_FORMAT.setTimeZone(VIETNAM_TIMEZONE);
    }
    
    /**
     * Lấy timestamp hiện tại dưới dạng string
     */
    public static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }
    
    /**
     * Chuyển đổi timestamp string thành Date object
     * Hỗ trợ cả 2 định dạng: milliseconds (1703123456789) và SQLite datetime (2023-12-21 10:30:56)
     * Tự động xử lý múi giờ Việt Nam
     */
    public static Date parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return null;
        }
        
        try {
            // Thử parse như milliseconds trước
            long time = Long.parseLong(timestamp);
            return new Date(time);
        } catch (NumberFormatException e) {
            // Nếu không phải milliseconds, thử parse như SQLite datetime
            try {
                // SQLite datetime format: "YYYY-MM-DD HH:MM:SS" 
                // Database lưu thời gian chậm hơn 7 tiếng so với giờ Việt Nam
                SimpleDateFormat sqliteFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                sqliteFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Parse như UTC
                Date utcDate = sqliteFormat.parse(timestamp);
                
                // Chuyển đổi từ UTC sang múi giờ Việt Nam (+7 giờ)
                if (utcDate != null) {
                    long vietnamTime = utcDate.getTime() + (7 * 60 * 60 * 1000); // +7 giờ
                    return new Date(vietnamTime);
                }
                return null;
            } catch (Exception ex) {
                return null;
            }
        }
    }
    
    /**
     * Format timestamp thành chuỗi ngày tháng
     */
    public static String formatDate(String timestamp) {
        Date date = parseTimestamp(timestamp);
        if (date != null) {
            return DATE_FORMAT.format(date);
        }
        return "Không xác định";
    }
    
    /**
     * Format timestamp thành chuỗi ngày giờ
     */
    public static String formatDateTime(String timestamp) {
        Date date = parseTimestamp(timestamp);
        if (date != null) {
            return DATETIME_FORMAT.format(date);
        }
        return "Không xác định";
    }
    
    /**
     * Format thời gian từ database thành chuỗi ngày giờ Việt Nam
     * Database bây giờ đã lưu đúng thời gian Việt Nam rồi, chỉ cần format hiển thị
     */
    public static String formatDatabaseTimeToVietnam(String databaseTime) {
        if (databaseTime == null || databaseTime.isEmpty()) {
            return "Chưa có ngày";
        }
        
        try {
            // Parse thời gian từ database (format: 2025-09-22 05:02:48) - đã là giờ Việt Nam
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date dbDate = dbFormat.parse(databaseTime);
            
            if (dbDate != null) {
                // Format theo yêu cầu: 2025-09-22 12:28:38 (giữ nguyên format từ database)
                return databaseTime;
            }
        } catch (Exception e) {
            // Nếu không parse được, thử các format khác
            try {
                // Thử với format có milliseconds
                SimpleDateFormat dbFormatMs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
                Date dbDateMs = dbFormatMs.parse(databaseTime);
                if (dbDateMs != null) {
                    // Nếu có milliseconds, format lại thành format chuẩn
                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    return outputFormat.format(dbDateMs);
                }
            } catch (Exception ex) {
                // Nếu tất cả đều thất bại, trả về chuỗi gốc
                return databaseTime;
            }
        }
        
        return "Chưa có ngày";
    }
    
    /**
     * Kiểm tra timestamp có hợp lệ không
     * Hỗ trợ cả 2 định dạng: milliseconds và SQLite datetime
     */
    public static boolean isValidTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return false;
        }
        
        try {
            // Thử parse như milliseconds
            long time = Long.parseLong(timestamp);
            // Kiểm tra timestamp có trong khoảng hợp lý không (từ 2020 đến 2030)
            return time >= 1577836800000L && time <= 1893456000000L;
        } catch (NumberFormatException e) {
            // Nếu không phải milliseconds, thử parse như SQLite datetime
            try {
                SimpleDateFormat sqliteFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = sqliteFormat.parse(timestamp);
                // Kiểm tra date có trong khoảng hợp lý không
                return date != null && date.getTime() >= 1577836800000L && date.getTime() <= 1893456000000L;
            } catch (Exception ex) {
                return false;
            }
        }
    }
    
    /**
     * Kiểm tra đơn hàng có thể hủy không (trong vòng 24 giờ)
     */
    public static boolean canCancelOrder(String orderTimestamp) {
        if (!isValidTimestamp(orderTimestamp)) {
            return false;
        }
        
        Date orderDate = parseTimestamp(orderTimestamp);
        if (orderDate == null) {
            return false;
        }
        
        // Thời gian hiện tại
        Date now = new Date();
        
        // Tính thời gian chênh lệch (milliseconds)
        long timeDiff = now.getTime() - orderDate.getTime();
        
        // 24 giờ = 24 * 60 * 60 * 1000 milliseconds
        long twentyFourHours = 24 * 60 * 60 * 1000;
        
        return timeDiff <= twentyFourHours;
    }
    
    /**
     * Lấy thời gian còn lại có thể hủy đơn hàng (giờ)
     */
    public static int getRemainingCancelHours(String orderTimestamp) {
        if (!isValidTimestamp(orderTimestamp)) {
            return 0;
        }
        
        Date orderDate = parseTimestamp(orderTimestamp);
        if (orderDate == null) {
            return 0;
        }
        
        // Thời gian hiện tại
        Date now = new Date();
        
        // Tính thời gian chênh lệch (milliseconds)
        long timeDiff = now.getTime() - orderDate.getTime();
        
        // 24 giờ = 24 * 60 * 60 * 1000 milliseconds
        long twentyFourHours = 24 * 60 * 60 * 1000;
        
        if (timeDiff >= twentyFourHours) {
            return 0; // Đã hết thời gian hủy
        }
        
        // Tính số giờ còn lại
        long remainingTime = twentyFourHours - timeDiff;
        return (int) (remainingTime / (60 * 60 * 1000)); // Chuyển đổi từ milliseconds sang giờ
    }
    
    /**
     * Kiểm tra đơn hàng có thể hủy không (trong vòng 30 phút) - với thời gian từ database
     * Database đã lưu đúng thời gian Việt Nam
     */
    public static boolean canCancelOrderFromDatabase(String databaseTime) {
        if (databaseTime == null || databaseTime.isEmpty()) {
            return false;
        }
        
        try {
            // Parse thời gian từ database (format: 2025-09-22 05:02:48) - đã là giờ Việt Nam
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date orderDate = dbFormat.parse(databaseTime);
            
            if (orderDate != null) {
                // Thời gian hiện tại (đã đúng giờ Việt Nam)
                Date now = new Date();
                
                // Tính thời gian chênh lệch (milliseconds)
                long timeDiff = now.getTime() - orderDate.getTime();
                
                // 30 phút = 30 * 60 * 1000 milliseconds
                long thirtyMinutes = 30 * 60 * 1000;
                
                return timeDiff <= thirtyMinutes;
            }
        } catch (Exception e) {
            return false;
        }
        
        return false;
    }
    
    /**
     * Lấy thời gian còn lại có thể hủy đơn hàng (phút) - với thời gian từ database
     * Database đã lưu đúng thời gian Việt Nam
     */
    public static int getRemainingCancelMinutesFromDatabase(String databaseTime) {
        if (databaseTime == null || databaseTime.isEmpty()) {
            return 0;
        }
        
        try {
            // Parse thời gian từ database (format: 2025-09-22 05:02:48) - đã là giờ Việt Nam
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date orderDate = dbFormat.parse(databaseTime);
            
            if (orderDate != null) {
                // Thời gian hiện tại (đã đúng giờ Việt Nam)
                Date now = new Date();
                
                // Tính thời gian chênh lệch (milliseconds)
                long timeDiff = now.getTime() - orderDate.getTime();
                
                // 30 phút = 30 * 60 * 1000 milliseconds
                long thirtyMinutes = 30 * 60 * 1000;
                
                if (timeDiff >= thirtyMinutes) {
                    return 0; // Đã hết thời gian hủy
                }
                
                // Tính số phút còn lại
                long remainingTime = thirtyMinutes - timeDiff;
                return (int) (remainingTime / (60 * 1000)); // Chuyển đổi từ milliseconds sang phút
            }
        } catch (Exception e) {
            return 0;
        }
        
        return 0;
    }
    
}
