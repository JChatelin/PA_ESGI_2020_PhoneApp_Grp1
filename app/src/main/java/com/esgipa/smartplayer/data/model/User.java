package com.esgipa.smartplayer.data.model;

import androidx.annotation.NonNull;

public class User {
    @NonNull
    private String name;
    @NonNull
    private String username;
    @NonNull
    private String email;
    @NonNull
    private String password;

    private String role;

    public User(@NonNull String name, @NonNull String username, @NonNull String email) {
        this.name = name;
        this.username = username;
        this.email = email;
    }

    public User(@NonNull String name, @NonNull String username, @NonNull String email,
                @NonNull String password, @NonNull String role) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }
}

