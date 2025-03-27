package com.example.calculat.passkey;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class PasskeyManager {
    private static final String PREFS_NAME = "SecurePasskeyPrefs";
    private static final String PASSKEY_KEY = "passkey";
    private SharedPreferences sharedPreferences;

    public PasskeyManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public void savePasskey(String passkey) {
        sharedPreferences.edit().putString(PASSKEY_KEY, passkey).apply();
    }

    public String getPasskey() {
        return sharedPreferences.getString(PASSKEY_KEY, null);
    }

    public boolean isPasskeySet() {
        return sharedPreferences.contains(PASSKEY_KEY);
    }
}
