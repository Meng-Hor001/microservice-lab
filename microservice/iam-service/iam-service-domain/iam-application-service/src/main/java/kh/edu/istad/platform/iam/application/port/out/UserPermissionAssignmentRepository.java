package kh.edu.istad.platform.iam.application.port.out;

import kh.edu.istad.platform.iam.domain.entity.UserPermission;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPermissionAssignmentRepository {
    List<UserPermission> saveAll(List<UserPermission> assignments);

    List<UserPermission> findActiveByUserUuid(UUID userUuid);

    Optional<UserPermission> findActiveByUserUuidAndPermissionUuid(UUID userUuid, UUID permissionUuid);

    UserPermission save(UserPermission assignments);
}
