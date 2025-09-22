package com.example.quanlycuahanglaptop.app;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.ui.fragments.CartFragment;
import com.example.quanlycuahanglaptop.ui.fragments.HomeFragment;
import com.example.quanlycuahanglaptop.ui.fragments.ProfileFragment;
import com.example.quanlycuahanglaptop.ui.fragments.AllProductsFragment;
import com.example.quanlycuahanglaptop.ui.fragments.ProductDetailFragment;
import com.example.quanlycuahanglaptop.data.AuthManager;
import com.example.quanlycuahanglaptop.service.AuthService;
import com.example.quanlycuahanglaptop.ui.components.CustomToastDialog;
import android.content.Intent;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.quanlycuahanglaptop.data.AppDatabase;
import com.example.quanlycuahanglaptop.util.CustomToast;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private boolean enableAnimations = true;
    
    // Cache fragments để tránh tạo mới mỗi lần
    private HomeFragment homeFragment;
    private AllProductsFragment allProductsFragment;
    private CartFragment cartFragment;
    private ProfileFragment profileFragment;
    
    // Track current selected item để tối ưu animation
    private int currentSelectedItem = R.id.nav_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Ẩn thanh tiêu đề
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        // Kiểm tra cấu hình thiết bị để quyết định có dùng animation không
        checkDevicePerformance();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Chỉ áp top/side để tránh tạo khoảng trống xám phía trên BottomNavigation
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Khởi tạo Bottom Navigation
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            // Tránh xử lý lại cùng một item
            if (itemId == currentSelectedItem) {
                return true;
            }
            
            if (itemId == R.id.nav_home) {
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                }
                selectedFragment = homeFragment;
            } else if (itemId == R.id.nav_all_products) {
                if (allProductsFragment == null) {
                    allProductsFragment = new AllProductsFragment();
                }
                selectedFragment = allProductsFragment;
            } else if (itemId == R.id.nav_cart) {
                if (cartFragment == null) {
                    cartFragment = new CartFragment();
                }
                selectedFragment = cartFragment;
            } else if (itemId == R.id.nav_profile) {
                AuthService authService = new AuthService(this);
                if (!authService.isLoggedIn()) {
                    CustomToastDialog dialog = CustomToastDialog.newInstance(
                            getString(R.string.title_require_login),
                            getString(R.string.msg_require_login),
                            getString(R.string.btn_agree)
                    );
                    dialog.setOnPrimaryClickListener(() -> {
                        Intent i = new Intent(this, LoginActivity.class);
                        startActivity(i);
                    });
                    dialog.show(getSupportFragmentManager(), "require_login_dialog");
                    return false;
                } else {
                    if (profileFragment == null) {
                        profileFragment = new ProfileFragment();
                    }
                    selectedFragment = profileFragment;
                }
            }
            
            if (selectedFragment != null) {
                // Kiểm tra xem có phải Fragment hiện tại không để tránh animation không cần thiết
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment == null || !currentFragment.getClass().equals(selectedFragment.getClass())) {
                    var transaction = getSupportFragmentManager().beginTransaction();
                    
                    // Animation thông minh dựa trên hướng chuyển đổi
                    if (enableAnimations) {
                        int enterAnim, exitAnim;
                        
                        // Animation đặc biệt cho Home
                        if (itemId == R.id.nav_home) {
                            enterAnim = R.anim.home_enter;
                            exitAnim = R.anim.smooth_fade_out;
                        } else if (shouldSlideFromRight(itemId)) {
                            enterAnim = R.anim.slide_in_right;
                            exitAnim = R.anim.slide_out_left;
                        } else {
                            enterAnim = R.anim.slide_in_left;
                            exitAnim = R.anim.slide_out_right;
                        }
                        
                        transaction.setCustomAnimations(enterAnim, exitAnim, enterAnim, exitAnim);
                    }
                    
                    transaction.replace(R.id.fragment_container, selectedFragment).commit();
                    currentSelectedItem = itemId;
                }
                return true;
            }
            return false;
        });

        // Xử lý intent để chuyển hướng đến fragment cụ thể
        handleIntent(getIntent());

        // Hiển thị HomeFragment mặc định
        if (savedInstanceState == null) {
            // Tạo HomeFragment ngay từ đầu để tránh delay
            if (homeFragment == null) {
                homeFragment = new HomeFragment();
            }
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        } else {
            // Restore current selected item từ savedInstanceState
            currentSelectedItem = savedInstanceState.getInt("current_selected_item", R.id.nav_home);
            bottomNavigation.setSelectedItemId(currentSelectedItem);
        }

        
        var db = AppDatabase.getInstance(this).getConnection();
        String dbPath = AppDatabase.getDatabaseAbsolutePath(this);
        Log.d("MainActivity", "Database path: " + dbPath);
    }
    
    /**
     * Kiểm tra hiệu suất thiết bị để quyết định có sử dụng animation không
     */
    private void checkDevicePerformance() {
        Configuration config = getResources().getConfiguration();
        
        // Kiểm tra xem có phải máy ảo không (thường có RAM thấp)
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024; // MB
        
        // Nếu RAM < 1GB hoặc là máy ảo, tắt animation để tối ưu hiệu suất
        if (maxMemory < 1024 || isEmulator()) {
            enableAnimations = false;
        }
    }
    
    /**
     * Kiểm tra xem có phải máy ảo không
     */
    private boolean isEmulator() {
        return android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
                || android.os.Build.MANUFACTURER.contains("Genymotion")
                || (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(android.os.Build.PRODUCT);
    }
    
    /**
     * Refresh HomeFragment khi cần thiết (ví dụ: sau khi thêm vào giỏ hàng)
     */
    public void refreshHomeFragment() {
        if (homeFragment != null) {
            homeFragment.refreshCartBadge();
        }
    }
    
    /**
     * Clear backstack để tránh navigation phức tạp
     */
    public void clearBackStack() {
        getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
    
    /**
     * Lấy fragment hiện tại
     */
    public Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }
    
    /**
     * Xác định hướng slide animation dựa trên vị trí item trong bottom navigation
     */
    private boolean shouldSlideFromRight(int itemId) {
        // Home -> All Products -> Cart -> Profile
        // Nếu chuyển từ trái sang phải thì slide từ right
        if (currentSelectedItem == R.id.nav_home && itemId == R.id.nav_all_products) return true;
        if (currentSelectedItem == R.id.nav_all_products && itemId == R.id.nav_cart) return true;
        if (currentSelectedItem == R.id.nav_cart && itemId == R.id.nav_profile) return true;
        
        // Nếu chuyển từ phải sang trái thì slide từ left
        if (currentSelectedItem == R.id.nav_profile && itemId == R.id.nav_cart) return false;
        if (currentSelectedItem == R.id.nav_cart && itemId == R.id.nav_all_products) return false;
        if (currentSelectedItem == R.id.nav_all_products && itemId == R.id.nav_home) return false;
        
        // Mặc định slide từ right
        return true;
    }
    
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_selected_item", currentSelectedItem);
    }
    
    /**
     * Tối ưu hóa việc load HomeFragment
     */
    public void preloadHomeFragment() {
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
        }
    }

    /**
     * Xử lý intent để chuyển hướng đến fragment cụ thể
     */
    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("fragment")) {
            String fragmentType = intent.getStringExtra("fragment");
            
            if ("product_detail".equals(fragmentType)) {
                long productId = intent.getLongExtra("product_id", -1);
                if (productId > 0) {
                    // Chuyển đến ProductDetailFragment
                    Bundle args = new Bundle();
                    args.putLong(ProductDetailFragment.ARG_PRODUCT_ID, productId);
                    ProductDetailFragment fragment = new ProductDetailFragment();
                    fragment.setArguments(args);
                    
                    getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, 
                                           R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }
}