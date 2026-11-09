package kh.edu.istad.platform.persistence.adapter;

import kh.edu.istad.platform.iam.domain.entity.UserRole;
import kh.edu.istad.platform.persistence.entity.RoleJpaEntity;
import kh.edu.istad.platform.persistence.entity.UserJpaEntity;
import kh.edu.istad.platform.persistence.entity.UserRoleJpaEntity;
import kh.edu.istad.platform.persistence.mapper.UserRoleMapper;
import kh.edu.istad.platform.persistence.repository.RoleJpaRepository;
import kh.edu.istad.platform.persistence.repository.UserJpaRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRoleAssignmentRepository;
import kh.edu.istad.platform.persistence.repository.UserRoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRoleAssignmentRepositoryAdapter implements UserRoleAssignmentRepository {
    private final UserJpaRepository users;
    private final RoleJpaRepository roles;
    private final UserRoleJpaRepository assignments;
    private final UserRoleMapper mapper;

    @Override
    @Transactional
    public List<UserRole> saveAll(List<UserRole> values) {
        return assignments.saveAll(values.stream()
                        .map(mapper::toNewEntity)
                        .toList())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<UserRole> findActiveByUserUuid(UUID userUuid) {
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
    public Optional<UserRole> findActiveByUserUuidAndRoleUuid(UUID userUuid, UUID roleUuid) {
        Long userId = users.findByUuidAndDeletedFalse(userUuid)
                .map(UserJpaEntity::getId)
                .orElse(null);

        if (userId == null){
            return Optional.empty();
        }
        Long roleId = roles.findByUuidAndDeletedFalse(roleUuid)
                .map(RoleJpaEntity::getId)
                .orElse(null);

        if (roleId == null) {
            return Optional.empty();
        }

        return assignments
                .findByUserIdAndRoleIdAndRevokedAtIsNull(userId, roleId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public UserRole save(UserRole assignment) {
        UserRoleJpaEntity entity = assignments
                .findById(assignment.getId())
                .orElseThrow(()-> new IllegalArgumentException(
                        "User role assignment not found :" + assignment.getId()
                ));

        if (!entity.getVersion().equals(assignment.getVersion())){
            throw new IllegalStateException(
                    "User role assignment was changed by another operation"
            );
        }
        mapper.updateEntity(assignment, entity);
        return mapper.toDomain(assignments.saveAndFlush(entity));
    }


}
