package com.college.event.controller;

import com.college.event.model.User;
import com.college.event.repository.UserRepository;
import com.college.event.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CertificateService certificateService;

    /**
     * Get certificate count for authenticated user
     */
    @GetMapping("/certificates/count")
    public ResponseEntity<Map<String, Object>> getCertificateCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check authentication
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }
            
            // Get certificate count and formatted display
            Map<String, Object> certificateData = certificateService.getCertificateCountWithFormat(userId);
            
            response.put("success", true);
            response.put("certificateCount", certificateData.get("count"));
            response.put("formattedCount", certificateData.get("formatted"));
            response.put("hasCertificates", certificateData.get("hasCertificates"));
            response.put("message", "Certificate count retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving certificate count: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Change user password
     */
    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody Map<String, String> passwordData, 
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check authentication
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }
            
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            
            // Validate input
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Current password is required");
                return ResponseEntity.status(400).body(response);
            }
            
            if (newPassword == null || newPassword.length() < 6) {
                response.put("success", false);
                response.put("message", "New password must be at least 6 characters");
                return ResponseEntity.status(400).body(response);
            }
            
            // Get user from database
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }
            
            // Verify current password
            if (!user.getPassword().equals(currentPassword)) {
                response.put("success", false);
                response.put("message", "Current password is incorrect");
                return ResponseEntity.status(400).body(response);
            }
            
            // Update password
            user.setPassword(newPassword);
            userRepository.save(user);
            
            response.put("success", true);
            response.put("message", "Password changed successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error changing password: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get user profile information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getProfileInfo(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check authentication
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }
            
            // Get user from database
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }
            
            // Return profile information
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("name", user.getName());
            profileData.put("email", user.getEmail());
            
            response.put("success", true);
            response.put("profile", profileData);
            response.put("message", "Profile information retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving profile information: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Update user profile information
     */
    @PutMapping("/info")
    public ResponseEntity<Map<String, Object>> updateProfileInfo(
            @RequestBody Map<String, String> profileData, 
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check authentication
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }
            
            String name = profileData.get("name");
            
            // Validate input
            if (name == null || name.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Name is required and cannot be empty");
                return ResponseEntity.status(400).body(response);
            }
            
            // Get user from database
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }
            
            // Update name (email remains read-only)
            user.setName(name.trim());
            userRepository.save(user);
            
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating profile: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}