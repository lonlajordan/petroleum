package com.petroleum.models;

public class Notification {
    private String type = "";
    private String message = "";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
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
