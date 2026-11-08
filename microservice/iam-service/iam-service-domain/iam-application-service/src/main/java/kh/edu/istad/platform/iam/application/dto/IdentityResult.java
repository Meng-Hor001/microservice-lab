package kh.edu.istad.platform.iam.application.dto;

import kh.edu.istad.platform.iam.domain.entity.Identity;
import kh.edu.istad.platform.iam.domain.enums.IdentityProvider;

import java.time.Instant;
import java.util.UUID;

public record IdentityResult(
        UUID uuid,
        IdentityProvider provider,
        String providerSubject,
        String email,
        boolean emailVerified,
        String displayName,
        String username,
        String pictureUrl,
        Instant linkedAt,
        Instant lastLoginAt
) {
    public static IdentityResult from(Identity identity) {
        return new IdentityResult(
                identity.getUuid(),
                identity.getProvider(),
                identity.getProviderSubject(),
                identity.getEmail(),
                identity.isEmailVerified(),
                identity.getDisplayName(),
                identity.getUsername(),
                identity.getPictureUrl(),
                identity.getLinkedAt(),
                identity.getLastLoginAt()
        );
    }
}
