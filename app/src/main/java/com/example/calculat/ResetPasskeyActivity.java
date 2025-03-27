package com.example.calculat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calculat.passkey.KeystoreHelper;
import com.example.calculat.passkey.PasskeyManager;

public class ResetPasskeyActivity extends AppCompatActivity {
    private EditText newPasskeyInput;
    private Button savePasskeyButton;
    private PasskeyManager keystoreHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_passkey);

        newPasskeyInput = findViewById(R.id.newPasskeyInput);
        savePasskeyButton = findViewById(R.id.savePasskeyButton);
        keystoreHelper = new PasskeyManager(this);

        savePasskeyButton.setOnClickListener(v -> {
            String newPasskey = newPasskeyInput.getText().toString().trim();
            if (!newPasskey.isEmpty()) {
                keystoreHelper.savePasskey(newPasskey);
                Toast.makeText(this, "Passkey обновлен", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Введите новый Passkey", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

