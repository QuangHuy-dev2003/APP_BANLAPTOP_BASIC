package com.example.quanlycuahanglaptop.service;

import android.content.Context;

import com.example.quanlycuahanglaptop.repository.SessionRepository;

public class SessionService {

    public static final long SESSION_TTL_7_DAYS = 7L * 24L * 60L * 60L * 1000L;

    private final SessionRepository sessionRepository;

    public SessionService(Context context) {
        this.sessionRepository = new SessionRepository(context);
    }

    public void startSession(long userId) {
        // Xoá phiên cũ của user rồi tạo mới để tránh rác
        sessionRepository.deleteByUserId(userId);
        sessionRepository.create(userId, SESSION_TTL_7_DAYS);
    }

    public boolean isSessionValid(long userId) {
        // đồng thời dọn rác phiên hết hạn
        sessionRepository.deleteExpired();
        return sessionRepository.hasValidSession(userId);
    }

    public void endSession(long userId) {
        sessionRepository.deleteByUserId(userId);
    }
}


