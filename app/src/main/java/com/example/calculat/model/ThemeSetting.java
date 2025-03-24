package com.example.calculat.model;

public class ThemeSetting {
    private boolean isDarkTheme;

    public ThemeSetting() {}

    public ThemeSetting(boolean isDarkTheme) {
        this.isDarkTheme = isDarkTheme;
    }

    // Геттеры и сеттеры
    public boolean isDarkTheme() {
        return isDarkTheme;
    }

    public void setDarkTheme(boolean darkTheme) {
        isDarkTheme = darkTheme;
    }
}

