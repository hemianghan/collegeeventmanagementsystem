package com.college.event.repository;

import com.college.event.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByUserIdOrderByGeneratedDateDesc(Long userId);
    List<Certificate> findByEventIdOrderByGeneratedDateDesc(Long eventId);
    List<Certificate> findByRegistrationIdOrderByGeneratedDateDesc(Long registrationId);
    Optional<Certificate> findByCertificateId(String certificateId);
    List<Certificate> findByCategoryOrderByGeneratedDateDesc(String category);
    List<Certificate> findByIsActiveTrueOrderByGeneratedDateDesc();
    long countByUserId(Long userId);
    long countByEventId(Long eventId);
    long countByCategory(String category);
}