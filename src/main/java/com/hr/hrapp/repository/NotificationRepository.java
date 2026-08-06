package com.hr.hrapp.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hr.hrapp.entity.Notification;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    // Paginated version
    Page<Notification> findByEmployeeIdOrderByCreatedAtDesc(
            Long employeeId,
            Pageable pageable);

    // Backwards-compatible list version
    List<Notification> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    long countByEmployeeIdAndIsReadFalse(
            Long employeeId);
}