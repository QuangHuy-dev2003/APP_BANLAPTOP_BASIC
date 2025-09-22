package com.example.quanlycuahanglaptop.service;

import android.content.Context;

import com.example.quanlycuahanglaptop.repository.CartItemRepository;
import com.example.quanlycuahanglaptop.domain.CartItem;
import com.example.quanlycuahanglaptop.domain.Product;

import java.util.List;

public class CartService {
    private final CartItemRepository cartItemRepository;

    public CartService(Context context) {
        this.cartItemRepository = new CartItemRepository(context);
    }

    public int countItems(long userId) {
        if (userId <= 0) return 0;
        return cartItemRepository.countItemsByUserId(userId);
    }

    /** Thêm sản phẩm vào giỏ (nếu có sẽ cộng dồn). */
    public void addToCart(long userId, long productId, int quantity) {
        if (userId <= 0) throw new IllegalArgumentException("userId invalid");
        if (productId <= 0) throw new IllegalArgumentException("productId invalid");
        if (quantity < 1) quantity = 1;
        cartItemRepository.addOrIncreaseQuantity(userId, productId, quantity);
    }

    /** Lấy danh sách sản phẩm trong giỏ hàng */
    public List<CartItemRepository.CartItemWithProduct> getCartItems(long userId) {
        if (userId <= 0) return new java.util.ArrayList<>();
        return cartItemRepository.getCartItemsWithProducts(userId);
    }

    /** Cập nhật số lượng sản phẩm trong giỏ hàng */
    public boolean updateQuantity(long cartItemId, int newQuantity) {
        if (cartItemId <= 0) return false;
        if (newQuantity < 1) return false;
        return cartItemRepository.updateQuantity(cartItemId, newQuantity);
    }

    /** Xóa một sản phẩm khỏi giỏ hàng */
    public boolean removeFromCart(long cartItemId) {
        if (cartItemId <= 0) return false;
        return cartItemRepository.deleteCartItem(cartItemId);
    }

    /** Xóa tất cả sản phẩm khỏi giỏ hàng */
    public boolean clearCart(long userId) {
        if (userId <= 0) return false;
        int deleted = cartItemRepository.deleteAllByUserId(userId);
        return deleted >= 0; // >= 0 vì có thể giỏ hàng đã trống
    }

    /** Tính tổng tiền giỏ hàng */
    public double calculateTotal(List<CartItemRepository.CartItemWithProduct> cartItems) {
        double total = 0.0;
        for (CartItemRepository.CartItemWithProduct item : cartItems) {
            total += item.getProduct().getPrice() * item.getCartItem().getQuantity();
        }
        return total;
    }
}


