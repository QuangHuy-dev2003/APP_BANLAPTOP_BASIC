package com.example.quanlycuahanglaptop.ui.checkout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.app.MainActivity;
import com.example.quanlycuahanglaptop.data.AppDatabase;
import com.example.quanlycuahanglaptop.utils.SessionManager;
import com.example.quanlycuahanglaptop.domain.Order;
import com.example.quanlycuahanglaptop.domain.OrderItem;
import com.example.quanlycuahanglaptop.repository.CartItemRepository;
import com.example.quanlycuahanglaptop.service.CartService;
import com.example.quanlycuahanglaptop.service.OrderService;
import com.example.quanlycuahanglaptop.service.UserService;
import com.example.quanlycuahanglaptop.util.CustomToast;
import androidx.appcompat.widget.AppCompatButton;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity hiển thị giao diện đặt hàng với thông tin sản phẩm, tổng tiền, địa chỉ và nút đặt hàng.
 * Sử dụng animation nhẹ nhàng và thiết kế Material Design.
 */
public class CheckoutActivity extends AppCompatActivity implements CheckoutAdapter.OnItemClickListener {

    private RecyclerView rvCheckoutItems;
    private TextView tvSubtotalPrice;
    private TextView tvShippingFee;
    private TextView tvTotalPrice;
    private EditText etAddress;
    private AppCompatButton btnPlaceOrder;
    private LinearLayout cardAddress;
    private LinearLayout cardTotal;
    private FrameLayout progressOverlay;
    private LinearLayout cardPaymentMethod;
    private LinearLayout layoutPaymentMethod;
    private TextView tvPaymentMethod;
    
    private CheckoutAdapter adapter;
    private List<CartItemRepository.CartItemWithProduct> cartItems;
    private double subtotalPrice = 0.0;
    private double shippingFee = 30000.0; // Phí vận chuyển cố định 30k
    private double totalPrice = 0.0;
    private CartService cartService;
    private OrderService orderService;
    private UserService userService;
    private long currentUserId;
    private long orderId;
    private String selectedPaymentMethod = "Thanh toán khi nhận hàng";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        
        // Ẩn action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        // Khởi tạo services
        cartService = new CartService(this);
        orderService = new OrderService(this);
        userService = new UserService(this);
        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        
        initViews();
        setupAnimations();
        loadCartItems();
        setupListeners();
        setupBackPressedCallback();
        updatePlaceOrderButton(); // Cập nhật trạng thái nút ban đầu
    }

    private void initViews() {
        rvCheckoutItems = findViewById(R.id.rv_checkout_items);
        cardPaymentMethod = findViewById(R.id.card_payment_method);
        layoutPaymentMethod = findViewById(R.id.layout_payment_method);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvSubtotalPrice = findViewById(R.id.tv_subtotal_price);
        tvShippingFee = findViewById(R.id.tv_shipping_fee);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        etAddress = findViewById(R.id.et_address);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        cardAddress = findViewById(R.id.card_address);
        cardTotal = findViewById(R.id.card_total);
        progressOverlay = findViewById(R.id.progress_overlay);
        
        // Setup RecyclerView
        rvCheckoutItems.setLayoutManager(new LinearLayoutManager(this));
        rvCheckoutItems.setHasFixedSize(true);
        
        // Ẩn progress overlay ban đầu
        progressOverlay.setVisibility(View.GONE);
    }

    private void setupAnimations() {
        // Animation cho các card khi load
        cardAddress.setAlpha(0f);
        cardTotal.setAlpha(0f);
        btnPlaceOrder.setAlpha(0f);
        
        // Animation slide in từ dưới lên
        cardAddress.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .setStartDelay(200)
                .start();
                
        cardTotal.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .setStartDelay(400)
                .start();
                
        btnPlaceOrder.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .setStartDelay(600)
                .start();
    }

    private void loadCartItems() {
        cartItems = cartService.getCartItems(currentUserId);
        if (cartItems.isEmpty()) {
            CustomToast.showError(this, "Giỏ hàng trống!");
            finish();
            return;
        }
        
        // Tính tổng tiền
        calculatePrices();
        
        // Setup adapter
        adapter = new CheckoutAdapter(cartItems, this);
        rvCheckoutItems.setAdapter(adapter);
        
        // Animation cho RecyclerView
        rvCheckoutItems.setAlpha(0f);
        rvCheckoutItems.animate()
                .alpha(1f)
                .setDuration(800)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(100)
                .start();
    }

    private void calculatePrices() {
        // Tính tổng tiền sản phẩm
        subtotalPrice = 0.0;
        for (CartItemRepository.CartItemWithProduct item : cartItems) {
            subtotalPrice += item.getProduct().getPrice() * item.getCartItem().getQuantity();
        }
        
        // Tính tổng tiền cuối cùng (sản phẩm + phí vận chuyển)
        totalPrice = subtotalPrice + shippingFee;
        
        updatePriceDisplay();
    }

    private void updatePriceDisplay() {
        // Hiển thị tổng tiền sản phẩm
        tvSubtotalPrice.setText(formatCurrency(subtotalPrice));
        
        // Hiển thị phí vận chuyển
        tvShippingFee.setText(formatCurrency(shippingFee));
        
        // Hiển thị tổng tiền cuối cùng
        tvTotalPrice.setText(formatCurrency(totalPrice));
        
        // Animation cho tổng tiền
        animatePriceChange();
    }
    
    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        return formatter.format(amount) + " đ";
    }

    private void animatePriceChange() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(tvTotalPrice, "scaleX", 1.0f, 1.1f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(tvTotalPrice, "scaleY", 1.0f, 1.1f, 1.0f);
        
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        
        scaleX.start();
        scaleY.start();
    }

    private void setupListeners() {
        // Text watcher cho địa chỉ
        etAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePlaceOrderButton();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Nút đặt hàng
        btnPlaceOrder.setOnClickListener(v -> handlePlaceOrder());
        
        // Nút back
        findViewById(R.id.btn_back).setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        // Xử lý chọn phương thức thanh toán
        layoutPaymentMethod.setOnClickListener(v -> showPaymentMethodDialog());
    }

    private void updatePlaceOrderButton() {
        boolean hasAddress = !etAddress.getText().toString().trim().isEmpty();
        boolean hasPaymentMethod = selectedPaymentMethod != null && !selectedPaymentMethod.isEmpty();
        boolean canPlaceOrder = hasAddress && hasPaymentMethod;
        
        btnPlaceOrder.setEnabled(canPlaceOrder);
        
        // Animation cho button state change
        if (canPlaceOrder) {
            btnPlaceOrder.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(150)
                    .withEndAction(() -> btnPlaceOrder.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(150)
                            .start())
                    .start();
        }
    }

    private void showPaymentMethodDialog() {
        PaymentMethodDialog dialog = PaymentMethodDialog.newInstance();
        dialog.setOnPaymentMethodSelectedListener(method -> {
            selectedPaymentMethod = method;
            tvPaymentMethod.setText(method);
            // Animation nhẹ nhàng khi cập nhật text
            tvPaymentMethod.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(150)
                    .withEndAction(() -> tvPaymentMethod.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(150)
                            .start())
                    .start();
            // Cập nhật trạng thái nút đặt hàng
            updatePlaceOrderButton();
        });
        dialog.show(getSupportFragmentManager(), "PaymentMethodDialog");
    }

    private void handlePlaceOrder() {
        String address = etAddress.getText().toString().trim();
        if (address.isEmpty()) {
            CustomToast.showError(this, "Vui lòng nhập địa chỉ giao hàng!");
            return;
        }
        
        if (selectedPaymentMethod == null || selectedPaymentMethod.isEmpty()) {
            CustomToast.showError(this, "Vui lòng chọn phương thức thanh toán!");
            return;
        }

        // Hiển thị progress
        showProgress(true);
        
        // Animation cho button khi đặt hàng
        animatePlaceOrderButton();
        
        // Tạo order
        createOrder(address);
    }

    private void animatePlaceOrderButton() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(btnPlaceOrder, "scaleX", 1.0f, 0.95f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(btnPlaceOrder, "scaleY", 1.0f, 0.95f, 1.0f);
        
        scaleX.setDuration(200);
        scaleY.setDuration(200);
        
        scaleX.start();
        scaleY.start();
    }

    private void createOrder(String address) {
        try {
            // Lấy thông tin user để lấy số điện thoại
            com.example.quanlycuahanglaptop.domain.User user = userService.getById(currentUserId);
            String userPhone = user != null ? user.getPhone() : null;
            
            // Tạo Order
            Order order = new Order();
            order.setUserId(currentUserId);
            order.setTotalPrice(totalPrice); // Đã bao gồm phí vận chuyển
            order.setAddress(address);
            order.setPhone(userPhone);
            order.setStatus(com.example.quanlycuahanglaptop.domain.OrderStatus.RECEIVED); // Mặc định là "Đã Tiếp Nhận"
            
            orderId = orderService.createOrder(order);
            
            // Tạo OrderItems
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItemRepository.CartItemWithProduct cartItem : cartItems) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(orderId);
                orderItem.setProductId(cartItem.getProduct().getId());
                orderItem.setQuantity(cartItem.getCartItem().getQuantity());
                orderItem.setPrice(cartItem.getProduct().getPrice());
                orderItems.add(orderItem);
            }
            
            // Lưu OrderItems
            orderService.createOrderItems(orderItems);
            
            // Xóa giỏ hàng
            cartService.clearCart(currentUserId);
            
            // Hiển thị thành công
            showSuccessAnimation();
            
        } catch (Exception e) {
            showProgress(false);
            CustomToast.showError(this, "Lỗi khi đặt hàng: " + e.getMessage());
        }
    }

    private void showSuccessAnimation() {
        // Animation thành công
        ObjectAnimator successAnim = ObjectAnimator.ofFloat(btnPlaceOrder, "scaleX", 1.0f, 1.2f, 1.0f);
        ObjectAnimator successAnimY = ObjectAnimator.ofFloat(btnPlaceOrder, "scaleY", 1.0f, 1.2f, 1.0f);
        
        successAnim.setDuration(300);
        successAnimY.setDuration(300);
        
        successAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                showProgress(false);
                CustomToast.showSuccess(CheckoutActivity.this, "Đặt hàng thành công!");
                
                // Chuyển đến ThankYouActivity
                Intent intent = new Intent(CheckoutActivity.this, ThankYouActivity.class);
                intent.putExtra("order_id", orderId);
                intent.putExtra("user_id", currentUserId);
                startActivity(intent);
                finish();
            }
        });
        
        successAnim.start();
        successAnimY.start();
    }

    private void showProgress(boolean show) {
        if (show) {
            progressOverlay.setVisibility(View.VISIBLE);
            progressOverlay.setAlpha(0f);
            progressOverlay.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        } else {
            progressOverlay.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            progressOverlay.setVisibility(View.GONE);
                        }
                    })
                    .start();
        }
    }

    @Override
    public void onItemClick(CartItemRepository.CartItemWithProduct item) {
        // Có thể thêm logic xem chi tiết sản phẩm nếu cần
    }

    private void setupBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }
}
