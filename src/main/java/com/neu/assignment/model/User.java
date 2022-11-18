package com.neu.assignment.model;

public class User {
    private String id;
    private String username;
    private String first_name;
    private String last_name;
    private String password;
    private String account_created;
    private String account_updated;
    private String verified;
    public static String USER_ACCOUNT_NOT_VERIFIED_STATUS = "NOT_VERIFIED";
    public static String USER_ACCOUNT_VERIFIED_STATUS = "VERIFIED";

    public User() {
    }

    public User(User user) {
        this.id = user.id;
        this.first_name = user.first_name;
        this.last_name = user.last_name;
        this.username = user.username;
        this.password = user.password;
        this.account_created = user.account_created;
        this.account_updated = user.account_updated;
        this.verified = verified;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccount_updated() {
        return account_updated;
    }

    public void setAccount_updated(String account_updated) {
        this.account_updated = account_updated;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccount_created() {
        return account_created;
    }

    public void setAccount_created(String account_created) {
        this.account_created = account_created;
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }
}
