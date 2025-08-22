package com.example.customer_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class AdminPinStore {
    private static final String FILE = "admin_secure_prefs";
    private static final String K_HASH = "hash";
    private static final String K_SALT = "salt";
    private final SharedPreferences prefs;

    public AdminPinStore(Context ctx) {
        SharedPreferences p;
        try {
            MasterKey key = new MasterKey.Builder(ctx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
            p = EncryptedSharedPreferences.create(
                    ctx, FILE, key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (Throwable t) {
            p = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE);
        }
        prefs = p;
    }

    public boolean hasPin() { return prefs.contains(K_HASH) && prefs.contains(K_SALT); }

    public void setPin(String pin) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        String h = hash(pin, salt);
        prefs.edit()
                .putString(K_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                .putString(K_HASH, h)
                .apply();
    }

    public boolean verify(String pin) {
        String s64 = prefs.getString(K_SALT, null);
        String stored = prefs.getString(K_HASH, null);
        if (s64 == null || stored == null) return false;
        byte[] salt = Base64.decode(s64, Base64.NO_WRAP);
        String h = hash(pin, salt);
        return MessageDigest.isEqual(stored.getBytes(StandardCharsets.UTF_8),
                h.getBytes(StandardCharsets.UTF_8));
    }

    private static String hash(String pin, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            md.update(pin.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(md.digest(), Base64.NO_WRAP);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
