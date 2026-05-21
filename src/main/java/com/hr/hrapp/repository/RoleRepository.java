package com.hr.hrapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hr.hrapp.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}