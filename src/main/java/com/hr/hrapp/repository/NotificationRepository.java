package com.hr.hrapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hr.hrapp.entity.Notification;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification>
    findByEmployeeIdOrderByCreatedAtDesc(
            Long employeeId);

    long countByEmployeeIdAndIsReadFalse(
            Long employeeId);
}