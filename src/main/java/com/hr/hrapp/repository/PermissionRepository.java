package com.hr.hrapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hr.hrapp.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Permission findByName(String name);
}