package kh.edu.istad.platform.persistence.adapter;

import jakarta.persistence.criteria.Predicate;
import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.exception.VersionConflictException;
import kh.edu.istad.platform.iam.application.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.application.query.PermissionFilterQuery;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.persistence.entity.PermissionJpaEntity;
import kh.edu.istad.platform.persistence.mapper.PermissionMapper;
import kh.edu.istad.platform.persistence.repository.PermissionJpaRepository;
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
public class PermissionRepositoryAdapter implements PermissionRepository {
    private final PermissionJpaRepository repository;
    private final PermissionMapper mapper;

    @Override
    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    @Override
    public Optional<Permission> findByUuid(UUID uuid) {
        return repository.findByUuidAndDeletedFalse(uuid).map(mapper::toDomain);
    }

    @Override
    public Optional<Permission> findById(Long id) {
        return repository.findById(id)
                .filter(entity -> !entity.isDeleted())
                .map(mapper::toDomain);
    }

    @Override
    public PageResult<Permission> findAll(PermissionFilterQuery query) {
        Specification<PermissionJpaEntity> specification =
                (root, criteriaQuery, cb) -> {
                    var predicates = new ArrayList<Predicate>();

                    predicates.add(cb.isFalse(root.get("deleted")));

                    if (query.resource() != null) {
                        predicates.add(
                                cb.equal(root.get("resource"), query.resource())
                        );
                    }

                    if (query.active() != null) {
                        predicates.add(
                                cb.equal(root.get("active"), query.active())
                        );
                    }

                    if (query.search() != null) {
                        String pattern = "%"
                                + escapeLike(
                                query.search().toLowerCase(Locale.ROOT)
                        )
                                + "%";

                        predicates.add(cb.or(
                                cb.like(
                                        cb.lower(root.<String>get("name")),
                                        pattern,
                                        '\\'
                                ),
                                cb.like(
                                        cb.lower(root.<String>get("description")),
                                        pattern,
                                        '\\'
                                )
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
                page.getContent().stream()
                        .map(mapper::toDomain)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
    @Override
    @Transactional
    public Permission save(Permission permission) {
        PermissionJpaEntity entity;

        if (permission.getId() == null) {
            entity = mapper.toNewEntity(permission);
        } else {
            entity = repository.findById(permission.getId())
                    .filter(existing -> !existing.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Permission", permission.getUuid()
                    ));

            if (!Objects.equals(entity.getUuid(), permission.getUuid())) {
                throw new IllegalArgumentException(
                        "Permission UUID does not match the stored entity"
                );
            }

            if (!Objects.equals(
                    entity.getVersion(),
                    Long.valueOf(permission.getVersion())
            )) {
                throw new VersionConflictException("Permission");
            }

            mapper.updateEntity(permission, entity);
        }


        return mapper.toDomain(repository.saveAndFlush(entity));
    }
}

