package com.petroleum.enums;

public enum Role {
    ROLE_ADMIN("Administrateur"),
    ROLE_DIRECTOR("Directeur Général"),
    ROLE_OPERATING_OFFICER("Chef d'exploitation"),
    ROLE_DISPATCHER("Dispacheur");

    private final String displayValue;

    Role(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
