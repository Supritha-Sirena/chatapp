package com.example.bitmessenger.models;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private String sender;
    private String receiver;
    private String message;
    private long timestamp;
    private String status; // sent, delivered, read
    private List<String> readBy; // List of user IDs who read the message

    public Chat() {
        // Default constructor required for Firebase
        this.readBy = new ArrayList<>();
    }

    public Chat(String sender, String receiver, String message, long timestamp, String status) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
        this.readBy = new ArrayList<>();
    }

    // Getters and setters
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getReadBy() { return readBy; }
    public void setReadBy(List<String> readBy) { this.readBy = readBy; }
}
