package com.college.event.controller;

import com.college.event.model.Certificate;
import com.college.event.model.Registration;
import com.college.event.model.Event;
import com.college.event.repository.CertificateRepository;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/certificates")
@CrossOrigin(origins = "*")
public class CertificateController {

    @Autowired
    private CertificateRepository certificateRepository;
    
    @Autowired
    private RegistrationRepository registrationRepository;
    
    @Autowired
    private EventRepository eventRepository;

    // Get certificates by user ID
    @GetMapping("/user/{userId}")
    public List<Certificate> getUserCertificates(@PathVariable Long userId) {
        return certificateRepository.findByUserIdOrderByGeneratedDateDesc(userId);
    }
    
    // Get certificates by event ID
    @GetMapping("/event/{eventId}")
    public List<Certificate> getEventCertificates(@PathVariable Long eventId) {
        return certificateRepository.findByEventIdOrderByGeneratedDateDesc(eventId);
    }
    
    // Get all certificates (admin)
    @GetMapping
    public List<Certificate> getAllCertificates() {
        return certificateRepository.findByIsActiveTrueOrderByGeneratedDateDesc();
    }
    
    // Generate certificate for a registration
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateCertificate(@RequestBody Map<String, Object> request) {
        try {
            Long registrationId = Long.valueOf(request.get("registrationId").toString());
            String category = request.get("category").toString(); // "Participant", "Winner", "Runner-up"
            String position = request.containsKey("position") ? request.get("position").toString() : null;
            
            // Get registration details
            Registration registration = registrationRepository.findById(registrationId).orElse(null);
            if (registration == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Registration not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Get event details
            Event event = eventRepository.findById(registration.getEventId()).orElse(null);
            if (event == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Event not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if certificate already exists
            List<Certificate> existingCerts = certificateRepository.findByRegistrationIdOrderByGeneratedDateDesc(registrationId);
            if (!existingCerts.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Certificate already exists for this registration");
                response.put("certificate", existingCerts.get(0));
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create certificate
            Certificate certificate = new Certificate();
            certificate.setUserId(registration.getUserId());
            certificate.setEventId(registration.getEventId());
            certificate.setRegistrationId(registrationId);
            certificate.setUserName(registration.getUserName());
            certificate.setUserEmail(registration.getUserEmail());
            certificate.setEventTitle(event.getTitle());
            certificate.setCategory(category);
            certificate.setPosition(position);
            certificate.setIssuedDate(LocalDateTime.now());
            certificate.setIssuerName("College Event Management System");
            certificate.setCertificateTemplate("standard");
            
            // Generate unique certificate ID
            String certId = "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            certificate.setCertificateId(certId);
            
            // Generate QR code data for verification
            String qrData = "VERIFY:" + certId + ":" + registration.getUserEmail() + ":" + event.getTitle();
            certificate.setQrCodeData(qrData);
            certificate.setVerificationUrl("http://localhost:8080/verify-certificate.html?id=" + certId);
            
            Certificate savedCertificate = certificateRepository.save(certificate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Certificate generated successfully");
            response.put("certificate", savedCertificate);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to generate certificate: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Verify certificate by ID
    @GetMapping("/verify/{certificateId}")
    public ResponseEntity<Map<String, Object>> verifyCertificate(@PathVariable String certificateId) {
        try {
            Optional<Certificate> certOpt = certificateRepository.findByCertificateId(certificateId);
            
            Map<String, Object> response = new HashMap<>();
            if (certOpt.isPresent()) {
                Certificate certificate = certOpt.get();
                if (certificate.getIsActive()) {
                    response.put("valid", true);
                    response.put("certificate", certificate);
                    response.put("message", "Certificate is valid");
                } else {
                    response.put("valid", false);
                    response.put("message", "Certificate has been revoked");
                }
            } else {
                response.put("valid", false);
                response.put("message", "Certificate not found");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Verification failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Get certificate statistics
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCertificateStats() {
        try {
            long totalCertificates = certificateRepository.count();
            long participantCerts = certificateRepository.countByCategory("Participant");
            long winnerCerts = certificateRepository.countByCategory("Winner");
            long runnerUpCerts = certificateRepository.countByCategory("Runner-up");
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCertificates", totalCertificates);
            stats.put("participantCertificates", participantCerts);
            stats.put("winnerCertificates", winnerCerts);
            stats.put("runnerUpCertificates", runnerUpCerts);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get stats: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Update certificate status (admin)
    @PostMapping("/update-status/{certificateId}")
    public ResponseEntity<Map<String, Object>> updateCertificateStatus(
            @PathVariable String certificateId, 
            @RequestBody Map<String, Object> request) {
        try {
            Optional<Certificate> certOpt = certificateRepository.findByCertificateId(certificateId);
            if (!certOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Certificate not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            Certificate certificate = certOpt.get();
            certificate.setIsActive(Boolean.valueOf(request.get("isActive").toString()));
            if (request.containsKey("notes")) {
                certificate.setNotes(request.get("notes").toString());
            }
            
            certificateRepository.save(certificate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Certificate status updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update certificate: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}