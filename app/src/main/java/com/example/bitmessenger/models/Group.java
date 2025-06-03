package com.example.bitmessenger.models;

import java.util.HashMap;
import java.util.Map;

public class Group {
    private String id;
    private String name;
    private String createdBy;
    private Map<String, Boolean> members;
    private long createdAt;
    private String imageUrl;

    public Group() {
        // Default constructor required for Firebase
        members = new HashMap<>();
    }

    public Group(String id, String name, String createdBy, Map<String, Boolean> members, long createdAt) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.members = members != null ? members : new HashMap<>();
        this.createdAt = createdAt;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Map<String, Boolean> getMembers() { return members; }
    public void setMembers(Map<String, Boolean> members) { this.members = members != null ? members : new HashMap<>(); }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}

