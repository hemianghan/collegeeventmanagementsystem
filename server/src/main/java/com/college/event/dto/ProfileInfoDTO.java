package com.college.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public class ProfileInfoDTO {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @Email(message = "Valid email is required")
    private String email;
    
    private Integer certificateCount;
    
    // Default constructor
    public ProfileInfoDTO() {
    }
    
    // Constructor with parameters
    public ProfileInfoDTO(String name, String email, Integer certificateCount) {
        this.name = name;
        this.email = email;
        this.certificateCount = certificateCount;
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Integer getCertificateCount() {
        return certificateCount;
    }
    
    public void setCertificateCount(Integer certificateCount) {
        this.certificateCount = certificateCount;
    }
    
    @Override
    public String toString() {
        return "ProfileInfoDTO{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", certificateCount=" + certificateCount +
                '}';
    }
}