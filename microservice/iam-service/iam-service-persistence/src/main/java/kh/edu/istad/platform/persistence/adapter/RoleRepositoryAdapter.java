package kh.edu.istad.platform.persistence.adapter;

import jakarta.persistence.criteria.Predicate;
import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.exception.VersionConflictException;
import kh.edu.istad.platform.iam.application.port.out.RoleRepository;
import kh.edu.istad.platform.iam.application.query.RoleFilterQuery;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.persistence.entity.RoleJpaEntity;
import kh.edu.istad.platform.persistence.mapper.RoleMapper;
import kh.edu.istad.platform.persistence.repository.RoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleRepositoryAdapter implements RoleRepository {
    private final RoleJpaRepository repository;
    private final RoleMapper mapper;

    @Override
    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    @Override
    public Optional<Role> findByUuid(UUID uuid) {
        return repository.findByUuidAndDeletedFalse(uuid)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Role save(Role role) {
        RoleJpaEntity entity;

        if (role.getId() == null) {
            entity = mapper.toNewEntity(role);
        } else {
            entity = repository.findById(role.getId())
                    .filter(existing -> !existing.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Role", role.getUuid()
                    ));

            if (!entity.getUuid().equals(role.getUuid())) {
                throw new IllegalArgumentException(
                        "Role UUID does not match stored entity"
                );
            }

            if (entity.getVersion() != role.getVersion()) {
                throw new VersionConflictException("Role");
            }

            mapper.updateEntity(role, entity);
        }

        return mapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public Optional<Long> findIdByUuid(UUID uuid) {
        return repository.findByUuidAndDeletedFalse(uuid)
                .map(RoleJpaEntity::getId);
    }

    @Override
    public PageResult<Role> findAll(RoleFilterQuery query) {
        Specification<RoleJpaEntity> specification = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (query.active() != null) {
                predicates.add(cb.equal(root.get("active"), query.active()));
            }

            if (query.search() != null) {
                String pattern = "%" + query.search().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.like(
                        cb.lower(root.<String>get("name")),
                        pattern
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Sort.Direction direction = query.descending()
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, query.sort())
                .and(Sort.by("id"));

        var page = repository.findAll(
                specification,
                PageRequest.of(query.page(), query.size(), sort)
        );

        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
