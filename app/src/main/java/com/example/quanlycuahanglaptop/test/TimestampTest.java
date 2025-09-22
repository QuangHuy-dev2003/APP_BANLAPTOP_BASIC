package com.example.quanlycuahanglaptop.test;

import com.example.quanlycuahanglaptop.util.TimeUtils;

/**
 * Test class để kiểm tra xử lý timestamp
 */
public class TimestampTest {
    
    public static void main(String[] args) {
        System.out.println("=== TEST TIMESTAMP UTILS ===");
        
        // Test 1: Milliseconds format (cũ)
        String millisTimestamp = "1703123456789";
        System.out.println("\n1. Milliseconds format: " + millisTimestamp);
        System.out.println("   Valid: " + TimeUtils.isValidTimestamp(millisTimestamp));
        System.out.println("   Date: " + TimeUtils.formatDate(millisTimestamp));
        System.out.println("   DateTime: " + TimeUtils.formatDateTime(millisTimestamp));
        
        // Test 2: SQLite datetime format (mới)
        String sqliteTimestamp = "2023-12-21 10:30:56";
        System.out.println("\n2. SQLite datetime format: " + sqliteTimestamp);
        System.out.println("   Valid: " + TimeUtils.isValidTimestamp(sqliteTimestamp));
        System.out.println("   Date: " + TimeUtils.formatDate(sqliteTimestamp));
        System.out.println("   DateTime: " + TimeUtils.formatDateTime(sqliteTimestamp));
        
        // Test 3: Current timestamp
        String currentTimestamp = TimeUtils.getCurrentTimestamp();
        System.out.println("\n3. Current timestamp: " + currentTimestamp);
        System.out.println("   Valid: " + TimeUtils.isValidTimestamp(currentTimestamp));
        System.out.println("   Date: " + TimeUtils.formatDate(currentTimestamp));
        System.out.println("   DateTime: " + TimeUtils.formatDateTime(currentTimestamp));
        
        // Test 4: Invalid formats
        String[] invalidTimestamps = {"invalid", "", null, "1970-01-01 00:00:00", "0"};
        System.out.println("\n4. Invalid formats:");
        for (String invalid : invalidTimestamps) {
            if (invalid != null) {
                System.out.println("   '" + invalid + "': " + TimeUtils.isValidTimestamp(invalid));
            }
        }
    }
}
