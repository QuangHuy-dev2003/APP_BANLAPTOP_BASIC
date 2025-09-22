package com.example.quanlycuahanglaptop.app;

import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.util.CustomToast;
import com.example.quanlycuahanglaptop.service.AuthService;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtUsername;
    private EditText edtPhone;
    private EditText edtPassword;
    private ImageView imgTogglePassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView tvGoLogin = findViewById(R.id.tvGoLogin);
        tvGoLogin.setOnClickListener(v -> finish());

        initViews();
        setupPasswordToggle();
        findViewById(R.id.btnRegister).setOnClickListener(v -> handleRegister());
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtUsername = findViewById(R.id.edtUsername);
        edtPhone = findViewById(R.id.edtPhone);
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

    private boolean validateRegister() {
        String email = edtEmail.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
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
        if (username.isEmpty()) {
            CustomToast.showError(this, "Vui lòng nhập tên người dùng");
            edtUsername.requestFocus();
            return false;
        }
        if (phone.isEmpty()) {
            CustomToast.showError(this, "Vui lòng nhập số điện thoại");
            edtPhone.requestFocus();
            return false;
        }
        if (!isValidPhoneNumber(phone)) {
            CustomToast.showError(this, "Số điện thoại không hợp lệ");
            edtPhone.requestFocus();
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

    private void handleRegister() {
        if (!validateRegister()) return;
        String email = edtEmail.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString();
        try {
            AuthService authService = new AuthService(this);
            authService.register(username, email, password, phone);
            CustomToast.showSuccess(this, getString(R.string.register_success));
            finish();
        } catch (IllegalArgumentException ex) {
            CustomToast.showError(this, ex.getMessage());
        } catch (Exception ex) {
            CustomToast.showError(this, "Đăng ký thất bại, vui lòng thử lại");
        }
    }

    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        // Loại bỏ tất cả ký tự không phải số
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        // Kiểm tra độ dài (10-11 chữ số cho số điện thoại Việt Nam)
        return cleanPhone.length() >= 10 && cleanPhone.length() <= 11;
    }
}


