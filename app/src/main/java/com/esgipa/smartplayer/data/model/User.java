package com.esgipa.smartplayer.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class User {
    @NonNull
    private String name;
    @NonNull
    private String username;
    @NonNull
    private String email;
    @NonNull
    private String password;

    private boolean logged;

    @Nullable
    private String authToken;

    private String role;

    public User(@NonNull String name, @NonNull String username, @NonNull String email) {
        this.name = name;
        this.username = username;
        this.email = email;
    }

    public User(@Nullable String authToken, @NonNull String name, @NonNull String username, @NonNull String email, boolean logged) {
        this(name, username, email);
        this.authToken = authToken;
        this.logged = logged;
    }

    public User(@NonNull String name, @NonNull String username, @NonNull String email,
                @NonNull String password, @NonNull String role) {
        this(name, username, email);
        this.password = password;
        this.role = role;
    }

    public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    @Nullable
    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(@Nullable String authToken) {
        this.authToken = authToken;
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

