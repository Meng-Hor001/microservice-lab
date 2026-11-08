package kh.edu.istad.platform.iam.application.port.out;

import kh.edu.istad.platform.iam.domain.entity.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleAssignmentRepository {
    List<UserRole> saveAll(List<UserRole> assignments);

    List<UserRole> findActiveByUserUuid(UUID userUuid);

    Optional<UserRole> findActiveByUserUuidAndRoleUuid(UUID userUuid, UUID roleUuid);
    UserRole save(UserRole assignment);

}
