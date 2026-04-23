package com.college.event.controller;

import com.college.event.model.Memory;
import com.college.event.model.Event;
import com.college.event.repository.MemoryRepository;
import com.college.event.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.Base64;
import java.io.IOException;

@RestController
@RequestMapping("/api/memories")
@CrossOrigin(origins = "*")
public class MemoryController {

    @Autowired
    private MemoryRepository memoryRepository;
    
    @Autowired
    private EventRepository eventRepository;

    // Get all approved memories for public display
    @GetMapping
    public List<Memory> getAllMemories() {
        return memoryRepository.findByIsApprovedTrueOrderByCreatedDateDesc();
    }
    
    // Get memories by event ID
    @GetMapping("/event/{eventId}")
    public List<Memory> getMemoriesByEvent(@PathVariable Long eventId) {
        return memoryRepository.findByEventIdAndIsApprovedTrueOrderByCreatedDateDesc(eventId);
    }
    
    // Get memories by user ID
    @GetMapping("/user/{userId}")
    public List<Memory> getMemoriesByUser(@PathVariable Long userId) {
        return memoryRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }
    
    // Get pending memories for admin approval
    @GetMapping("/pending")
    public List<Memory> getPendingMemories() {
        return memoryRepository.findByIsApprovedFalseOrderByCreatedDateDesc();
    }

    // Add new memory/review
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addMemory(@RequestBody Map<String, Object> request) {
        try {
            Memory memory = new Memory();
            memory.setUserId(Long.valueOf(request.get("userId").toString()));
            memory.setUserName(request.get("userName").toString());
            memory.setUserEmail(request.get("userEmail").toString());
            memory.setEventId(Long.valueOf(request.get("eventId").toString()));
            memory.setReviewText(request.get("reviewText").toString());
            memory.setRating(Integer.valueOf(request.get("rating").toString()));
            memory.setCategory(request.get("category").toString()); // "Memory", "Review", "Feedback"
            
            // Get event title
            Event event = eventRepository.findById(memory.getEventId()).orElse(null);
            if (event != null) {
                memory.setEventTitle(event.getTitle());
            }
            
            // Handle image if provided
            if (request.containsKey("imageData") && request.get("imageData") != null) {
                memory.setImageUrl(request.get("imageData").toString());
            }
            
            memory.setCreatedDate(LocalDateTime.now());
            memory.setIsApproved(false); // Requires admin approval
            
            Memory savedMemory = memoryRepository.save(memory);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Memory shared successfully! Your memory is pending admin approval and will be visible on the rating board within 24-48 hours. You can check your submission status in your profile.");
            response.put("memory", savedMemory);
            response.put("approvalStatus", "PENDING");
            response.put("estimatedApprovalTime", "24-48 hours");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to share memory: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Upload image for memory
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Please select a file to upload");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Convert to Base64 for simple storage
            byte[] bytes = file.getBytes();
            String base64Image = "data:" + file.getContentType() + ";base64," + Base64.getEncoder().encodeToString(bytes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("imageData", base64Image);
            response.put("message", "Image uploaded successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Admin approve memory
    @PostMapping("/approve/{memoryId}")
    public ResponseEntity<Map<String, Object>> approveMemory(@PathVariable Long memoryId) {
        try {
            Memory memory = memoryRepository.findById(memoryId).orElse(null);
            if (memory == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Memory not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            memory.setIsApproved(true);
            memory.setUpdatedDate(LocalDateTime.now());
            memoryRepository.save(memory);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Memory approved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to approve memory: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Like a memory
    @PostMapping("/like/{memoryId}")
    public ResponseEntity<Map<String, Object>> likeMemory(@PathVariable Long memoryId) {
        try {
            Memory memory = memoryRepository.findById(memoryId).orElse(null);
            if (memory == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Memory not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            memory.setLikes(memory.getLikes() + 1);
            memoryRepository.save(memory);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("likes", memory.getLikes());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to like memory: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Get memory statistics
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMemoryStats() {
        try {
            long totalMemories = memoryRepository.count();
            long approvedMemories = memoryRepository.countByIsApprovedTrue();
            long pendingMemories = memoryRepository.countByIsApprovedFalse();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalMemories", totalMemories);
            stats.put("approvedMemories", approvedMemories);
            stats.put("pendingMemories", pendingMemories);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get stats: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
