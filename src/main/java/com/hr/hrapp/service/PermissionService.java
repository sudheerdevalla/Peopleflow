package com.hr.hrapp.service;

import com.hr.hrapp.entity.Permission;
import java.util.List;

public interface PermissionService {
    Permission savePermission(Permission permission);
    Permission getPermissionById(Long id);
    List<Permission> getAllPermissions();
    Permission updatePermission(Long id, Permission permission);
    void deletePermission(Long id);
    Permission getPermissionByName(String name);
}
