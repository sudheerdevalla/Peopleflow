package com.hr.hrapp.service.impl;

import com.hr.hrapp.entity.Permission;
import com.hr.hrapp.repository.PermissionRepository;
import com.hr.hrapp.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public Permission savePermission(Permission permission) {
        return permissionRepository.save(permission);
    }

    @Override
    public Permission getPermissionById(Long id) {
        Optional<Permission> permission = permissionRepository.findById(id);
        return permission.orElse(null);
    }

    @Override
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Override
    public Permission updatePermission(Long id, Permission permission) {
        if (permissionRepository.existsById(id)) {
            permission.setId(id);
            return permissionRepository.save(permission);
        }
        return null;
    }

    @Override
    public void deletePermission(Long id) {
        permissionRepository.deleteById(id);
    }

    @Override
    public Permission getPermissionByName(String name) {
        return permissionRepository.findByName(name);
    }
}
