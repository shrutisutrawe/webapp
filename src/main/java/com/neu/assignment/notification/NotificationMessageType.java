package com.neu.assignment.notification;

public enum NotificationMessageType {
    EMAIL_VERIFICATION_NOTIFICATION("EMAIL");

    private final String name;

    NotificationMessageType(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        // (otherName == null) check is not needed because name.equals(null) returns false
        return name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
