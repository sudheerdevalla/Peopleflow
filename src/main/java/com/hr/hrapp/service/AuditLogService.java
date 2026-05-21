package com.hr.hrapp.service;

import com.hr.hrapp.entity.AuditLog;

import java.util.List;

public interface AuditLogService {
    AuditLog save(AuditLog log);
    List<AuditLog> findAll();
    List<AuditLog> findByUsername(String username);
}
