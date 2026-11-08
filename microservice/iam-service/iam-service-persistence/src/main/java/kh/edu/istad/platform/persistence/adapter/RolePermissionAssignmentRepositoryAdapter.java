package kh.edu.istad.platform.persistence.adapter;

import jakarta.persistence.EntityNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.RolePermissionAssignmentRepository;
import kh.edu.istad.platform.iam.domain.entity.RolePermission;
import kh.edu.istad.platform.persistence.entity.RoleJpaEntity;
import kh.edu.istad.platform.persistence.entity.RolePermissionJpaEntity;
import kh.edu.istad.platform.persistence.mapper.RolePermissionMapper;
import kh.edu.istad.platform.persistence.repository.RoleJpaRepository;
import kh.edu.istad.platform.persistence.repository.RolePermissionJpaRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RolePermissionAssignmentRepositoryAdapter implements RolePermissionAssignmentRepository {
    private final RoleJpaRepository roles;
    private final RolePermissionJpaRepository assignments;
    private final RolePermissionMapper mapper;

    @Override
    public List<RolePermission> findByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return List.of();
        return assignments.findByRoleIdInAndRevokedAtIsNull(roleIds).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public List<RolePermission> saveAll(List<RolePermission> assignmentsToSave) {
        var entities = assignmentsToSave.stream()
                .map(mapper::toNewEntity)
                .toList();

        return assignments.saveAll(entities).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public RolePermission save(RolePermission assignment) {
        RolePermissionJpaEntity entity;

        if (assignment.getId() == null) {
            entity = mapper.toNewEntity(assignment);
        } else {
            entity = assignments.findById(assignment.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Role permission assignment not found: "
                                    + assignment.getId()
                    ));

            if (!entity.getVersion().equals(assignment.getVersion())) {
                throw new IllegalStateException(
                        "Role permission assignment was changed by another operation"
                );
            }

            mapper.updateEntity(assignment, entity);
        }

        return mapper.toDomain(assignments.saveAndFlush(entity));
    }

    @Override
    public List<RolePermission> findActiveByRoleUuid(UUID roleUuid) {
        Long roleId = roles.findByUuidAndDeletedFalse(roleUuid)
                .map(RoleJpaEntity::getId)
                .orElse(null);

        if (roleId == null) {
            return List.of();
        }

        return assignments.findByRoleIdAndRevokedAtIsNull(roleId).stream()
                .map(mapper::toDomain)
                .toList();
    }

}
