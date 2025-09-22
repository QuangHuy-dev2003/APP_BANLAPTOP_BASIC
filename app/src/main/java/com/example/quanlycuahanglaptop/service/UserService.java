package com.example.quanlycuahanglaptop.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlycuahanglaptop.data.AppDatabase;
import com.example.quanlycuahanglaptop.domain.Role;
import com.example.quanlycuahanglaptop.domain.User;
import com.example.quanlycuahanglaptop.repository.CartItemRepository;
import com.example.quanlycuahanglaptop.repository.UserRepository;
import com.example.quanlycuahanglaptop.utils.PasswordHasher;

import java.util.List;

public class UserService {

    private final AppDatabase appDatabase;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;

    public UserService(Context context) {
        this.appDatabase = AppDatabase.getInstance(context);
        this.userRepository = new UserRepository(context);
        this.cartItemRepository = new CartItemRepository(context);
    }

    public long createUser(String name, String email, String rawPassword, String phone, Role role) {
        validateName(name);
        validateEmail(email);
        validatePassword(rawPassword);
        validatePhone(phone);
        if (userRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        String hashed = PasswordHasher.sha256(rawPassword);
        User user = new User(null, name, email, hashed, phone, role == null ? Role.USER : role);
        return userRepository.create(user);
    }

    public int updateUser(long id, String name, String email, String newPasswordOrNull, String phone, Role role) {
        User existing = userRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Người dùng không tồn tại");
        }
        if (name != null) validateName(name);
        if (email != null) validateEmail(email);
        if (newPasswordOrNull != null) validatePassword(newPasswordOrNull);
        if (phone != null) validatePhone(phone);
        if (email != null) {
            User byEmail = userRepository.findByEmail(email);
            if (byEmail != null && byEmail.getId() != id) {
                throw new IllegalArgumentException("Email đã được sử dụng");
            }
        }
        existing.setName(name != null ? name : existing.getName());
        existing.setEmail(email != null ? email : existing.getEmail());
        existing.setPhone(phone != null ? phone : existing.getPhone());
        if (newPasswordOrNull != null && !newPasswordOrNull.isEmpty()) {
            existing.setPassword(PasswordHasher.sha256(newPasswordOrNull));
        }
        if (role != null) existing.setRole(role);
        return userRepository.update(existing);
    }

    public int deleteUserCascadeButKeepOrders(long userId) {
        // Xoá dữ liệu liên quan: giỏ hàng, các bản ghi phụ thuộc khác; Order giữ lại (fk ON DELETE SET NULL)
        SQLiteDatabase db = appDatabase.getConnection();
        db.beginTransaction();
        try {
            cartItemRepository.deleteByUserId(userId);
            int affected = userRepository.deleteById(userId);
            db.setTransactionSuccessful();
            return affected;
        } finally {
            db.endTransaction();
        }
    }

    public User getById(long id) { return userRepository.findById(id); }

    public List<User> getAll() { return userRepository.findAll(); }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên không được để trống");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Tên quá dài");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống");
        }
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        if (cleanPhone.length() < 10 || cleanPhone.length() > 11) {
            throw new IllegalArgumentException("Số điện thoại phải có 10-11 chữ số");
        }
    }

    public com.example.quanlycuahanglaptop.domain.User getUserById(long userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * Cập nhật thông tin user (chỉ tên và số điện thoại)
     */
    public boolean update(User user) {
        try {
            int result = userRepository.update(user);
            return result > 0;
        } catch (Exception e) {
            return false;
        }
    }
}


