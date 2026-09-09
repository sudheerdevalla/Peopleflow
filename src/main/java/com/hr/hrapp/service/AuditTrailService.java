package com.hr.hrapp.service;

import com.hr.hrapp.entity.AuditLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditTrailService {

    @Autowired
    private AuditLogService auditLogService;

    public void record(String username,
                       String action,
                       String endpoint,
                       String status,
                       String entityType,
                       Object entityId,
                       String details) {
        auditLogService.save(new AuditLog(
                username,
                action,
                endpoint,
                LocalDateTime.now(),
                status,
                entityType,
                entityId == null ? null : String.valueOf(entityId),
                details
        ));
    }
}
