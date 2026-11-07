package kh.edu.istad.platform.persistence.adapter;

import kh.edu.istad.platform.iam.domain.entity.UserRole;
import kh.edu.istad.platform.persistence.entity.UserJpaEntity;
import kh.edu.istad.platform.persistence.mapper.UserRoleMapper;
import kh.edu.istad.platform.persistence.repository.UserJpaRepository;
import kh.edu.istad.platform.persistence.repository.UserRoleAssignmentRepository;
import kh.edu.istad.platform.persistence.repository.UserRoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRoleAssignmentRepositoryAdapter implements UserRoleAssignmentRepository {
    private final UserJpaRepository users;
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
}
