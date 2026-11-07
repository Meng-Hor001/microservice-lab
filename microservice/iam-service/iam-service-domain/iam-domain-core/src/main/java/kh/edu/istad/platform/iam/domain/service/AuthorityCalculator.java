package kh.edu.istad.platform.iam.domain.service;

import static kh.edu.istad.platform.iam.domain.support.DomainRules.*;
import kh.edu.istad.platform.iam.domain.enums.PermissionEffect;


import kh.edu.istad.platform.iam.domain.entity.*;

import java.time.Instant;
import java.util.*;

public final class AuthorityCalculator {
    public record Result(
            Set<String> roles,
            Set<String> effectivePermissions
    ) {

        public Result {
            roles = Collections.unmodifiableSet(new TreeSet<>(roles));

            effectivePermissions = Collections.unmodifiableSet(
                    new TreeSet<>(effectivePermissions)
            );
        }

        public Set<String> authorities() {
            Set<String> result = new TreeSet<>(roles);
            result.addAll(effectivePermissions);
            return Collections.unmodifiableSet(result);
        }
    }

    public Result calculate(
            User user,
            Collection<Role> roles,
            Collection<Permission> permissions,
            Collection<UserRole> userRoles,
            Collection<RolePermission> rolePermissions,
            Collection<UserPermission> userPermissions,
            Instant now
    ) {
        required(user, "User");
        required(roles, "Roles");
        required(permissions, "Permissions");
        required(userRoles, "User-role assignments");
        required(rolePermissions, "Role-permission assignments");
        required(userPermissions, "User-permission assignments");
        required(now, "Current timestamp");

        if (user.isDeleted()) {
            return new Result(Set.of(), Set.of());
        }

        Long userId = persistedId(user.getId(), "User ID");

        Map<Long, Role> roleById = new HashMap<>();

        for (Role role : roles) {
            Long id = persistedId(role.getId(), "Role ID");

            check(
                    roleById.putIfAbsent(id, role) == null,
                    "Duplicate role ID"
            );
        }

        Map<Long, Permission> permissionById = new HashMap<>();

        for (Permission permission : permissions) {
            Long id = persistedId(permission.getId(), "Permission ID");

            check(
                    permissionById.putIfAbsent(id, permission) == null,
                    "Duplicate permission ID"
            );
        }

        Set<Long> activeRoleIds = new HashSet<>();
        Set<String> roleAuthorities = new HashSet<>();

        for (UserRole assignment : userRoles) {
            if (!assignment.getUserId().equals(userId)
                    || !assignment.isActiveAt(now)) {
                continue;
            }

            Role role = roleById.get(assignment.getRoleId());

            if (role != null && role.isActive() && !role.isDeleted()) {
                activeRoleIds.add(role.getId());
                roleAuthorities.add("ROLE_" + role.getName());
            }
        }

        Set<String> grants = new HashSet<>();
        Set<String> denies = new HashSet<>();

        for (RolePermission assignment : rolePermissions) {
            if (!activeRoleIds.contains(assignment.getRoleId())
                    || !assignment.isActiveAt(now)) {
                continue;
            }

            Permission permission = permissionById.get(
                    assignment.getPermissionId()
            );

            if (usable(permission)) {
                grants.add(permission.getName());
            }
        }

        for (UserPermission assignment : userPermissions) {
            if (!assignment.getUserId().equals(userId)
                    || !assignment.isActiveAt(now)) {
                continue;
            }

            Permission permission = permissionById.get(
                    assignment.getPermissionId()
            );

            if (!usable(permission)) {
                continue;
            }

            if (assignment.getEffect() == PermissionEffect.DENY) {
                denies.add(permission.getName());
            } else {
                grants.add(permission.getName());
            }
        }

        grants.removeAll(denies);

        return new Result(roleAuthorities, grants);
    }

    private boolean usable(Permission permission) {
        return permission != null
                && permission.isActive()
                && !permission.isDeleted();
    }
}
