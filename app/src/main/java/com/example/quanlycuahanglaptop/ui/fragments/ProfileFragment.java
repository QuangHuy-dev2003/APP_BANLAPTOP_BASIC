package com.example.quanlycuahanglaptop.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.app.LoginActivity;
import com.example.quanlycuahanglaptop.domain.User;
import com.example.quanlycuahanglaptop.service.AuthService;
import com.example.quanlycuahanglaptop.service.UserService;
import com.example.quanlycuahanglaptop.ui.userinfo.UserInfoActivity;
import com.example.quanlycuahanglaptop.ui.order.OrderHistoryActivity;
import com.example.quanlycuahanglaptop.utils.SessionManager;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    // Views và services
    private TextView tvAvatar;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvUserPhone;
    private LinearLayout optionUserInfo;
    private LinearLayout optionOrderHistory;
    private Button btnLogout;
    private AuthService authService;
    private SessionManager sessionManager;
    private UserService userService;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Khởi tạo views
        tvAvatar = view.findViewById(R.id.tv_avatar);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvUserPhone = view.findViewById(R.id.tv_user_phone);
        optionUserInfo = view.findViewById(R.id.option_user_info);
        optionOrderHistory = view.findViewById(R.id.option_order_history);
        btnLogout = view.findViewById(R.id.btnLogout);
        
        // Khởi tạo services
        authService = new AuthService(requireContext());
        sessionManager = new SessionManager(requireContext());
        userService = new UserService(requireContext());

        // Load thông tin user
        loadUserInfo();
        
        // Setup click listeners
        setupClickListeners();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload thông tin user khi quay lại từ UserInfoActivity
        loadUserInfo();
    }
    
    private void loadUserInfo() {
        boolean isLoggedIn = authService.isLoggedIn();
        if (isLoggedIn) {
            // Lấy user ID từ session
            long userId = sessionManager.getUserId();
            
            if (userId > 0) {
                // Load thông tin user đầy đủ từ database
                User user = userService.getById(userId);
                
                if (user != null) {
                    // Hiển thị thông tin user từ database
                    String userName = user.getName();
                    String userEmail = user.getEmail();
                    String userPhone = user.getPhone();
                    
                    if (userName != null && !userName.isEmpty()) {
                        tvUserName.setText(userName);
                        // Tạo avatar từ 2 chữ đầu của tên
                        String avatarText = getInitials(userName);
                        tvAvatar.setText(avatarText);
                    } else {
                        tvUserName.setText("Chưa cập nhật tên");
                        tvAvatar.setText("U");
                    }
                    
                    if (userEmail != null && !userEmail.isEmpty()) {
                        tvUserEmail.setText(userEmail);
                    } else {
                        tvUserEmail.setText("Chưa cập nhật email");
                    }
                    
                    if (userPhone != null && !userPhone.isEmpty()) {
                        tvUserPhone.setText(userPhone);
                    } else {
                        tvUserPhone.setText("Chưa cập nhật số điện thoại");
                    }
                } else {
                    // User không tồn tại trong database
                    showUserNotFoundMessage();
                }
            } else {
                // Không có user ID trong session
                showUserNotFoundMessage();
            }
            
            // Hiển thị các options và button logout
            optionUserInfo.setVisibility(View.VISIBLE);
            optionOrderHistory.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.VISIBLE);
            
        } else {
            // Ẩn tất cả thông tin user
            optionUserInfo.setVisibility(View.GONE);
            optionOrderHistory.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);
            
            // Hiển thị AlertDialog ngay khi người dùng chưa đăng nhập
            new AlertDialog.Builder(requireContext())
                    .setTitle("Yêu cầu đăng nhập")
                    .setMessage("Vui lòng đăng nhập để xem hồ sơ của bạn.")
                    .setPositiveButton("Đăng nhập", (dialog, which) -> {
                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton("Đóng", null)
                    .show();
        }
    }
    
    private void setupClickListeners() {
        // Xử lý click events
        optionUserInfo.setOnClickListener(v -> {
            // Mở UserInfoActivity để chỉnh sửa thông tin user
            Intent intent = new Intent(requireContext(), UserInfoActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        optionOrderHistory.setOnClickListener(v -> {
            // Mở OrderHistoryActivity
            long userId = sessionManager.getUserId();
            Intent intent = new Intent(requireContext(), OrderHistoryActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        btnLogout.setOnClickListener(v -> {
            // Đăng xuất qua AuthService để đóng session
            authService.logout();
            
            // Điều hướng về Login và xoá back stack
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.putExtra("SHOW_LOGOUT_SUCCESS", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
    
    /**
     * Tạo avatar text từ 2 chữ đầu của tên
     */
    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "U";
        }
        
        String[] words = name.trim().split("\\s+");
        if (words.length == 1) {
            // Nếu chỉ có 1 từ, lấy 2 ký tự đầu
            return words[0].substring(0, Math.min(2, words[0].length())).toUpperCase();
        } else {
            // Nếu có nhiều từ, lấy chữ cái đầu của 2 từ đầu tiên
            return (words[0].charAt(0) + "" + words[1].charAt(0)).toUpperCase();
        }
    }
    
    /**
     * Hiển thị toast message
     */
    private void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Hiển thị thông báo khi không tìm thấy thông tin user
     */
    private void showUserNotFoundMessage() {
        if (getContext() != null) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Thông tin người dùng")
                    .setMessage("Không tìm thấy thông tin người dùng. Vui lòng cập nhật thông tin trong phần 'Thông tin người dùng'.")
                    .setPositiveButton("Đồng ý", null)
                    .show();
        }
    }
}
