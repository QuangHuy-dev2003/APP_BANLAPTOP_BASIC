package com.example.quanlycuahanglaptop.service;

import android.content.Context;

import com.example.quanlycuahanglaptop.domain.Product;
import com.example.quanlycuahanglaptop.repository.ProductRepository;

import java.util.List;

public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(Context context) {
        this.productRepository = new ProductRepository(context);
    }

    public long create(Product product) {
        validate(product);
        return productRepository.create(product);
    }

    public int update(Product product) {
        if (product.getId() == null) throw new IllegalArgumentException("Id sản phẩm không được null");
        validate(product);
        return productRepository.update(product);
    }

    public int delete(long id) {
        return productRepository.deleteById(id);
    }

    public Product findById(long id) {
        return productRepository.findById(id);
    }

    public List<Product> findPage(int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 5;
        return productRepository.findPageOrderedByIdDesc(page, pageSize);
    }

    public List<Product> findPageOrderedByPriceAsc(int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 5;
        return productRepository.findPageOrderedByPriceAsc(page, pageSize);
    }

    public List<Product> findPageOrderedByPriceDesc(int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 5;
        return productRepository.findPageOrderedByPriceDesc(page, pageSize);
    }

    public List<Product> findPageOrderedByNameAsc(int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 5;
        return productRepository.findPageOrderedByNameAsc(page, pageSize);
    }

    public List<Product> findRandomProducts(int limit) {
        if (limit < 1) limit = 1;
        return productRepository.findRandomProducts(limit);
    }

    public int countAll() {
        return productRepository.countAll();
    }

    public List<Product> searchByName(String keyword, int page, int pageSize) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findPage(page, pageSize);
        }
        return productRepository.searchByNameOrderedDesc(keyword.trim(), page, pageSize);
    }

    public int countByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return countAll();
        return productRepository.countByName(keyword.trim());
    }

    /** Giảm tồn kho cho một sản phẩm, trả về true nếu trừ thành công (đủ hàng). */
    public boolean decreaseQuantity(long productId, int quantity) {
        if (productId <= 0) throw new IllegalArgumentException("productId invalid");
        if (quantity <= 0) return true;
        return productRepository.decreaseQuantity(productId, quantity);
    }

    /**
     * Giảm tồn kho theo danh sách item. Trả về false nếu bất kỳ sản phẩm nào không đủ hàng.
     * Lưu ý: Không tự quản lý transaction tại đây; hãy gọi từ service cấp trên đã bắt đầu transaction.
     */
    public boolean decreaseStockBatch(java.util.List<com.example.quanlycuahanglaptop.domain.OrderItem> items) {
        if (items == null || items.isEmpty()) return true;
        for (com.example.quanlycuahanglaptop.domain.OrderItem it : items) {
            if (!decreaseQuantity(it.getProductId(), it.getQuantity())) {
                return false;
            }
        }
        return true;
    }

    /** Tăng kho cho 1 sản phẩm. */
    public boolean increaseQuantity(long productId, int quantity) {
        if (productId <= 0) throw new IllegalArgumentException("productId invalid");
        if (quantity <= 0) return true;
        return productRepository.increaseQuantity(productId, quantity);
    }

    /** Cộng tồn kho theo danh sách item (dùng khi huỷ đơn). */
    public boolean increaseStockBatch(java.util.List<com.example.quanlycuahanglaptop.domain.OrderItem> items) {
        if (items == null || items.isEmpty()) return true;
        boolean ok = true;
        for (com.example.quanlycuahanglaptop.domain.OrderItem it : items) {
            ok &= increaseQuantity(it.getProductId(), it.getQuantity());
        }
        return ok;
    }

    private void validate(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
        }
        if (product.getPrice() < 0) {
            throw new IllegalArgumentException("Giá sản phẩm không hợp lệ");
        }
        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }
    }
}


