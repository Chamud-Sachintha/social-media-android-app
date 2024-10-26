package com.example.socialmediaplatform.models;

// User model class to hold data for Firebase Database
public class User {
    public String userId;
    public String name;
    public String email;

    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }
}