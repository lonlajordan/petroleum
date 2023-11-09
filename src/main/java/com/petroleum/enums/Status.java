package com.petroleum.enums;

import lombok.Getter;

@Getter
public enum Status {
    PENDING("En attente du chef d'exploitation"),
    WAITING("En attente du directeur général"),
    APPROVED("Approuvé"),
    REJECTED("Rejeté");

    private final String displayValue;

    Status(String displayValue) {
        this.displayValue = displayValue;
    }

}
