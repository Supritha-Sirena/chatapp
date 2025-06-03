package com.example.bitmessenger.models;

public class GroupMessage {
    private String id;
    private String groupId;
    private String senderId;
    private String message;
    private long timestamp;
    private String status; // sent, delivered, read

    public GroupMessage() {
        // Default constructor required for Firebase
    }

    public GroupMessage(String id, String groupId, String senderId, String message, long timestamp, String status) {
        this.id = id;
        this.groupId = groupId;
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}