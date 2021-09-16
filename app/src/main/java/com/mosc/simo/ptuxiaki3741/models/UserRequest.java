package com.mosc.simo.ptuxiaki3741.models;


import com.mosc.simo.ptuxiaki3741.models.entities.User;

public class UserRequest {
    private boolean isRequest;
    private User user;
    public UserRequest(User user,boolean isRequest) {
        this.isRequest = isRequest;
        this.user = user;
    }

    public boolean isRequest() {
        return isRequest;
    }
    public User getUser() {
        return user;
    }

    public void setRequest(boolean request) {
        isRequest = request;
    }
    public void setUser(User user) {
        this.user = user;
    }
}
