package com.example.quanlycuahanglaptop.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.service.CartService;
import com.example.quanlycuahanglaptop.ui.cart.CartAdapter;
import com.example.quanlycuahanglaptop.ui.components.CustomToastDialog;
import com.example.quanlycuahanglaptop.ui.components.DeleteConfirmationDialog;
import com.example.quanlycuahanglaptop.utils.SessionManager;
import android.widget.Button;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemListener {

    private LinearLayout layoutEmptyCart;
    private LinearLayout layoutCartContent;
    private RecyclerView rvCartItems;
    private TextView tvTotalPrice;
    private MaterialButton btnShopNow;
    private MaterialButton btnClearAll;
    private Button btnCheckout;

    private CartService cartService;
    private CartAdapter cartAdapter;
    private List<com.example.quanlycuahanglaptop.repository.CartItemRepository.CartItemWithProduct> cartItems;
    
    // Threading
    private ExecutorService executorService;
    private Handler mainHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Khởi tạo threading
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Khởi tạo views
        initViews(view);
        
        // Khởi tạo services
        cartService = new CartService(requireContext());
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup click listeners
        setupClickListeners();
        
        // Load dữ liệu
        loadCartData();
    }

    private void initViews(View view) {
        layoutEmptyCart = view.findViewById(R.id.layout_empty_cart);
        layoutCartContent = view.findViewById(R.id.layout_cart_content);
        rvCartItems = view.findViewById(R.id.rv_cart_items);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        btnShopNow = view.findViewById(R.id.btn_shop_now);
        btnClearAll = view.findViewById(R.id.btn_clear_all);
        btnCheckout = view.findViewById(R.id.btn_checkout);
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartItems, this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCartItems.setAdapter(cartAdapter);
    }

    private void setupClickListeners() {
        // Button mua sắm ngay
        btnShopNow.setOnClickListener(v -> {
            // Chuyển đến All Products
            if (requireActivity() instanceof com.example.quanlycuahanglaptop.app.MainActivity) {
                com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                    requireActivity().findViewById(R.id.bottom_navigation);
                bottomNav.setSelectedItemId(R.id.nav_all_products);
            }
        });

        // Button xóa tất cả
        btnClearAll.setOnClickListener(v -> showClearAllDialog());

        // Button đặt hàng
        btnCheckout.setOnClickListener(v -> {
            if (cartItems == null || cartItems.isEmpty()) {
                com.example.quanlycuahanglaptop.util.CustomToast.showError(requireContext(), "Giỏ hàng trống!");
                return;
            }
            
            // Chuyển đến CheckoutActivity
            android.content.Intent intent = new android.content.Intent(requireContext(), 
                com.example.quanlycuahanglaptop.ui.checkout.CheckoutActivity.class);
            startActivity(intent);
        });
    }

    private void loadCartData() {
        if (executorService == null || executorService.isShutdown()) {
            return;
        }
        
        SessionManager sessionManager = new SessionManager(requireContext());
        long userId = sessionManager.getUserId();
        
        if (userId <= 0) {
            showEmptyCart();
            return;
        }

        executorService.execute(() -> {
            try {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                
                cartItems = cartService.getCartItems(userId);
                
                if (mainHandler != null && !Thread.currentThread().isInterrupted()) {
                    mainHandler.post(() -> {
                        if (getView() == null || getActivity() == null) {
                            return; // Fragment đã bị destroy
                        }
                        
                        if (cartItems == null || cartItems.isEmpty()) {
                            showEmptyCart();
                        } else {
                            showCartContent();
                            if (cartAdapter != null) {
                                cartAdapter.updateCartItems(cartItems);
                            }
                            updateTotalPrice();
                        }
                    });
                }
                
            } catch (Exception e) {
                if (mainHandler != null && !Thread.currentThread().isInterrupted()) {
                    mainHandler.post(() -> {
                        if (getView() != null && getActivity() != null) {
                            showEmptyCart();
                        }
                    });
                }
            }
        });
    }

    private void showEmptyCart() {
        layoutEmptyCart.setVisibility(View.VISIBLE);
        layoutCartContent.setVisibility(View.GONE);
    }

    private void showCartContent() {
        layoutEmptyCart.setVisibility(View.GONE);
        layoutCartContent.setVisibility(View.VISIBLE);
    }

    private void updateTotalPrice() {
        if (cartItems != null && !cartItems.isEmpty()) {
            double total = cartService.calculateTotal(cartItems);
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvTotalPrice.setText(formatter.format(total));
        } else {
            tvTotalPrice.setText("0 VNĐ");
        }
    }

    private void showClearAllDialog() {
        CustomToastDialog dialog = CustomToastDialog.newInstance(
            "Xác nhận xóa",
            "Bạn có chắc chắn muốn xóa tất cả sản phẩm khỏi giỏ hàng?",
            "Xóa tất cả"
        );
        
        dialog.setOnPrimaryClickListener(() -> {
            if (executorService == null || executorService.isShutdown()) {
                return;
            }
            
            SessionManager sessionManager = new SessionManager(requireContext());
            long userId = sessionManager.getUserId();
            
            executorService.execute(() -> {
                try {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    
                    boolean success = cartService.clearCart(userId);
                    
                    if (mainHandler != null && !Thread.currentThread().isInterrupted()) {
                        mainHandler.post(() -> {
                            if (getView() == null || getActivity() == null) {
                                return;
                            }
                            
                            if (success) {
                                com.example.quanlycuahanglaptop.util.CustomToast.showSuccess(requireContext(), "Đã xóa tất cả sản phẩm khỏi giỏ hàng!");
                                showEmptyCart();
                            } else {
                                com.example.quanlycuahanglaptop.util.CustomToast.showError(requireContext(), "Có lỗi xảy ra khi xóa giỏ hàng!");
                            }
                        });
                    }
                } catch (Exception e) {
                    // Ignore exception if fragment is destroyed
                }
            });
        });
        
        dialog.show(getParentFragmentManager(), "clear_all_dialog");
    }

    private void showDeleteItemDialog(long cartItemId, String productName) {
        DeleteConfirmationDialog dialog = DeleteConfirmationDialog.newInstance(productName);
        
        dialog.setOnDeleteConfirmListener(new DeleteConfirmationDialog.OnDeleteConfirmListener() {
            @Override
            public void onConfirm() {
                if (executorService == null || executorService.isShutdown()) {
                    return;
                }
                
                executorService.execute(() -> {
                    try {
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        
                        boolean success = cartService.removeFromCart(cartItemId);
                        
                        if (mainHandler != null && !Thread.currentThread().isInterrupted()) {
                            mainHandler.post(() -> {
                                if (getView() == null || getActivity() == null) {
                                    return;
                                }
                                
                                if (success) {
                                    com.example.quanlycuahanglaptop.util.CustomToast.showSuccess(requireContext(), "Đã xóa sản phẩm khỏi giỏ hàng!");
                                    loadCartData(); // Reload data
                                } else {
                                    com.example.quanlycuahanglaptop.util.CustomToast.showError(requireContext(), "Có lỗi xảy ra khi xóa sản phẩm!");
                                }
                            });
                        }
                    } catch (Exception e) {
                        // Ignore exception if fragment is destroyed
                    }
                });
            }
            
            @Override
            public void onCancel() {
                // Không làm gì, chỉ đóng dialog
            }
        });
        
        dialog.show(getParentFragmentManager(), "delete_item_dialog");
    }

    // CartAdapter.OnCartItemListener implementation
    @Override
    public void onQuantityChanged(long cartItemId, int newQuantity) {
        if (executorService == null || executorService.isShutdown()) {
            return;
        }
        
        executorService.execute(() -> {
            try {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                
                boolean success = cartService.updateQuantity(cartItemId, newQuantity);
                
                if (mainHandler != null && !Thread.currentThread().isInterrupted()) {
                    mainHandler.post(() -> {
                        if (getView() == null || getActivity() == null) {
                            return;
                        }
                        
                        if (success) {
                            loadCartData(); // Reload data
                        } else {
                            com.example.quanlycuahanglaptop.util.CustomToast.showError(requireContext(), "Có lỗi xảy ra khi cập nhật số lượng!");
                        }
                    });
                }
            } catch (Exception e) {
                // Ignore exception if fragment is destroyed
            }
        });
    }

    @Override
    public void onRemoveItem(long cartItemId) {
        // Tìm tên sản phẩm để hiển thị trong dialog
        String productName = "sản phẩm";
        if (cartItems != null) {
            for (com.example.quanlycuahanglaptop.repository.CartItemRepository.CartItemWithProduct item : cartItems) {
                if (item.getCartItem().getId() == cartItemId) {
                    productName = item.getProduct().getName();
                    break;
                }
            }
        }
        showDeleteItemDialog(cartItemId, productName);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload data khi quay lại fragment
        loadCartData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanup();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cleanup();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cleanup();
    }

    private void cleanup() {
        // Cancel tất cả pending tasks
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
            try {
                if (!executorService.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // Clear handler callbacks
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
        
        // Clear references
        cartItems = null;
        cartAdapter = null;
    }
}
