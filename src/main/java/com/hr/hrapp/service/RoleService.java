package com.hr.hrapp.service;

import com.hr.hrapp.entity.Role;
import java.util.List;

public interface RoleService {
    Role saveRole(Role role);
    Role getRoleById(Long id);
    List<Role> getAllRoles();
    Role updateRole(Long id, Role role);
    void deleteRole(Long id);
    Role getRoleByName(String name);
}
