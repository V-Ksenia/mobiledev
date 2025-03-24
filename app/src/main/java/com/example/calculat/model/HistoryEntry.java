package com.example.calculat.model; // Обнови пакет, если другое имя

public class HistoryEntry {
    private String expression;
    private String result;

    public HistoryEntry() {} // Пустой конструктор нужен для Firebase

    public HistoryEntry(String expression, String result) {
        this.expression = expression;
        this.result = result;
    }

    public String getExpression() { return expression; }
    public String getResult() { return result; }
}
