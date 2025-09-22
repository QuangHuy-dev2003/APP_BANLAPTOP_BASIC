package com.example.quanlycuahanglaptop.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.data.AuthManager;
import com.example.quanlycuahanglaptop.util.CustomToast;
import com.example.quanlycuahanglaptop.domain.Role;
import com.example.quanlycuahanglaptop.domain.User;
import com.example.quanlycuahanglaptop.service.AuthService;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtPassword;
    private ImageView imgTogglePassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupPasswordToggle();
        findViewById(R.id.btnLogin).setOnClickListener(v -> handleLogin());
        // Nếu vừa chuyển từ đăng xuất, hiển thị toast thành công
        boolean showLogoutSuccess = getIntent() != null && getIntent().getBooleanExtra("SHOW_LOGOUT_SUCCESS", false);
        if (showLogoutSuccess) {
            CustomToast.showSuccess(this, "Đăng xuất thành công!");
        }

        TextView tvGoRegister = findViewById(R.id.tvGoRegister);
        tvGoRegister.setOnClickListener(v -> {
            Intent i = new Intent(this, RegisterActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);
    }

    private void setupPasswordToggle() {
        if (imgTogglePassword == null || edtPassword == null) return;
        imgTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void togglePasswordVisibility() {
        int start = edtPassword.getSelectionStart();
        int end = edtPassword.getSelectionEnd();
        boolean isPasswordVisible = (edtPassword.getInputType() & InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        if (isPasswordVisible) {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imgTogglePassword.setImageResource(R.drawable.ic_eye_off);
        } else {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imgTogglePassword.setImageResource(R.drawable.ic_eye_on);
        }
        edtPassword.setSelection(start, end);
    }

    private boolean validateLogin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();

        if (email.isEmpty()) {
            CustomToast.showError(this, "Vui lòng nhập email");
            edtEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            CustomToast.showError(this, "Email không hợp lệ");
            edtEmail.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            CustomToast.showError(this, "Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            CustomToast.showError(this, "Mật khẩu tối thiểu 6 ký tự");
            edtPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void handleLogin() {
        if (!validateLogin()) return;
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();
        try {
            AuthService authService = new AuthService(this);
            User user = authService.login(email, password);
            CustomToast.showSuccess(this, getString(R.string.login_success));

            if (user.getRole() == Role.ADMIN) {
                Intent i = new Intent(this, AdminActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            } else {
                // Quay về MainActivity (UI bình thường)
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        } catch (IllegalArgumentException ex) {
            CustomToast.showError(this, ex.getMessage());
        }
    }
}


