package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.application.dto.IdentityResult;

import java.time.Instant;
import java.util.UUID;

public record IdentityResponse(
        UUID uuid,
        String provider,
        String email,
        String displayName,
        Instant linkedAt
) {
    public static IdentityResponse from(IdentityResult identity) {
        return new IdentityResponse(
                identity.uuid(),
                identity.provider().name(),
                identity.email(),
                identity.displayName(),
                identity.linkedAt());
    }
}
