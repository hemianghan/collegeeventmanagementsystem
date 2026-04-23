package com.college.event.repository;

import com.college.event.model.Memory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemoryRepository extends JpaRepository<Memory, Long> {
    List<Memory> findByIsApprovedTrueOrderByCreatedDateDesc();
    List<Memory> findByIsApprovedFalseOrderByCreatedDateDesc();
    List<Memory> findByEventIdAndIsApprovedTrueOrderByCreatedDateDesc(Long eventId);
    List<Memory> findByUserIdOrderByCreatedDateDesc(Long userId);
    long countByIsApprovedTrue();
    long countByIsApprovedFalse();
}
