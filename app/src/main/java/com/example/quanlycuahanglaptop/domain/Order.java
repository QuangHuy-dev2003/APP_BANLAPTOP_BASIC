package com.example.quanlycuahanglaptop.domain;

import java.io.Serializable;

/**
 * Domain model cho đơn hàng với địa chỉ giao hàng.
 */
public class Order implements Serializable {
    private Long id;
    private Long userId;
    private double totalPrice;
    private String address;
    private String phone;
    private OrderStatus status;
    private String createdAt;

    public Order() {}

    public Order(Long id, Long userId, double totalPrice, String address, String phone, OrderStatus status, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.address = address;
        this.phone = phone;
        this.status = status;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
