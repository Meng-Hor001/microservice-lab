package kh.edu.istad.platform.iam.application.dto;

import kh.edu.istad.platform.iam.domain.enums.IdentityProvider;

import java.util.Map;

public record FederatedIdentityCommand(
        IdentityProvider provider,
        String providerSubject,
        String email,
        boolean emailVerified,
        String displayName,
        String username,
        String pictureUrl,
        Map<String, Object> attributes
) {
    public FederatedIdentityCommand {
        attributes = kh.edu.istad.platform.iam.domain.support.JsonAttributes.copy(attributes);
    }
}
