package kh.edu.istad.platform.iam.application.port.out;

import kh.edu.istad.platform.iam.domain.entity.Identity;
import kh.edu.istad.platform.iam.domain.enums.IdentityProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityRepository {
    Optional<Identity> findByProviderAndSubject(IdentityProvider provider, String subject);

    Optional<Identity> findByUuid(UUID uuid);

    List<Identity> findByUserUuid(UUID userUuid);

    Identity save(Identity identity);
}
