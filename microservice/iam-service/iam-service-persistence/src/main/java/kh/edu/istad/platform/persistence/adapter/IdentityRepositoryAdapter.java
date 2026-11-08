package kh.edu.istad.platform.persistence.adapter;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.exception.VersionConflictException;
import kh.edu.istad.platform.iam.application.port.out.IdentityRepository;
import kh.edu.istad.platform.iam.domain.entity.Identity;
import kh.edu.istad.platform.iam.domain.enums.IdentityProvider;
import kh.edu.istad.platform.persistence.entity.IdentityJpaEntity;
import kh.edu.istad.platform.persistence.mapper.IdentityMapper;
import kh.edu.istad.platform.persistence.repository.IdentityJpaRepository;
import kh.edu.istad.platform.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IdentityRepositoryAdapter implements IdentityRepository {
    private final IdentityJpaRepository identities;
    private final UserJpaRepository users;
    private final IdentityMapper mapper;

    @Override
    public Optional<Identity> findByProviderAndSubject(IdentityProvider provider, String subject) {
        return identities.findByProviderAndProviderSubjectAndDeletedFalse(provider, subject).map(mapper::toDomain);
    }

    @Override
    public Optional<Identity> findByUuid(UUID uuid) {
        return identities.findByUuidAndDeletedFalse(uuid).map(mapper::toDomain);
    }

    @Override
    public List<Identity> findByUserUuid(UUID userUuid) {
        return users.findByUuidAndDeletedFalse(userUuid)
                .map(user -> identities.findByUserIdAndDeletedFalse(user.getId()).stream().map(mapper::toDomain).toList())
                .orElseGet(List::of);
    }

    @Override
    @Transactional
    public Identity save(Identity identity) {
        IdentityJpaEntity entity;
        if (identity.getId() == null) {
            entity = mapper.toNewEntity(identity);
        } else {
            entity = identities.findById(identity.getId())
                    .filter(existing -> !existing.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Identity", identity.getUuid()));
            if (!entity.getUuid().equals(identity.getUuid())) throw new IllegalArgumentException("Identity UUID does not match stored entity");
            if (!entity.getVersion().equals(identity.getVersion())) throw new VersionConflictException("Identity");
            mapper.updateEntity(identity, entity);
        }
        return mapper.toDomain(identities.saveAndFlush(entity));
    }
}
