package com.hr.hrapp.service.impl;

import com.hr.hrapp.entity.AuditLog;
import com.hr.hrapp.repository.AuditLogRepository;
import com.hr.hrapp.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public AuditLog save(AuditLog log) {
        return auditLogRepository.save(log);
    }

    @Override
    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }

    @Override
    public List<AuditLog> findByUsername(String username) {
        // simple in-memory filtering via repository - add custom query if needed
        return auditLogRepository.findAll().stream()
                .filter(l -> l.getUsername() != null && l.getUsername().equals(username))
                .toList();
    }
}
