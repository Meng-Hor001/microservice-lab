package kh.edu.istad.platform.iam.application.port.out;

import kh.edu.istad.platform.iam.application.usecase.ListRolesUseCase;
import kh.edu.istad.platform.iam.domain.entity.RolePermission;

import java.util.List;
import java.util.UUID;

public interface RolePermissionAssignmentRepository {
    List<RolePermission> saveAll(List<RolePermission> assignments);

    RolePermission save(RolePermission assignment);

    List<RolePermission> findActiveByRoleUuid(UUID uuid);

    List<RolePermission> findByRoleIds(List<Long> roleIds);
}
