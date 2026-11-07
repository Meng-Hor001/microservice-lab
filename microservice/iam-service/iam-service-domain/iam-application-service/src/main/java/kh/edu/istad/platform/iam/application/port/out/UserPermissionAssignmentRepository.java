package kh.edu.istad.platform.iam.application.port.out;

import kh.edu.istad.platform.iam.domain.entity.UserPermission;

import java.util.List;
import java.util.UUID;

public interface UserPermissionAssignmentRepository {
    List<UserPermission> saveAll(List<UserPermission> assignments);

    List<UserPermission> findActiveByUserUuid(UUID userUuid);
}
