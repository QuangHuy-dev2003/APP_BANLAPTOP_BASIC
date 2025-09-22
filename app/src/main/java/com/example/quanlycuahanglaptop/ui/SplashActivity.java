package com.example.quanlycuahanglaptop.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.app.AdminActivity;
import com.example.quanlycuahanglaptop.app.LoginActivity;
import com.example.quanlycuahanglaptop.app.MainActivity;
import com.example.quanlycuahanglaptop.service.AuthService;
import com.example.quanlycuahanglaptop.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		LottieAnimationView lottie = findViewById(R.id.splashLottie);

		lottie.addAnimatorListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				goToMain();
			}
		});

		// Fallback phòng trường hợp animation bị cancel/quá nhanh
		lottie.postDelayed(this::goToMain, 3000);
	}

    private void goToMain() {
        if (isFinishing()) return;

        // Kiểm tra session đăng nhập
        AuthService authService = new AuthService(this);
        SessionManager sessionManager = new SessionManager(this);

        if (authService.isLoggedIn()) {
            // Điều hướng theo role lưu trong SessionManager
            String role = sessionManager.getUserRole();
            if (role != null && role.equalsIgnoreCase("ADMIN")) {
                Intent i = new Intent(this, AdminActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            } else {
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        } else {
            // Hết hạn hoặc chưa đăng nhập → về màn Login
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
        finish();
    }
}