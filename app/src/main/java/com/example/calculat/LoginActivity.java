package com.example.calculat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.calculat.passkey.PasskeyManager;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private PasskeyManager passkeyManager;
    private EditText passkeyInput;
    private Button btnLogin, btnUseBiometric, btnForgotPasskey;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        passkeyManager = new PasskeyManager(this);
        passkeyInput = findViewById(R.id.passkeyInput);
        btnLogin = findViewById(R.id.btnLogin);
        btnUseBiometric = findViewById(R.id.btnUseBiometric);
        btnForgotPasskey = findViewById(R.id.btnForgotPasskey);

        btnLogin.setOnClickListener(view -> checkPasskey());
        btnUseBiometric.setOnClickListener(view -> authenticateWithBiometrics());
        btnForgotPasskey.setOnClickListener(view -> resetPasskey());

        Button biometricButton = findViewById(R.id.btnUseBiometric);
        biometricButton.setOnClickListener(view -> authenticateWithBiometrics());

        setupBiometricPrompt();

    }

    private void checkPasskey() {
        String enteredPasskey = passkeyInput.getText().toString().trim();
        String savedPasskey = passkeyManager.getPasskey();

        if (savedPasskey == null) {
            Toast.makeText(this, "Passkey не установлен", Toast.LENGTH_SHORT).show();
            return;
        }

        if (enteredPasskey.equals(savedPasskey)) {
            Toast.makeText(this, "Вход выполнен", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Неверный Passkey", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d("BiometricAuth", "Аутентификация успешна!");
                // TODO: Перенаправить пользователя на следующий экран
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.e("BiometricAuth", "Ошибка аутентификации: " + errString);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.e("BiometricAuth", "Не удалось распознать отпечаток!");
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Вход с помощью биометрии")
                .setSubtitle("Приложите палец к сканеру или используйте распознавание лица")
                .setNegativeButtonText("Отмена") // Должен быть указан, если device credential НЕ используется
                .build();
    }

    private void authenticateWithBiometrics() {
        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(promptInfo);
        } else {
            Log.e("BiometricAuth", "Биометрия не поддерживается или не настроена!");
        }
    }


    private void resetPasskey() {
        Intent intent = new Intent(this, ResetPasskeyActivity.class);
        startActivity(intent);
    }
}
