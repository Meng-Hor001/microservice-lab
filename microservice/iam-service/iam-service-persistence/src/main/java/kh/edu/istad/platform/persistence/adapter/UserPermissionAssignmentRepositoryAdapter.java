package kh.edu.istad.platform.persistence.adapter;

import kh.edu.istad.platform.iam.application.port.out.UserPermissionAssignmentRepository;
import kh.edu.istad.platform.iam.domain.entity.UserPermission;
import kh.edu.istad.platform.persistence.entity.PermissionJpaEntity;
import kh.edu.istad.platform.persistence.entity.UserJpaEntity;
import kh.edu.istad.platform.persistence.mapper.UserPermissionMapper;
import kh.edu.istad.platform.persistence.repository.PermissionJpaRepository;
import kh.edu.istad.platform.persistence.repository.UserJpaRepository;
import kh.edu.istad.platform.persistence.repository.UserPermissionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPermissionAssignmentRepositoryAdapter implements UserPermissionAssignmentRepository {
    private final UserJpaRepository users;
    private final UserPermissionJpaRepository assignments;
    private final PermissionJpaRepository permissions;
    private final UserPermissionMapper mapper;

    @Override
    @Transactional
    public List<UserPermission> saveAll(List<UserPermission> values) {
        return assignments.saveAll(values.stream()
                        .map(mapper::toNewEntity)
                        .toList())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<UserPermission> findActiveByUserUuid(UUID userUuid) {
        Long userId = users.findByUuidAndDeletedFalse(userUuid)
                .map(UserJpaEntity::getId)
                .orElse(null);

        if (userId == null) {
            return List.of();
        }

        return assignments.findByUserIdAndRevokedAtIsNull(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<UserPermission> findActiveByUserUuidAndPermissionUuid(UUID userUuid, UUID permissionUuid) {
        Long userId = users.findByUuidAndDeletedFalse(userUuid)
                .map(UserJpaEntity::getId)
                .orElse(null);

        if (userId == null) {
            return Optional.empty();
        }

        Long permissionId = permissions.findByUuidAndDeletedFalse(permissionUuid)
                .map(PermissionJpaEntity::getId)
                .orElse(null);

        if (permissionId == null) {
            return Optional.empty();
        }

        return assignments
                .findByUserIdAndPermissionIdAndRevokedAtIsNull(userId, permissionId)
                .map(mapper::toDomain);
    }

    @Override
    public UserPermission save(UserPermission assignments) {
        return null;
    }
}
