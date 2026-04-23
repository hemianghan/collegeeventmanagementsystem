package com.college.event.service;

import com.college.event.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CertificateService {

    @Autowired
    private RegistrationRepository registrationRepository;

    /**
     * Get certificate count for a specific user
     * @param userId The ID of the user
     * @return The number of certificates earned by the user
     */
    public Long getCertificateCount(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            
            // Count registrations where certificateId is not null and not empty
            Long count = registrationRepository.countByUserIdAndCertificateIdIsNotNull(userId);
            
            return count != null ? count : 0L;
            
        } catch (Exception e) {
            // Log the error (in a real application, use proper logging)
            System.err.println("Error counting certificates for user " + userId + ": " + e.getMessage());
            
            // Return 0 for database connection issues to provide graceful degradation
            return 0L;
        }
    }

    /**
     * Format certificate count for display
     * @param count The certificate count
     * @return Formatted string with proper pluralization
     */
    public String formatCertificateCount(Long count) {
        if (count == null) {
            count = 0L;
        }
        
        if (count == 0) {
            return "0 certificates earned";
        } else if (count == 1) {
            return "1 certificate earned";
        } else {
            return count + " certificates earned";
        }
    }

    /**
     * Check if a user has any certificates
     * @param userId The ID of the user
     * @return true if user has at least one certificate, false otherwise
     */
    public boolean hasCertificates(Long userId) {
        return getCertificateCount(userId) > 0;
    }

    /**
     * Get certificate count with formatted display
     * @param userId The ID of the user
     * @return Map containing count and formatted display string
     */
    public java.util.Map<String, Object> getCertificateCountWithFormat(Long userId) {
        Long count = getCertificateCount(userId);
        String formattedCount = formatCertificateCount(count);
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("count", count);
        result.put("formatted", formattedCount);
        result.put("hasCertificates", count > 0);
        
        return result;
    }
}