package com.hr.hrapp.controller;

import com.hr.hrapp.entity.Permission;
import com.hr.hrapp.service.PermissionService;
import com.hr.hrapp.service.AuditLogService;
import com.hr.hrapp.entity.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private AuditLogService auditLogService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Permission> createPermission(@RequestBody Permission permission, HttpServletRequest request) {
        Permission saved = permissionService.savePermission(permission);
        auditLogService.save(new AuditLog(request.getUserPrincipal().getName(), "CREATE_PERMISSION", request.getRequestURI(), LocalDateTime.now(), "SUCCESS"));
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Permission> getPermissionById(@PathVariable Long id) {
        Permission permission = permissionService.getPermissionById(id);
        if (permission == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(permission);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Permission> getAllPermissions() {
        return permissionService.getAllPermissions();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Permission> updatePermission(@PathVariable Long id, @RequestBody Permission permission, HttpServletRequest request) {
        Permission updated = permissionService.updatePermission(id, permission);
        if (updated == null) {
            auditLogService.save(new AuditLog(request.getUserPrincipal().getName(), "UPDATE_PERMISSION", request.getRequestURI(), LocalDateTime.now(), "FAILURE"));
            return ResponseEntity.notFound().build();
        }
        auditLogService.save(new AuditLog(request.getUserPrincipal().getName(), "UPDATE_PERMISSION", request.getRequestURI(), LocalDateTime.now(), "SUCCESS"));
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id, HttpServletRequest request) {
        permissionService.deletePermission(id);
        auditLogService.save(new AuditLog(request.getUserPrincipal().getName(), "DELETE_PERMISSION", request.getRequestURI(), LocalDateTime.now(), "SUCCESS"));
        return ResponseEntity.noContent().build();
    }
}