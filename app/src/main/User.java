package com.example.bitmessenger.models;

public class User {
    private String id;
    private String email;
    private String name;
    private String status;
    private String imageUrl;
    private String token;

    public User() {

    }

    public User(String id, String email, String name, String status) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.status = status;
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}