package com.example.quanlycuahanglaptop;

import static org.junit.Assert.assertEquals;

import com.example.quanlycuahanglaptop.utils.PasswordHasher;

import org.junit.Test;

public class PasswordHasherTest {

    @Test
    public void sha256_hash_ok() {
        String result = PasswordHasher.sha256("Admin123");
        assertEquals("3a6548e1b0f87a026d908f0f92c0b6dbd58accf5fda5aee1b4eafc1f3c5f1454", result);
    }
}


