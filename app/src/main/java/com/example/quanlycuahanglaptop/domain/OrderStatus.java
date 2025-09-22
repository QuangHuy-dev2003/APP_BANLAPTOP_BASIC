package com.example.quanlycuahanglaptop.domain;

/**
 * Enum định nghĩa các trạng thái đơn hàng
 */
public enum OrderStatus {
    RECEIVED("Đã Tiếp Nhận"),
    SHIPPING("Đang Vận Chuyển"), 
    DELIVERED("Đã Giao"),
    CANCELLED("Đã Hủy");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Chuyển đổi từ string sang enum
     */
    public static OrderStatus fromString(String status) {
        if (status == null) return RECEIVED;
        
        switch (status.toUpperCase()) {
            case "RECEIVED":
                return RECEIVED;
            case "SHIPPING":
                return SHIPPING;
            case "DELIVERED":
                return DELIVERED;
            case "CANCELLED":
                return CANCELLED;
            default:
                return RECEIVED;
        }
    }

    /**
     * Chuyển đổi từ enum sang string
     */
    public String toString() {
        return this.name();
    }
}
