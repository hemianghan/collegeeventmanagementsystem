package com.college.event.controller;

import com.college.event.model.Registration;
import com.college.event.model.Event;
import com.college.event.repository.RegistrationRepository;
import com.college.event.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "*")
public class RegistrationController {

    @Autowired
    private RegistrationRepository registrationRepository;
    
    @Autowired
    private EventRepository eventRepository;

    @GetMapping("/user/{userId}")
    public List<Registration> getUserRegistrations(@PathVariable Long userId) {
        return registrationRepository.findByUserId(userId);
    }

    @GetMapping("/event/{eventId}")
    public List<Registration> getEventRegistrations(@PathVariable Long eventId) {
        return registrationRepository.findByEventId(eventId);
    }

    @PostMapping("/add")
    public Registration addRegistration(@RequestBody Registration registration) {
        return registrationRepository.save(registration);
    }
    
    // New endpoint for student registration with payment
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerForEvent(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long eventId = Long.valueOf(request.get("eventId").toString());
            String userName = request.get("userName").toString();
            String userEmail = request.get("userEmail").toString();
            String paymentMethod = request.get("paymentMethod").toString(); // "ONLINE" or "DESK"
            
            // Check if user is already registered for this event
            List<Registration> existingRegistrations = registrationRepository.findByUserIdAndEventId(userId, eventId);
            if (!existingRegistrations.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "You are already registered for this event");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Get event details for registration fee
            Event event = eventRepository.findById(eventId).orElse(null);
            if (event == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Event not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create registration
            Registration registration = new Registration();
            registration.setUserId(userId);
            registration.setEventId(eventId);
            registration.setUserName(userName);
            registration.setUserEmail(userEmail);
            registration.setPaymentMethod(paymentMethod);
            registration.setRegistrationFee(event.getRegistrationFee());
            registration.setRegistrationDate(LocalDateTime.now());
            registration.setEventRole("Participant");
            
            if ("ONLINE".equals(paymentMethod)) {
                // Simulate online payment processing
                registration.setStatus("PAID");
                registration.setPaymentStatus("COMPLETED");
                registration.setTransactionId("TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                registration.setPaymentDate(LocalDateTime.now());
            } else {
                // Desk payment - pending until paid at desk
                registration.setStatus("PENDING");
                registration.setPaymentStatus("PENDING");
            }
            
            Registration savedRegistration = registrationRepository.save(registration);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Registration successful!");
            response.put("registration", savedRegistration);
            response.put("eventTitle", event.getTitle());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Get user registrations with event details
    @GetMapping("/user/{userId}/detailed")
    public ResponseEntity<List<Map<String, Object>>> getUserRegistrationsDetailed(@PathVariable Long userId) {
        List<Registration> registrations = registrationRepository.findByUserId(userId);
        List<Map<String, Object>> detailedRegistrations = new java.util.ArrayList<>();
        
        for (Registration reg : registrations) {
            Event event = eventRepository.findById(reg.getEventId()).orElse(null);
            if (event != null) {
                Map<String, Object> regWithEvent = new HashMap<>();
                regWithEvent.put("registration", reg);
                regWithEvent.put("event", event);
                detailedRegistrations.add(regWithEvent);
            }
        }
        
        return ResponseEntity.ok(detailedRegistrations);
    }
    
    // Get registration statistics for a user
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getUserRegistrationStats(@PathVariable Long userId) {
        List<Registration> registrations = registrationRepository.findByUserId(userId);
        
        long totalRegistrations = registrations.size();
        long pendingPayments = registrations.stream()
            .filter(r -> "PENDING".equals(r.getPaymentStatus()))
            .count();
        long successfulPayments = registrations.stream()
            .filter(r -> "COMPLETED".equals(r.getPaymentStatus()))
            .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRegistrations", totalRegistrations);
        stats.put("pendingPayments", pendingPayments);
        stats.put("successfulPayments", successfulPayments);
        
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/cancel/{userId}/{eventId}")
    @jakarta.transaction.Transactional
    public void cancelRegistration(@PathVariable Long userId, @PathVariable Long eventId) {
        registrationRepository.deleteByUserIdAndEventId(userId, eventId);
    }

    // Clear all registrations - for testing/reset purposes
    @DeleteMapping("/clear-all")
    @jakarta.transaction.Transactional
    public ResponseEntity<Map<String, Object>> clearAllRegistrations() {
        try {
            long deletedCount = registrationRepository.count();
            registrationRepository.deleteAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All registrations cleared successfully");
            response.put("deletedCount", deletedCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to clear registrations: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Create sample student registrations - for testing purposes
    @PostMapping("/create-sample")
    @jakarta.transaction.Transactional
    public ResponseEntity<Map<String, Object>> createSampleRegistrations() {
        try {
            // Get all events
            List<Event> events = eventRepository.findAll();
            
            if (events.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "No events found. Please create events first.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create 12 sample registrations for test student
            Long testStudentId = 1001L;
            String testStudentName = "Test Student";
            String testStudentEmail = "student@college.edu";
            
            int registrationCount = Math.min(12, events.size());
            
            for (int i = 0; i < registrationCount; i++) {
                Event event = events.get(i);
                
                // Check if registration already exists
                List<Registration> existing = registrationRepository.findByUserIdAndEventId(testStudentId, event.getId());
                if (!existing.isEmpty()) {
                    continue; // Skip if already registered
                }
                
                // Create registration
                Registration registration = new Registration();
                registration.setUserId(testStudentId);
                registration.setEventId(event.getId());
                registration.setUserName(testStudentName);
                registration.setUserEmail(testStudentEmail);
                registration.setPaymentMethod("DESK");
                registration.setRegistrationFee(event.getRegistrationFee());
                registration.setRegistrationDate(LocalDateTime.now().minusDays(i + 1));
                registration.setEventRole("Participant");
                registration.setStatus("PAID");
                registration.setPaymentStatus("COMPLETED");
                registration.setTransactionId("TXN" + System.currentTimeMillis() + String.format("%03d", i));
                registration.setPaymentDate(LocalDateTime.now().minusDays(i));
                
                registrationRepository.save(registration);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Created " + registrationCount + " sample registrations for test student");
            response.put("studentEmail", testStudentEmail);
            response.put("registrationCount", registrationCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create sample registrations: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/update-role")
    public Registration updateRole(@RequestBody Registration update) {
        Registration reg = registrationRepository.findById(update.getId()).orElseThrow();
        reg.setEventRole(update.getEventRole());
        reg.setCertificateId(update.getCertificateId());
        return registrationRepository.save(reg);
    }
}
