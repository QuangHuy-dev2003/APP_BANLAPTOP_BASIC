package com.example.quanlycuahanglaptop.ui.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.domain.User;
import com.example.quanlycuahanglaptop.service.UserService;
import com.example.quanlycuahanglaptop.utils.SessionManager;
import com.example.quanlycuahanglaptop.util.CustomToast;

public class UserInfoActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvAvatar;
    private TextView tvCurrentName;
    private TextView tvCurrentPhone;
    private EditText etName;
    private EditText etPhone;
    private Button btnSave;
    private Button btnCancel;
    
    private UserService userService;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        
        // Ẩn action bar để có giao diện fullscreen
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        // Khởi tạo services
        userService = new UserService(this);
        sessionManager = new SessionManager(this);
        
        // Khởi tạo views
        initViews();
        
        // Load thông tin user hiện tại
        loadUserInfo();
        
        // Xử lý events
        setupEventListeners();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvAvatar = findViewById(R.id.tv_avatar);
        tvCurrentName = findViewById(R.id.tv_current_name);
        tvCurrentPhone = findViewById(R.id.tv_current_phone);
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
    }
    
    private void loadUserInfo() {
        long userId = sessionManager.getUserId();
        if (userId > 0) {
            currentUser = userService.getById(userId);
            if (currentUser != null) {
                // Hiển thị thông tin hiện tại
                String name = currentUser.getName();
                String phone = currentUser.getPhone();
                
                // Hiển thị thông tin hiện tại
                if (name != null && !name.isEmpty()) {
                    tvCurrentName.setText(name);
                    etName.setText(name);
                    updateAvatar(name);
                } else {
                    tvCurrentName.setText("Chưa cập nhật");
                    etName.setText("");
                }
                
                if (phone != null && !phone.isEmpty()) {
                    tvCurrentPhone.setText(phone);
                    etPhone.setText(phone);
                } else {
                    tvCurrentPhone.setText("Chưa cập nhật");
                    etPhone.setText("");
                }
            }
        }
    }
    
    private void updateAvatar(String name) {
        String avatarText = getInitials(name);
        tvAvatar.setText(avatarText);
    }
    
    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "U";
        }
        
        String[] words = name.trim().split("\\s+");
        if (words.length == 1) {
            return words[0].substring(0, Math.min(2, words[0].length())).toUpperCase();
        } else {
            return (words[0].charAt(0) + "" + words[1].charAt(0)).toUpperCase();
        }
    }
    
    private void setupEventListeners() {
        // Nút quay lại
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
        
        // Nút lưu
        btnSave.setOnClickListener(v -> saveUserInfo());
        
        // Nút hủy
        btnCancel.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
        
        // Cập nhật avatar khi thay đổi tên
        etName.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                String name = s.toString().trim();
                if (!name.isEmpty()) {
                    updateAvatar(name);
                } else {
                    tvAvatar.setText("U");
                }
            }
        });
        
        // Xóa lỗi khi người dùng bắt đầu nhập
        etName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                etName.setError(null);
            }
        });
        
        etPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                etPhone.setError(null);
            }
        });
    }
    
    private void saveUserInfo() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        
        // Kiểm tra xem có thay đổi gì không
        boolean hasNameChange = !name.equals(currentUser.getName() != null ? currentUser.getName() : "");
        boolean hasPhoneChange = !phone.equals(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        
        if (!hasNameChange && !hasPhoneChange) {
            CustomToast.showWarning(this, "Không có thay đổi nào để lưu");
            return;
        }
        
        // Validation cho tên (nếu có thay đổi)
        if (hasNameChange && TextUtils.isEmpty(name)) {
            etName.setError("Vui lòng nhập tên");
            etName.requestFocus();
            return;
        }
        
        // Validation cho số điện thoại (nếu có thay đổi)
        if (hasPhoneChange) {
            if (TextUtils.isEmpty(phone)) {
                etPhone.setError("Vui lòng nhập số điện thoại");
                etPhone.requestFocus();
                return;
            }
            
            if (!isValidPhone(phone)) {
                etPhone.setError("Số điện thoại không hợp lệ");
                etPhone.requestFocus();
                return;
            }
        }
        
        // Cập nhật thông tin user
        if (currentUser != null) {
            if (hasNameChange) {
                currentUser.setName(name);
            }
            if (hasPhoneChange) {
                currentUser.setPhone(phone);
            }
            
            boolean success = userService.update(currentUser);
            if (success) {
                String message = "Cập nhật thông tin thành công!";
                if (hasNameChange && hasPhoneChange) {
                    message = "Cập nhật tên và số điện thoại thành công!";
                } else if (hasNameChange) {
                    message = "Cập nhật tên thành công!";
                } else if (hasPhoneChange) {
                    message = "Cập nhật số điện thoại thành công!";
                }
                CustomToast.showSuccess(this, message);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            } else {
                CustomToast.showError(this, "Có lỗi xảy ra khi cập nhật thông tin");
            }
        }
    }
    
    private boolean isValidPhone(String phone) {
        // Kiểm tra số điện thoại Việt Nam (10-11 số, bắt đầu bằng 0)
        return phone.matches("^0[0-9]{9,10}$");
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
