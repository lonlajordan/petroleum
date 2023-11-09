package com.petroleum.models;

import lombok.Getter;

@Getter
public class Notification {
    private String type = "";
    private String message = "";

    public void setType(String type) {
        this.type = type;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Notification() {
    }

    public Notification(String type, String message) {
        this.type = type;
        this.message = message;
    }
}
