package com.example.bitmessenger.models;

import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String id;
    private String email;
    private String name;
    private String status;
    private String imageUrl;
    private String token;
    private long lastSeen;
    private boolean online;

    public User() {
        // Required empty constructor for Firebase
    }

    public User(String id, String email, String name, String status) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.status = status;
        this.lastSeen = System.currentTimeMillis();
        this.online = false;
    }

    // Getters and Setters
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

    public long getLastSeen() { return lastSeen; }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("email", email);
        result.put("name", name);
        result.put("status", status);
        result.put("imageUrl", imageUrl);
        result.put("token", token);
        result.put("lastSeen", lastSeen);
        result.put("online", online);
        return result;
    }
} 