package com.contacts.dto;

public class UserSummary {

    private String username;
    private String email;

    public UserSummary(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
}
