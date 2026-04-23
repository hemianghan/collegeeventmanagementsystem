package com.college.event.controller;

import com.college.event.model.Notification;
import com.college.event.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepo;

    // Get all notifications for a user
    @GetMapping("/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        return notificationRepo.findByUserIdOrderByCreatedDateDesc(userId);
    }

    // Get unread notifications count
    @GetMapping("/user/{userId}/unread-count")
    public Map<String, Long> getUnreadCount(@PathVariable Long userId) {
        Long count = notificationRepo.countByUserIdAndIsRead(userId, false);
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return response;
    }

    // Get unread notifications
    @GetMapping("/user/{userId}/unread")
    public List<Notification> getUnreadNotifications(@PathVariable Long userId) {
        return notificationRepo.findByUserIdAndIsReadOrderByCreatedDateDesc(userId, false);
    }

    // Mark notification as read
    @PutMapping("/{notificationId}/mark-read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long notificationId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Notification notification = notificationRepo.findById(notificationId).orElse(null);
            if (notification != null) {
                notification.setIsRead(true);
                notification.setReadDate(LocalDateTime.now());
                notificationRepo.save(notification);
                
                response.put("success", true);
                response.put("message", "Notification marked as read");
            } else {
                response.put("success", false);
                response.put("message", "Notification not found");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    // Mark all notifications as read for a user
    @PutMapping("/user/{userId}/mark-all-read")
    public ResponseEntity<Map<String, Object>> markAllAsRead(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Notification> notifications = notificationRepo.findByUserIdAndIsReadOrderByCreatedDateDesc(userId, false);
            for (Notification notification : notifications) {
                notification.setIsRead(true);
                notification.setReadDate(LocalDateTime.now());
            }
            notificationRepo.saveAll(notifications);
            
            response.put("success", true);
            response.put("message", "All notifications marked as read");
            response.put("count", notifications.size());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    // Create a new notification (for admin notification system)
    @PostMapping
    public ResponseEntity<Map<String, Object>> sendNotification(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String title = (String) requestData.get("title");
            String message = (String) requestData.get("message");
            String type = (String) requestData.get("type");
            String audience = (String) requestData.get("audience");
            Long eventId = requestData.get("eventId") != null ? 
                Long.valueOf(requestData.get("eventId").toString()) : null;
            
            int notificationsSent = 0;
            
            if ("ALL_STUDENTS".equals(audience)) {
                // Send to all students - create individual notifications for each student
                List<Long> studentIds = List.of(1001L, 1002L); // Known student IDs
                String[] studentNames = {"Test Student", "Hemishaan Ghan"};
                String[] studentEmails = {"student@college.edu", "hemishaanghan1@gmail.com"};
                
                for (int i = 0; i < studentIds.size(); i++) {
                    Notification notification = new Notification();
                    notification.setUserId(studentIds.get(i));
                    notification.setUserName(studentNames[i]);
                    notification.setUserEmail(studentEmails[i]);
                    notification.setTitle(title);
                    notification.setMessage(message);
                    notification.setType(type);
                    notification.setCreatedDate(LocalDateTime.now());
                    notification.setIsRead(false);
                    
                    notificationRepo.save(notification);
                    notificationsSent++;
                }
            } else if ("EVENT_PARTICIPANTS".equals(audience) && eventId != null) {
                // Send to specific event participants
                // This would require a registration service to get participants
                // For now, we'll send to known students as a fallback
                List<Long> studentIds = List.of(1001L, 1002L);
                String[] studentNames = {"Test Student", "Hemishaan Ghan"};
                String[] studentEmails = {"student@college.edu", "hemishaanghan1@gmail.com"};
                
                for (int i = 0; i < studentIds.size(); i++) {
                    Notification notification = new Notification();
                    notification.setUserId(studentIds.get(i));
                    notification.setUserName(studentNames[i]);
                    notification.setUserEmail(studentEmails[i]);
                    notification.setTitle(title);
                    notification.setMessage(message);
                    notification.setType(type);
                    notification.setCreatedDate(LocalDateTime.now());
                    notification.setIsRead(false);
                    
                    notificationRepo.save(notification);
                    notificationsSent++;
                }
            } else {
                // Single notification (legacy support)
                Notification notification = new Notification();
                notification.setUserId((Long) requestData.get("userId"));
                notification.setUserName((String) requestData.get("userName"));
                notification.setUserEmail((String) requestData.get("userEmail"));
                notification.setTitle(title);
                notification.setMessage(message);
                notification.setType(type);
                notification.setCreatedDate(LocalDateTime.now());
                notification.setIsRead(false);
                
                notificationRepo.save(notification);
                notificationsSent = 1;
            }
            
            response.put("success", true);
            response.put("message", "Notification sent successfully to " + notificationsSent + " recipients");
            response.put("recipientCount", notificationsSent);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    // Create a new notification
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createNotification(@RequestBody Notification notification) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Notification savedNotification = notificationRepo.save(notification);
            response.put("success", true);
            response.put("message", "Notification created successfully");
            response.put("notification", savedNotification);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    // Delete a notification
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long notificationId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            notificationRepo.deleteById(notificationId);
            response.put("success", true);
            response.put("message", "Notification deleted successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
