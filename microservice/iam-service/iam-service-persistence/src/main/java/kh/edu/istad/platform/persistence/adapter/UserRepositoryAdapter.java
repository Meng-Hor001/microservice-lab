package kh.edu.istad.platform.persistence.adapter;

import jakarta.persistence.criteria.Predicate;
import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.exception.VersionConflictException;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.application.query.UserFilterQuery;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.persistence.entity.UserJpaEntity;
import kh.edu.istad.platform.persistence.mapper.UserMapper;
import kh.edu.istad.platform.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository repository;
    private final UserMapper mapper;
    @Override
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByUuid(UUID uuid) {
        return repository.findByUuidAndDeletedFalse(uuid)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id)
                .filter(user -> !user.isDeleted())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return repository.findByUsernameOrEmailAndDeletedFalse(usernameOrEmail)
                .map(mapper::toDomain);
    }

    @Override
    public PageResult<User> findAll(UserFilterQuery query) {
        Specification<UserJpaEntity> specification = (root, criteria, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (query.enabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), query.enabled()));
            }

            if (query.search() != null) {
                String pattern = "%"
                        + query.search().toLowerCase(Locale.ROOT)
                        + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("username")), pattern),
                        cb.like(cb.lower(root.<String>get("email")), pattern)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
        Sort.Direction direction = query.descending()
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        var page = repository.findAll(
                specification,
                PageRequest.of(
                        query.page(),
                        query.size(),
                        Sort.by(direction, query.sort()).and(Sort.by("id"))
                )
        );

        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    @Transactional
    public User save(User user) {
        UserJpaEntity entity;
        if (user.getId() == null) {
            entity = mapper.toNewEntity(user);
        } else {
            entity = repository.findById(user.getId())
                    .filter(existing -> !existing.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User", user.getUuid()
                    ));

            if (!entity.getUuid().equals(user.getUuid())) {
                throw new IllegalArgumentException(
                        "User UUID does not match stored entity"
                );
            }

            if (!entity.getVersion().equals(user.getVersion())) {
                throw new VersionConflictException("User");
            }

            mapper.updateEntity(user, entity);
        }

        return mapper.toDomain(repository.saveAndFlush(entity));

    }
}
