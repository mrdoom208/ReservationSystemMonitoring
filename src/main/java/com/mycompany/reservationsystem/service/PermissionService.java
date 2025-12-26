package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.model.Permission;
import com.mycompany.reservationsystem.model.PositionPermission;
import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.repository.PermissionRepository;
import com.mycompany.reservationsystem.repository.PositionPermissionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepo;

    @Autowired
    private PositionPermissionRepository positionPermissionRepo;

    /**
     * Check if a given user has the given permission code
     */
    public boolean hasPermission(User user, String permissionCode) {
        if (user == null || user.getPosition() == null) return false;

        Permission permission = permissionRepo.findByCode(permissionCode).orElse(null);
        if (permission == null) return false;

        return positionPermissionRepo.existsByPositionAndPermission(user.getPosition(), permission);
    }

    public boolean hasPermission(User.Position position, Permission permission) {
        return positionPermissionRepo
                .existsByPositionAndPermission(position, permission);
    }

    public List<Permission> findAllPermissions() {
        return permissionRepo.findAll();
    }

    @Transactional
    public void updatePermission(User.Position position, Long permissionId, boolean enabled) {
        Permission permission = permissionRepo.findById(permissionId).orElse(null);
        System.out.println("Update Permission : "+position + enabled + permission.getCode());
        if (permission == null) return;

        if (enabled) {
            grant(position, permission);   // use your existing grant() method
        } else {
            revoke(position, permission);  // use your existing revoke() method
        }
    }

    // ---------------- WRITE ----------------

    public void grant(User.Position position, Permission permission) {
        System.out.println("GRANT"+position + permission.getCode());
        if (!hasPermission(position, permission)) {
            positionPermissionRepo.save(
                    new PositionPermission(position, permission)
            );
        }
    }

    public void revoke(User.Position position, Permission permission) {
        System.out.println("REVOKE "+position + permission.getCode());
        positionPermissionRepo
                .findByPositionAndPermission(position, permission)
                .ifPresent(positionPermissionRepo::delete);
    }
}
