package com.example.quanlycuahanglaptop.service;

import android.content.Context;

import com.example.quanlycuahanglaptop.domain.Role;
import com.example.quanlycuahanglaptop.domain.User;
import com.example.quanlycuahanglaptop.repository.UserRepository;
import com.example.quanlycuahanglaptop.utils.PasswordHasher;
import com.example.quanlycuahanglaptop.utils.SessionManager;

public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final SessionManager sessionManager;
    private final SessionService sessionService;

    public AuthService(Context context) {
        this.userRepository = new UserRepository(context);
        this.userService = new UserService(context);
        this.sessionManager = new SessionManager(context);
        this.sessionService = new SessionService(context);
    }

    public long register(String name, String email, String password, String phone) {
        return userService.createUser(name, email, password, phone, Role.USER);
    }

    public User login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("Sai email hoặc mật khẩu");
        }
        String hashed = PasswordHasher.sha256(password);
        if (!hashed.equals(user.getPassword())) {
            throw new IllegalArgumentException("Sai email hoặc mật khẩu");
        }
        // Lưu thông tin user tối thiểu và tạo session DB 7 ngày
        sessionManager.saveUser(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole().name());
        sessionService.startSession(user.getId());
        return user;
    }

    public void logout() {
        long userId = sessionManager.getUserId();
        if (userId > 0) {
            sessionService.endSession(userId);
        }
        sessionManager.clear();
    }

    public boolean isLoggedIn() {
        long userId = sessionManager.getUserId();
        return userId > 0 && sessionService.isSessionValid(userId);
    }
}


