package com.example.quanlycuahanglaptop.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.domain.Product;
import com.example.quanlycuahanglaptop.service.ProductService;
import com.example.quanlycuahanglaptop.service.CartService;
import com.example.quanlycuahanglaptop.ui.components.GridSpacingItemDecoration;
import com.example.quanlycuahanglaptop.ui.home.HomeAdapter;
import com.example.quanlycuahanglaptop.ui.home.HomeItem;
import com.example.quanlycuahanglaptop.utils.SessionManager;

public class HomeFragment extends Fragment {

    private RecyclerView rvProducts;
    private HomeAdapter adapter;
    private ProductService productService;
    private CartService cartService;
    
    // Threading và caching
    private ExecutorService executorService;
    private Handler mainHandler;
    private java.util.List<HomeItem> cachedItems;
    private boolean isDataLoaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Khởi tạo threading
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        rvProducts = view.findViewById(R.id.rv_home_products);
        productService = new ProductService(requireContext());
        cartService = new CartService(requireContext());

        // Grid 2 cột với span linh hoạt cho đa view-type
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        rvProducts.setLayoutManager(layoutManager);

        int spacing = (int) (getResources().getDisplayMetrics().density * 3);
        rvProducts.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));

        adapter = new HomeAdapter(new HomeAdapter.Listener() {
            @Override
            public void onSearchClick() { }
            @Override
            public void onFilterClick() { }
            @Override
            public void onCategoryClick(String category) { }
            @Override
            public void onProductClick(Product product) {
                Bundle args = new Bundle();
                if (product.getId() != null) args.putLong(ProductDetailFragment.ARG_PRODUCT_ID, product.getId());
                androidx.fragment.app.Fragment f = new ProductDetailFragment();
                f.setArguments(args);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.fragment_container, f)
                        .addToBackStack(null)
                        .commit();
            }
            @Override
            public void onAddToCartClick(Product product) { }
            @Override
            public void onSeeAllClick(String sectionTitle) { }
        });
        rvProducts.setAdapter(adapter);

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                HomeItem item = adapter.getCurrentList().get(position);
                switch (item.getType()) {
                    case PRODUCT_BIG_CARD:
                        return 1;
                    default:
                        return 2;
                }
            }
        });

        // Load dữ liệu với caching và threading
        loadDataAsync();

        // Cập nhật badge giỏ hàng
        updateCartBadge(view);
        
        // Thêm click listener cho icon giỏ hàng
        View btnCart = view.findViewById(R.id.btn_cart_home);
        btnCart.setOnClickListener(v -> {
            // Sử dụng bottom navigation để chuyển đến CartFragment
            // Điều này đảm bảo navbar được cập nhật đúng
            if (requireActivity() instanceof com.example.quanlycuahanglaptop.app.MainActivity) {
                com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                    requireActivity().findViewById(R.id.bottom_navigation);
                bottomNav.setSelectedItemId(R.id.nav_cart);
            }
        });
    }
    
    private void updateCartBadge(View view) {
        View badge = view.findViewById(R.id.badge_cart);
        SessionManager sessionManager = new SessionManager(requireContext());
        long userId = sessionManager.getUserId();
        int count = userId > 0 ? cartService.countItems(userId) : 0;
        if (badge instanceof android.widget.TextView) {
            android.widget.TextView tv = (android.widget.TextView) badge;
            if (count > 0) {
                tv.setText(String.valueOf(count));
                tv.setVisibility(View.VISIBLE);
            } else {
                tv.setVisibility(View.GONE);
            }
        }
    }
    
    public void refreshCartBadge() {
        if (getView() != null) {
            updateCartBadge(getView());
        }
    }
    
    /**
     * Load dữ liệu bất đồng bộ để tránh block UI
     */
    private void loadDataAsync() {
        // Nếu đã có dữ liệu cached, sử dụng ngay
        if (cachedItems != null && isDataLoaded) {
            adapter.submitList(cachedItems);
            return;
        }
        
        // Hiển thị loading state nếu cần
        showLoadingState();
        
        executorService.execute(() -> {
            try {
                // Load dữ liệu từ DB trong background thread
                // Bán chạy: 6 sản phẩm, ID giảm dần (mới nhất)
                java.util.List<Product> latest = productService.findPage(1, 8);
                // Deal hot hôm nay: 6 sản phẩm, ID bất kỳ (ngẫu nhiên)
                java.util.List<Product> hotDeals = productService.findRandomProducts(8);
                
                // Build danh sách HomeItem
                java.util.ArrayList<HomeItem> items = new java.util.ArrayList<>();
                items.add(HomeItem.header());
                items.add(HomeItem.banner(java.util.Arrays.asList(R.drawable.banner_1, R.drawable.ic_products)));
                items.add(HomeItem.categoryChips(java.util.Arrays.asList("Gaming", "Ultrabook", "Workstation", "HSSV")));
                items.add(HomeItem.sectionTitle("Deal hot hôm nay"));
                items.add(HomeItem.smallList(hotDeals));
                items.add(HomeItem.sectionTitle("Bán chạy"));
                for (Product p : latest) items.add(HomeItem.bigCard(p));
                
                // Cache dữ liệu
                cachedItems = items;
                isDataLoaded = true;
                
                // Update UI trên main thread
                mainHandler.post(() -> {
                    if (getView() != null && adapter != null) {
                        adapter.submitList(items);
                        hideLoadingState();
                    }
                });
                
            } catch (Exception e) {
                // Handle error
                mainHandler.post(() -> {
                    if (getView() != null) {
                        hideLoadingState();
                        // Có thể hiển thị error message
                    }
                });
            }
        });
    }
    
    /**
     * Hiển thị loading state
     */
    private void showLoadingState() {
        // Có thể thêm loading indicator nếu cần
    }
    
    /**
     * Ẩn loading state
     */
    private void hideLoadingState() {
        // Ẩn loading indicator nếu có
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh cart badge khi quay lại fragment
        if (getView() != null) {
            updateCartBadge(getView());
        }
    }
}
