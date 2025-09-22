package com.example.quanlycuahanglaptop.ui.checkout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.app.MainActivity;
import com.example.quanlycuahanglaptop.domain.Product;
import com.example.quanlycuahanglaptop.service.ProductService;
import com.example.quanlycuahanglaptop.ui.home.HomeProductAdapter;

import java.util.List;

/**
 * Activity hiển thị giao diện cảm ơn sau khi đặt hàng thành công
 * Bao gồm thông tin đơn hàng, nút theo dõi đơn hàng, về trang chủ và gợi ý sản phẩm
 */
public class ThankYouActivity extends AppCompatActivity implements HomeProductAdapter.Listener {

    private TextView tvThankYou;
    private TextView tvOrderId;
    private AppCompatButton btnTrackOrder;
    private AppCompatButton btnGoHome;
    private RecyclerView rvRecommendedProducts;
    private View cardThankYou;
    private View cardRecommended;
    
    private HomeProductAdapter productAdapter;
    private ProductService productService;
    private long orderId;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thank_you);
        
        // Ẩn action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        // Lấy orderId và userId từ intent
        orderId = getIntent().getLongExtra("order_id", -1);
        userId = getIntent().getLongExtra("user_id", -1);
        
        initViews();
        setupAnimations();
        loadRecommendedProducts();
        setupListeners();
        setupBackPressedCallback();
    }

    private void initViews() {
        tvThankYou = findViewById(R.id.tv_thank_you);
        tvOrderId = findViewById(R.id.tv_order_id);
        btnTrackOrder = findViewById(R.id.btn_track_order);
        btnGoHome = findViewById(R.id.btn_go_home);
        rvRecommendedProducts = findViewById(R.id.rv_recommended_products);
        cardThankYou = findViewById(R.id.card_thank_you);
        cardRecommended = findViewById(R.id.card_recommended);
        
        // Setup RecyclerView cho sản phẩm gợi ý
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvRecommendedProducts.setLayoutManager(layoutManager);
        
        productAdapter = new HomeProductAdapter(this);
        rvRecommendedProducts.setAdapter(productAdapter);
        
        // Hiển thị mã đơn hàng
        if (orderId != -1) {
            tvOrderId.setText("Mã đơn hàng: #" + orderId);
        } else {
            tvOrderId.setText("Mã đơn hàng: #" + System.currentTimeMillis());
        }
        
        // Khởi tạo service
        productService = new ProductService(this);
    }

    private void setupAnimations() {
        // Ẩn các view ban đầu
        cardThankYou.setAlpha(0f);
        cardThankYou.setTranslationY(50f);
        cardRecommended.setAlpha(0f);
        cardRecommended.setTranslationY(50f);
        
        // Animation cho card cảm ơn
        cardThankYou.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .setStartDelay(200)
                .start();
        
        // Animation cho card gợi ý sản phẩm (chỉ nếu có sản phẩm)
        if (cardRecommended.getVisibility() == View.VISIBLE) {
            cardRecommended.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .setStartDelay(600)
                    .start();
        }
        
        // Animation cho các button
        animateButtons();
    }

    private void animateButtons() {
        btnTrackOrder.setAlpha(0f);
        btnGoHome.setAlpha(0f);
        
        btnTrackOrder.animate()
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(400)
                .start();
                
        btnGoHome.animate()
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(500)
                .start();
    }

    private void loadRecommendedProducts() {
        try {
            // Load 4 sản phẩm random
            List<Product> randomProducts = productService.findRandomProducts(4);
            
            if (randomProducts != null && !randomProducts.isEmpty()) {
                productAdapter.submitList(randomProducts);
                
                // Animation cho RecyclerView
                rvRecommendedProducts.setAlpha(0f);
                rvRecommendedProducts.animate()
                        .alpha(1f)
                        .setDuration(1000)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setStartDelay(800)
                        .start();
            } else {
                // Ẩn card gợi ý nếu không có sản phẩm
                cardRecommended.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            // Xử lý lỗi và ẩn card gợi ý
            cardRecommended.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        // Nút theo dõi đơn hàng
        btnTrackOrder.setOnClickListener(v -> {
            animateButtonClick(btnTrackOrder);
            // Chuyển đến trang lịch sử đơn hàng
            Intent intent = new Intent(this, com.example.quanlycuahanglaptop.ui.order.OrderHistoryActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });
        
        // Nút về trang chủ
        btnGoHome.setOnClickListener(v -> {
            animateButtonClick(btnGoHome);
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void animateButtonClick(AppCompatButton button) {
        // Animation scale khi click
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1.0f, 0.95f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1.0f, 0.95f, 1.0f);
        
        // Animation elevation để tạo hiệu ứng nổi
        ObjectAnimator elevation = ObjectAnimator.ofFloat(button, "elevation", 4f, 8f, 4f);
        
        scaleX.setDuration(150);
        scaleY.setDuration(150);
        elevation.setDuration(200);
        
        scaleX.start();
        scaleY.start();
        elevation.start();
    }

    @Override
    public void onClick(Product product) {
        // Xử lý click vào sản phẩm gợi ý - chuyển đến MainActivity với ProductDetailFragment
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("fragment", "product_detail");
        intent.putExtra("product_id", product.getId());
        // Không dùng CLEAR_TOP để tránh restart MainActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    public void onAddToCart(Product product) {
        // Xử lý thêm vào giỏ hàng từ sản phẩm gợi ý
        // TODO: Implement add to cart logic
    }

    private void setupBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Chuyển về trang chủ thay vì quay lại checkout
                Intent intent = new Intent(ThankYouActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
