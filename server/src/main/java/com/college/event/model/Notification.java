package com.college.event.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;
    private String userName;
    private String userEmail;
    
    private String title;
    private String message;
    private String type; // "EVENT_APPROVED", "CERTIFICATE_AVAILABLE", "EVENT_STARTING_SOON"
    
    private Long relatedEventId;
    private String relatedEventTitle;
    
    private Boolean isRead;
    private LocalDateTime createdDate;
    private LocalDateTime readDate;

    // Constructors
    public Notification() {
        this.createdDate = LocalDateTime.now();
        this.isRead = false;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Long getRelatedEventId() { return relatedEventId; }
    public void setRelatedEventId(Long relatedEventId) { this.relatedEventId = relatedEventId; }
    
    public String getRelatedEventTitle() { return relatedEventTitle; }
    public void setRelatedEventTitle(String relatedEventTitle) { this.relatedEventTitle = relatedEventTitle; }
    
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getReadDate() { return readDate; }
    public void setReadDate(LocalDateTime readDate) { this.readDate = readDate; }
}
