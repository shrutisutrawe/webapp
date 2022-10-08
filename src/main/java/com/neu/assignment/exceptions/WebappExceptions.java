package com.neu.assignment.exceptions;

public class WebappExceptions extends RuntimeException{
    private String message;

    public WebappExceptions(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    public WebappExceptions(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
