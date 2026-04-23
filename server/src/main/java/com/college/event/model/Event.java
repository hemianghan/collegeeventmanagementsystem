package com.college.event.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String location;
    
    @Column(length = 2000)
    private String description;
    
    @Column(length = 3000)
    private String rulesAndGuidelines;
    
    private String date;
    private String registrationDeadline; // Registration deadline date (YYYY-MM-DD format)
    
    @Column(length = 1000)
    private String imageUrl;
    
    private String category;
    private Double registrationFee; // Registration fee for the event

    // Constructors
    public Event() {
    }

    public Event(String title, String location, String description, String category, String date, String imageUrl) {
        this.title = title;
        this.location = location;
        this.description = description;
        this.category = category;
        this.date = date;
        this.imageUrl = imageUrl;
        this.registrationFee = 100.0; // Default registration fee
        // Set registration deadline to 3 days before event date by default
        this.registrationDeadline = calculateDefaultDeadline(date);
    }

    // Helper method to calculate default deadline (3 days before event)
    private String calculateDefaultDeadline(String eventDate) {
        try {
            if (eventDate != null && eventDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                String[] parts = eventDate.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                
                // Simple calculation: subtract 3 days
                day -= 3;
                if (day <= 0) {
                    month -= 1;
                    if (month <= 0) {
                        year -= 1;
                        month = 12;
                    }
                    // Approximate days in month (simplified)
                    day += 30;
                }
                
                return String.format("%04d-%02d-%02d", year, month, day);
            }
        } catch (Exception e) {
            // If calculation fails, return event date minus 3 days as string
        }
        return eventDate; // Fallback to event date
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRulesAndGuidelines() { return rulesAndGuidelines; }
    public void setRulesAndGuidelines(String rulesAndGuidelines) { this.rulesAndGuidelines = rulesAndGuidelines; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(String registrationDeadline) { this.registrationDeadline = registrationDeadline; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getRegistrationFee() { return registrationFee; }
    public void setRegistrationFee(Double registrationFee) { this.registrationFee = registrationFee; }
}
