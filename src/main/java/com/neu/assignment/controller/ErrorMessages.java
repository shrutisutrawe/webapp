package com.neu.assignment.controller;

public class ErrorMessages {
    private String error_message;

    public ErrorMessages(String message) {
        this.error_message = message;
    }

    public String getMessage() {
        return error_message;
    }

    public void setMessage(String message) {
        this.error_message = message;
    }
}
