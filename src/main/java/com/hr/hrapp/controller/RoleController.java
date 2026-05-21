package com.hr.hrapp.controller;

import com.hr.hrapp.entity.Role;
import com.hr.hrapp.service.RoleService;
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
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private AuditLogService auditLogService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role, HttpServletRequest request) {
        Role saved = roleService.saveRole(role);
        auditLogService.save(new AuditLog(request.getUserPrincipal().getName(), "CREATE_ROLE", request.getRequestURI(), LocalDateTime.now(), "SUCCESS"));
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        if (role == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(role);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Role> getAllRoles() {
        return roleService.getAllRoles();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role role, HttpServletRequest request) {
        Role updated = roleService.updateRole(id, role);
        if (updated == null) {
            auditLogService.save(new AuditLog(request.getUserPrincipal().getName(), "UPDATE_ROLE", request.getRequestURI(), LocalDateTime.now(), "FAILURE"));
            return ResponseEntity.notFound().build();
        }
        auditLogService.save(new AuditLog(request.getUserPrincipal().getName(), "UPDATE_ROLE", request.getRequestURI(), LocalDateTime.now(), "SUCCESS"));
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id, HttpServletRequest request) {
        roleService.deleteRole(id);
        auditLogService.save(new AuditLog(request.getUserPrincipal().getName(), "DELETE_ROLE", request.getRequestURI(), LocalDateTime.now(), "SUCCESS"));
        return ResponseEntity.noContent().build();
    }
}