package com.campuscare.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String senderId;
    private String recipientId;
    private String message;
    private boolean isRead;
    private String timestamp;

    public Notification(String senderId, String recipientId, String message) {
        this.id = UUID.randomUUID().toString();
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.message = message;
        this.isRead = false;
        this.timestamp = LocalDateTime.now().toString();
    }
    
    // Constructor for loading from JSON
    public Notification(String id, String senderId, String recipientId, String message, boolean isRead, String timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.message = message;
        this.isRead = isRead;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getRecipientId() { return recipientId; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
    public String getTimestamp() { return timestamp; }
    
    public void setRead(boolean read) { isRead = read; }
}