package com.example.quanlycuahanglaptop.domain;

import java.io.Serializable;

/**
 * Domain model cho đơn hàng (khớp bảng "Order").
 */
public class OrderEntity implements Serializable {
    private Long id;
    private Long userId;
    private double totalPrice;
    private String createdAt; // lưu ISO-8601 hoặc timestamp dạng TEXT

    public OrderEntity() {}

    public OrderEntity(Long id, Long userId, double totalPrice, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}


