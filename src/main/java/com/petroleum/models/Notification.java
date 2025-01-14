package com.petroleum.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Notification {
    private String type = "";
    private String message = "";

    public Notification() {
    }

    public Notification(String type, String message) {
        this.type = type;
        this.message = message;
    }
}
