package com.example.calculat;

import static androidx.core.graphics.drawable.DrawableCompat.applyTheme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.mariuszgromada.math.mxparser.*;
import android.view.KeyEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.example.calculat.model.HistoryEntry;
import com.example.calculat.model.ThemeSetting;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TextView previousCalculation;
    private EditText display;
    private GestureDetector gestureDetector;
    private FirebaseFirestore db;
    private DocumentReference themeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        themeRef = db.collection("settings").document("theme");

        // Загружаем тему из Firestore
        themeRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("isDarkTheme")) {
                boolean isDarkTheme = documentSnapshot.getBoolean("isDarkTheme");
                applyTheme(isDarkTheme);
            }
        });

        setContentView(R.layout.activity_main);

        previousCalculation = findViewById(R.id.previousCalculationView);
        display = findViewById(R.id.displayEditText);

        display.setShowSoftInputOnFocus(false);
        display.setText("0");

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX < 0) {
                        backspaceOnSwipe();
                    }
                    return true;
                }
                return false;
            }
        });

        display.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    public void toggleTheme(View view) {
        boolean isDarkTheme = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        boolean newTheme = !isDarkTheme;

        applyTheme(newTheme);

        themeRef.set(new ThemeSetting(newTheme))
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Theme updated in Firestore"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating theme", e));
    }

    private void applyTheme(boolean isDarkTheme) {
        AppCompatDelegate.setDefaultNightMode(
                isDarkTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void backspaceOnSwipe() {
        String text = display.getText().toString();
        int cursorPos = display.getSelectionStart();

        if (text.length() > 1 && cursorPos > 0) {
            StringBuilder newText = new StringBuilder(text);
            newText.delete(cursorPos - 1, cursorPos);
            display.setText(newText.toString());
            display.setSelection(Math.max(cursorPos - 1, 0));
        } else {
            display.setText("0");
            display.setSelection(1);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            incrementValue();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            decrementValue();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void incrementValue() {
        String text = display.getText().toString();
        try {
            double value = Double.parseDouble(text);
            value++;
            display.setText(String.valueOf(value));
            display.setSelection(display.getText().length());
        } catch (NumberFormatException e) {
            display.setText("0");
            display.setSelection(1);
        }
    }

    private void decrementValue() {
        String text = display.getText().toString();
        try {
            double value = Double.parseDouble(text);
            value--;
            display.setText(String.valueOf(value));
            display.setSelection(display.getText().length());
        } catch (NumberFormatException e) {
            display.setText("0");
            display.setSelection(1);
        }
    }
    private void updateText(String strToAdd) {
        String oldStr = display.getText().toString();
        int cursorPos = display.getSelectionStart();

        if (oldStr.equals("0")) {
            if (strToAdd.matches("[0-9]") || strToAdd.equals("-") || strToAdd.equals("sin(") || strToAdd.equals("cos(") || strToAdd.equals("tan(") || strToAdd.equals("arcsin(") || strToAdd.equals("arccos(") || strToAdd.equals("arctan(")
                || strToAdd.equals("ln(") || strToAdd.equals("lg(") || strToAdd.equals("sqrt(") || strToAdd.equals("abs(") || strToAdd.equals("pi(") || strToAdd.equals("e")) {
                display.setText(strToAdd);
            } else {
                display.setText("0" + strToAdd);
            }
        } else {
            String leftStr = oldStr.substring(0, cursorPos);
            String rightStr = oldStr.substring(cursorPos);
            display.setText(String.format("%s%s%s", leftStr, strToAdd, rightStr));
        }
        display.setSelection(display.getText().length());
    }

    private void addOperator(String operator) {
        String text = display.getText().toString();
        if (text.endsWith("+") || text.endsWith("-") || text.endsWith("×") || text.endsWith("÷")) {
            display.setText(text.substring(0, text.length() - 1) + operator);
        } else {
            updateText(operator);
        }
    }

    public void decimalBTNPush(View view) {
        String text = display.getText().toString();
        String[] parts = text.split("[+\\-*/]");
        String lastNumber = parts[parts.length - 1];

        if (!lastNumber.contains(".")) {
            updateText(".");
        }
    }

    public void equalBTNPush(View view) {
        String userExp = display.getText().toString();

        previousCalculation.setText(userExp);

        userExp = userExp.replaceAll(getResources().getString(R.string.divideText), "/");
        userExp = userExp.replaceAll(getResources().getString(R.string.multiplyText), "*");

        Expression exp = new Expression(userExp);
        String result = String.valueOf(exp.calculate());

        display.setText(result);
        display.setSelection(result.length());

        saveHistory(userExp, result);
    }

    private void saveHistory(String expression, String result) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> historyEntry = new HashMap<>();
        historyEntry.put("expression", expression);
        historyEntry.put("result", result);
        historyEntry.put("timestamp", System.currentTimeMillis()); // Сортировка по времени

        db.collection("history")
                .add(historyEntry)
                .addOnSuccessListener(documentReference ->
                        Log.d("Firebase", "История сохранена: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        Log.e("Firebase", "Ошибка сохранения в Firestore", e));
    }


    public void openHistory(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    public void backspaceBTNPush(View view) {
        int cursorPos = display.getSelectionStart();
        int textLen = display.getText().length();

        if (textLen > 1) {
            SpannableStringBuilder selection = (SpannableStringBuilder) display.getText();
            selection.replace(cursorPos - 1, cursorPos, "");
            display.setText(selection);
            display.setSelection(cursorPos - 1);
        } else {
            display.setText("0");
            display.setSelection(1);
        }
    }

    public void zeroBTNPush(View view){
        updateText(getResources().getString(R.string.zeroText));
    }

    public void oneBTNPush(View view){
        updateText(getResources().getString(R.string.oneText));
    }

    public void twoBTNPush(View view){
        updateText(getResources().getString(R.string.twoText));
    }

    public void threeBTNPush(View view){
        updateText(getResources().getString(R.string.threeText));
    }

    public void fourBTNPush(View view){
        updateText(getResources().getString(R.string.fourText));
    }

    public void fiveBTNPush(View view){
        updateText(getResources().getString(R.string.fiveText));
    }

    public void sixBTNPush(View view){
        updateText(getResources().getString(R.string.sixText));
    }

    public void sevenBTNPush(View view){
        updateText(getResources().getString(R.string.sevenText));
    }

    public void eightBTNPush(View view){
        updateText(getResources().getString(R.string.eightText));
    }

    public void nineBTNPush(View view){
        updateText(getResources().getString(R.string.nineText));
    }

    public void addBTNPush(View view){
        addOperator(getResources().getString(R.string.addText));
    }

    public void subtractBTNPush(View view){
        addOperator(getResources().getString(R.string.subtractText));
    }

    public void multiplyBTNPush(View view){
        addOperator(getResources().getString(R.string.multiplyText));
    }

    public void divideBTNPush(View view){
        addOperator(getResources().getString(R.string.divideText));
    }

    public void clearBTNPush(View view){
        display.setText("0");
        display.setSelection(1);
        previousCalculation.setText("");
    }

    public void parOpenBTNPush(View view){
        updateText(getResources().getString(R.string.parenthesesOpenText));
    }

    public void parCloseBTNPush(View view){
        updateText(getResources().getString(R.string.parenthesesCloseText));
    }


    public void trigSinBTNPush(View view){
        updateText("sin(");
    }

    public void trigCosBTNPush(View view){
        updateText("cos(");
    }

    public void trigTanBTNPush(View view){
        updateText("tan(");
    }

    public void trigArcSinBTNPush(View view){
        updateText("arcsin(");
    }

    public void trigArcCosBTNPush(View view){
        updateText("arccos(");
    }

    public void trigArcTanBTNPush(View view){
        updateText("arctan(");
    }

    public void naturalLogBTNPush(View view){
        updateText("ln(");
    }

    public void logBTNPush(View view){
        updateText("lg(");
    }

    public void sqrtBTNPush(View view){
        updateText("sqrt(");
    }

    public void absBTNPush(View view){
        updateText("abs(");
    }

    public void piBTNPush(View view){
        updateText("pi");
    }

    public void eBTNPush(View view){
        updateText("e");
    }

    public void xSquaredBTNPush(View view){
        updateText("^(2)");
    }

    public void xPowerYBTNPush(View view){
        updateText("^(");
    }

    public void primeBTNPush(View view){
        updateText("rad(");
    }
}