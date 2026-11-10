package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.UserAuthoritiesResult;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.*;
import kh.edu.istad.platform.iam.domain.entity.*;
import kh.edu.istad.platform.iam.domain.service.AuthorityCalculator;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class GetUserAuthoritiesUseCase {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final UserRoleAssignmentRepository userRoles;
    private final RolePermissionAssignmentRepository rolePermissions;
    private final UserPermissionAssignmentRepository userPermissions;
    private final AuthorityCalculator calculator;
    private final Clock clock;

    public GetUserAuthoritiesUseCase(
            UserRepository users,
            RoleRepository roles,
            PermissionRepository permissions,
            UserRoleAssignmentRepository userRoles,
            RolePermissionAssignmentRepository rolePermissions,
            UserPermissionAssignmentRepository userPermissions,
            AuthorityCalculator calculator,
            Clock clock
    ) {
        this.users = Objects.requireNonNull(users);
        this.roles = Objects.requireNonNull(roles);
        this.permissions = Objects.requireNonNull(permissions);
        this.userRoles = Objects.requireNonNull(userRoles);
        this.rolePermissions = Objects.requireNonNull(rolePermissions);
        this.userPermissions = Objects.requireNonNull(userPermissions);
        this.calculator = Objects.requireNonNull(calculator);
        this.clock = Objects.requireNonNull(clock);
    }

    public UserAuthoritiesResult execute(UUID userUuid) {
        User user = users.findByUuid(userUuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", userUuid));

        List<UserRole> assignedRoles =
                userRoles.findActiveByUserUuid(userUuid);

        List<Role> roleEntities = assignedRoles.stream()
                .map(UserRole::getRoleId)
                .distinct()
                .map(id -> roles.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        List<Long> roleIds = roleEntities.stream()
                .map(Role::getId)
                .toList();

        List<RolePermission> rolePermissionEntities =
                rolePermissions.findByRoleIds(roleIds);

        List<UserPermission> directAssignments =
                userPermissions.findActiveByUserUuid(userUuid);

        List<Permission> permissionEntities =
                java.util.stream.Stream.concat(
                                rolePermissionEntities.stream()
                                        .map(RolePermission::getPermissionId),
                                directAssignments.stream()
                                        .map(UserPermission::getPermissionId)
                        )
                        .distinct()
                        .map(id -> permissions.findById(id).orElse(null))
                        .filter(Objects::nonNull)
                        .toList();

        var calculated = calculator.calculate(
                user,
                roleEntities,
                permissionEntities,
                assignedRoles,
                rolePermissionEntities,
                directAssignments,
                clock.instant()
        );

        List<UserAuthoritiesResult.RoleAuthority> roleResults =
                roleEntities.stream()
                        .filter(role -> calculated.roles()
                                .contains("ROLE_" + role.getName()))
                        .map(role -> new UserAuthoritiesResult.RoleAuthority(
                                role.getUuid(),
                                role.getName(),
                                assignedRoles.stream()
                                        .filter(assignment -> assignment.getRoleId().equals(role.getId()))
                                        .findFirst()
                                        .map(UserRole::getExpiresAt)
                                        .orElse(null)
                        ))
                        .toList();

        List<UserAuthoritiesResult.DirectPermissionAuthority>
                directPermissionResults = directAssignments.stream()
                .map(assignment -> permissions
                        .findById(assignment.getPermissionId())
                        .map(permission ->
                                new UserAuthoritiesResult
                                        .DirectPermissionAuthority(
                                        permission.getName(),
                                        assignment.getEffect().name()
                                )))
                .flatMap(java.util.Optional::stream)
                .toList();

        return new UserAuthoritiesResult(
                roleResults,
                directPermissionResults,
                List.copyOf(calculated.effectivePermissions())
        );
    }
}
