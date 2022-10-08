package com.neu.assignment.controller.getUser;

import com.neu.assignment.model.User;

public class GetUserResponse {
    private String id;
    private String first_name;
    private String last_name;
    private String username;
    private String account_created;
    private String account_updated;

    public GetUserResponse(User user) {
        this.id = user.getId();
        this.first_name = user.getFirst_name();
        this.last_name = user.getLast_name();
        this.username = user.getUsername();
        this.account_created = user.getAccount_created();
        this.account_updated = user.getAccount_updated();
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

    public String getAccount_created() {
        return account_created;
    }

    public void setAccount_created(String account_created) {
        this.account_created = account_created;
    }

    public String getAccount_updated() {
        return account_updated;
    }

    public void setAccount_updated(String account_updated) {
        this.account_updated = account_updated;
    }
}
