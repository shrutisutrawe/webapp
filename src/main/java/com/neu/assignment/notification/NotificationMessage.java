package com.neu.assignment.notification;

public class NotificationMessage {
    private String username;
    private String oneTimeVerificationToken;
    private NotificationMessageType emailVerificationNotification;
    private String firstName;

    public NotificationMessage(String username,
                               String firstName,
                               String oneTimeVerificationToken,
                               NotificationMessageType emailVerificationNotification) {

        this.username = username;
        this.firstName = firstName;
        this.oneTimeVerificationToken = oneTimeVerificationToken;
        this.emailVerificationNotification = emailVerificationNotification;
    }

    public String getUsername() {
        return username;
    }

    public String getOneTimeVerificationToken() {
        return oneTimeVerificationToken;
    }

    public NotificationMessageType getEmailVerificationNotification() {
        return emailVerificationNotification;
    }

    public String getFirstName() {
        return firstName;
    }
}
