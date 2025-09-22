package com.example.quanlycuahanglaptop.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.domain.Product;
import com.example.quanlycuahanglaptop.service.ProductService;
import com.example.quanlycuahanglaptop.ui.components.GridSpacingItemDecoration;
import com.example.quanlycuahanglaptop.ui.home.HomeProductAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AllProductsFragment extends Fragment {

    // UI Components
    private RecyclerView rvAllProducts;
    private RecyclerView rvFilterChips;
    private HomeProductAdapter productAdapter;
    private FilterChipAdapter filterChipAdapter;
    private android.widget.EditText etSearch;
    private android.widget.ImageView btnClearSearch;
    private ImageButton btnFilter;
    private ImageButton btnPrevPage, btnNextPage;
    private TextView tvPageInfo;
    private ProgressBar progressLoading;
    private LinearLayout emptyState;

    // Data & Services
    private ProductService productService;
    private List<Product> currentProducts = new ArrayList<>();
    private List<FilterOption> filterOptions = new ArrayList<>();
    
    // Pagination
    private int currentPage = 1;
    private int totalPages = 1;
    private static final int PAGE_SIZE = 6;
    private String currentSearchKeyword = "";
    private String currentSortBy = "id_desc";
    
    // Threading
    private ExecutorService executorService;
    private Handler mainHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Khởi tạo threading
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Khởi tạo services
        productService = new ProductService(requireContext());
        
        // Khởi tạo UI
        initViews(view);
        setupRecyclerViews();
        setupSearch();
        setupPagination();
        setupFilterChips();
        
        // Load dữ liệu ban đầu
        loadProducts();
    }

    private void initViews(View view) {
        rvAllProducts = view.findViewById(R.id.rv_all_products);
        rvFilterChips = view.findViewById(R.id.rv_filter_chips);
        etSearch = view.findViewById(R.id.et_search);
        btnClearSearch = view.findViewById(R.id.btn_clear_search);
        btnFilter = view.findViewById(R.id.btn_filter);
        btnPrevPage = view.findViewById(R.id.btn_prev_page);
        btnNextPage = view.findViewById(R.id.btn_next_page);
        tvPageInfo = view.findViewById(R.id.tv_page_info);
        progressLoading = view.findViewById(R.id.progress_loading);
        emptyState = view.findViewById(R.id.empty_state);
    }

    private void setupRecyclerViews() {
        // Setup products RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        rvAllProducts.setLayoutManager(layoutManager);

        int spacing = (int) (getResources().getDisplayMetrics().density * 8);
        rvAllProducts.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));

        productAdapter = new HomeProductAdapter(new HomeProductAdapter.Listener() {
            @Override
            public void onClick(Product product) {
                openProductDetail(product);
            }

            @Override
            public void onAddToCart(Product product) {
                // Không cần xử lý thêm vào giỏ hàng
            }
        });
        rvAllProducts.setAdapter(productAdapter);
        
        // Setup filter chips RecyclerView
        LinearLayoutManager chipLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        rvFilterChips.setLayoutManager(chipLayoutManager);
        
        filterChipAdapter = new FilterChipAdapter(sortBy -> {
            currentSortBy = sortBy;
            currentPage = 1;
            loadProducts();
        });
        rvFilterChips.setAdapter(filterChipAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Hiển thị/ẩn nút clear
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Debounce search để tránh query quá nhiều
                mainHandler.removeCallbacks(searchRunnable);
                mainHandler.postDelayed(searchRunnable, 500);
            }
        });
        
        // Xử lý nút clear
        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            etSearch.clearFocus();
        });
    }

    private final Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            currentSearchKeyword = etSearch.getText().toString().trim();
            currentPage = 1;
            loadProducts();
        }
    };

    private void setupPagination() {
        btnPrevPage.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                loadProducts();
            }
        });
        
        btnNextPage.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadProducts();
            }
        });
    }

    private void setupFilterChips() {
        // Khởi tạo filter options
        filterOptions.clear();
        filterOptions.add(new FilterOption("id_desc", "Mới nhất", true));
        filterOptions.add(new FilterOption("price_asc", "Giá thấp → cao", false));
        filterOptions.add(new FilterOption("price_desc", "Giá cao → thấp", false));
        filterOptions.add(new FilterOption("name_asc", "Tên A → Z", false));
        
        filterChipAdapter.submitList(new ArrayList<>(filterOptions));
    }

    private void loadProducts() {
        showLoading(true);
        
        executorService.execute(() -> {
            try {
                List<Product> products;
                int totalCount;
                
                if (currentSearchKeyword.isEmpty()) {
                    // Load với sắp xếp từ database
                    products = loadProductsWithSorting();
                    totalCount = productService.countAll();
                } else {
                    // Với tìm kiếm, load tất cả rồi sắp xếp trong memory
                    products = productService.searchByName(currentSearchKeyword, currentPage, PAGE_SIZE);
                    products = applySorting(products);
                    totalCount = productService.countByName(currentSearchKeyword);
                }
                
                totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
                
                final List<Product> finalProducts = products;
                mainHandler.post(() -> {
                    currentProducts = finalProducts;
                    productAdapter.submitList(finalProducts);
                    updatePaginationUI();
                    showEmptyState(finalProducts.isEmpty());
                    showLoading(false);
                });
                
            } catch (Exception e) {
                mainHandler.post(() -> {
                    showLoading(false);
                    // Có thể hiển thị error message
                });
            }
        });
    }

    private List<Product> loadProductsWithSorting() {
        switch (currentSortBy) {
            case "price_asc":
                return productService.findPageOrderedByPriceAsc(currentPage, PAGE_SIZE);
            case "price_desc":
                return productService.findPageOrderedByPriceDesc(currentPage, PAGE_SIZE);
            case "name_asc":
                return productService.findPageOrderedByNameAsc(currentPage, PAGE_SIZE);
            case "id_desc":
            default:
                return productService.findPage(currentPage, PAGE_SIZE);
        }
    }

    private List<Product> applySorting(List<Product> products) {
        switch (currentSortBy) {
            case "price_asc":
                Collections.sort(products, new Comparator<Product>() {
                    @Override
                    public int compare(Product p1, Product p2) {
                        return Double.compare(p1.getPrice(), p2.getPrice());
                    }
                });
                break;
            case "price_desc":
                Collections.sort(products, new Comparator<Product>() {
                    @Override
                    public int compare(Product p1, Product p2) {
                        return Double.compare(p2.getPrice(), p1.getPrice());
                    }
                });
                break;
            case "name_asc":
                Collections.sort(products, new Comparator<Product>() {
                    @Override
                    public int compare(Product p1, Product p2) {
                        return p1.getName().compareToIgnoreCase(p2.getName());
                    }
                });
                break;
            case "id_desc":
            default:
                // Mặc định sắp xếp theo ID desc (mới nhất)
                break;
        }
        return products;
    }

    private void updatePaginationUI() {
        tvPageInfo.setText(String.format("Trang %d/%d", currentPage, totalPages));
        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < totalPages);
    }

    private void showLoading(boolean show) {
        progressLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        rvAllProducts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        rvAllProducts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void openProductDetail(Product product) {
        Bundle args = new Bundle();
        if (product.getId() != null) {
            args.putLong(ProductDetailFragment.ARG_PRODUCT_ID, product.getId());
        }
        ProductDetailFragment fragment = new ProductDetailFragment();
        fragment.setArguments(args);
        
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, 
                                   R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    // Filter option class
    public static class FilterOption {
        private String value;
        private String label;
        private boolean selected;

        public FilterOption(String value, String label, boolean selected) {
            this.value = value;
            this.label = label;
            this.selected = selected;
        }

        public String getValue() { return value; }
        public String getLabel() { return label; }
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
    }
}


