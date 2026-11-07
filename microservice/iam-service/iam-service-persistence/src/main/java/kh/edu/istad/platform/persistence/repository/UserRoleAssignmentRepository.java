package kh.edu.istad.platform.persistence.repository;

import kh.edu.istad.platform.iam.domain.entity.UserRole;

import java.util.List;
import java.util.UUID;

public interface UserRoleAssignmentRepository {


    List<UserRole> saveAll(List<UserRole> assignments);

    List<UserRole> findActiveByUserUuid(UUID userUuid);
}
