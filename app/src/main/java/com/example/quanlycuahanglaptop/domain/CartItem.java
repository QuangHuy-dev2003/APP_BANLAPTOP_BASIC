package com.example.quanlycuahanglaptop.domain;

import java.io.Serializable;

/**
 * Domain model cho sản phẩm trong giỏ hàng (khớp bảng CartItem).
 */
public class CartItem implements Serializable {
    private Long id;
    private Long userId;
    private Long productId;
    private int quantity;
    private String addedAt; // TEXT DEFAULT CURRENT_TIMESTAMP

    public CartItem() {}

    public CartItem(Long id, Long userId, Long productId, int quantity, String addedAt) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.addedAt = addedAt;
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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(String addedAt) {
        this.addedAt = addedAt;
    }
}


