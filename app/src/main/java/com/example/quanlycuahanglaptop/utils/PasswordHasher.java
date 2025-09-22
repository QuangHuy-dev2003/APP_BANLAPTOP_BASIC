package com.example.quanlycuahanglaptop.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordHasher {

    private PasswordHasher() {}

    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}


