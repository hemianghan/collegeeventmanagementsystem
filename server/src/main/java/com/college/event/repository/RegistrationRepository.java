package com.college.event.repository;

import com.college.event.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByUserId(Long userId);
    List<Registration> findByEventId(Long eventId);
    List<Registration> findByUserIdAndEventId(Long userId, Long eventId);
    void deleteByUserIdAndEventId(Long userId, Long eventId);
    
    /**
     * Count registrations for a user where certificateId is not null and not empty
     * This method counts certificates earned by a specific user
     */
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.userId = :userId AND r.certificateId IS NOT NULL AND r.certificateId != ''")
    Long countByUserIdAndCertificateIdIsNotNull(@Param("userId") Long userId);
    
    /**
     * Find registrations for a user where certificateId is not null and not empty
     * This method retrieves all registrations with certificates for a specific user
     */
    @Query("SELECT r FROM Registration r WHERE r.userId = :userId AND r.certificateId IS NOT NULL AND r.certificateId != ''")
    List<Registration> findByUserIdAndCertificateIdIsNotNull(@Param("userId") Long userId);
}
