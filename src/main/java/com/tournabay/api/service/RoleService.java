package com.tournabay.api.service;

import com.tournabay.api.exception.AppException;
import com.tournabay.api.model.Role;
import com.tournabay.api.model.RoleName;
import com.tournabay.api.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    /**
     * It returns a set of roles that contains the role with the name ROLE_USER
     *
     * @return A set of roles.
     */
    public Set<Role> getBasicRoles() {
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow(() -> new AppException("Roles not set!"));
        roles.add(userRole);
        return roles;
    }

    /**
     * If the role exists, return it, otherwise throw an exception.
     *
     * @param roleName The name of the role you want to get.
     * @return A Role object
     */
    public Role getByName(RoleName roleName) {
        return roleRepository.findByName(roleName).orElseThrow(() -> new AppException("Role not set!"));
    }

    /**
     * If the role doesn't exist, create it
     */
    @PostConstruct
    private void createDefaultRoles() {
        roleRepository.findByName(RoleName.ROLE_USER).orElseGet(() -> {
            Role userRole = new Role(1L, RoleName.ROLE_USER);
            return roleRepository.save(userRole);
        });
        roleRepository.findByName(RoleName.ROLE_ADMIN).orElseGet(() -> {
            Role adminRole = new Role(2L, RoleName.ROLE_ADMIN);
            return roleRepository.save(adminRole);
        });
    }
}
