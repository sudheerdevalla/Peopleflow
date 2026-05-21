package com.hr.hrapp.controller;

import com.hr.hrapp.entity.AuditLog;
import com.hr.hrapp.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLog> getAllLogs() {
        return auditLogService.findAll();
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLog> getLogsByUser(@PathVariable String username) {
        return auditLogService.findByUsername(username);
    }

}
