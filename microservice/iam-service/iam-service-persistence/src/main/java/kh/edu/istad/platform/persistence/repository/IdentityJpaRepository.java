package kh.edu.istad.platform.persistence.repository;

import kh.edu.istad.platform.iam.domain.enums.IdentityProvider;
import kh.edu.istad.platform.persistence.entity.IdentityJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityJpaRepository extends JpaRepository<IdentityJpaEntity, Long> {
    Optional<IdentityJpaEntity> findByProviderAndProviderSubjectAndDeletedFalse(IdentityProvider provider, String subject);
    Optional<IdentityJpaEntity> findByUuidAndDeletedFalse(UUID uuid);
    List<IdentityJpaEntity> findByUserIdAndDeletedFalse(Long userId);
}
