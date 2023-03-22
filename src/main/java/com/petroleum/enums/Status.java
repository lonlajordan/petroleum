package com.petroleum.enums;

public enum Status {
    PENDING("En attente du chef d'exploitation"),
    WAITING("En attente du directeur général"),
    APPROVED("Approuvé"),
    REJECTED("Rejété");

    private final String displayValue;

    Status(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
