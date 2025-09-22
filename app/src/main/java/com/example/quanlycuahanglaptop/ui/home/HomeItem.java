package com.example.quanlycuahanglaptop.ui.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.quanlycuahanglaptop.domain.Product;

import java.util.List;

/**
 * Mục dữ liệu hiển thị trên Home (đa view-type).
 */
public class HomeItem {

    public enum Type {
        HEADER,
        BANNER,
        CATEGORY_CHIPS,
        SECTION_TITLE,
        PRODUCT_BIG_CARD,   // dùng trực tiếp cho grid 2 cột
        PRODUCT_SMALL_LIST  // một hàng ngang gồm nhiều card nhỏ
    }

    private final Type type;
    @Nullable private final String title;
    @Nullable private final List<String> categories;
    @Nullable private final List<Integer> bannerImages; // drawable ids tạm
    @Nullable private final Product product; // cho PRODUCT_BIG_CARD
    @Nullable private final List<Product> products; // cho PRODUCT_SMALL_LIST

    private HomeItem(Type type, @Nullable String title, @Nullable List<String> categories,
                     @Nullable List<Integer> bannerImages, @Nullable Product product,
                     @Nullable List<Product> products) {
        this.type = type;
        this.title = title;
        this.categories = categories;
        this.bannerImages = bannerImages;
        this.product = product;
        this.products = products;
    }

    public static HomeItem header() {
        return new HomeItem(Type.HEADER, null, null, null, null, null);
    }

    public static HomeItem banner(@NonNull List<Integer> drawableIds) {
        return new HomeItem(Type.BANNER, null, null, drawableIds, null, null);
    }

    public static HomeItem categoryChips(@NonNull List<String> chips) {
        return new HomeItem(Type.CATEGORY_CHIPS, null, chips, null, null, null);
    }

    public static HomeItem sectionTitle(@NonNull String title) {
        return new HomeItem(Type.SECTION_TITLE, title, null, null, null, null);
    }

    public static HomeItem bigCard(@NonNull Product product) {
        return new HomeItem(Type.PRODUCT_BIG_CARD, null, null, null, product, null);
    }

    public static HomeItem smallList(@NonNull List<Product> products) {
        return new HomeItem(Type.PRODUCT_SMALL_LIST, null, null, null, null, products);
    }

    public Type getType() { return type; }
    @Nullable public String getTitle() { return title; }
    @Nullable public List<String> getCategories() { return categories; }
    @Nullable public List<Integer> getBannerImages() { return bannerImages; }
    @Nullable public Product getProduct() { return product; }
    @Nullable public List<Product> getProducts() { return products; }
}


