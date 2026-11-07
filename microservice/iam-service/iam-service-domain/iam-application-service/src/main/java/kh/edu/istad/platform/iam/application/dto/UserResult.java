package kh.edu.istad.platform.iam.application.dto;

import kh.edu.istad.platform.iam.domain.entity.User;

import java.time.Instant;
import java.util.UUID;

public record UserResult(
        UUID uuid,
        String username,
        String email,
        boolean emailVerified,
        String givenName,
        String familyName,
        String locale,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
    public static UserResult from(User user) {
        return new UserResult(
                user.getUuid(),
                user.getUsername(),
                user.getEmail(),
                user.isEmailVerified(),
                user.getGivenName(),
                user.getFamilyName(),
                user.getLocale(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getVersion()
        );
    }
}
