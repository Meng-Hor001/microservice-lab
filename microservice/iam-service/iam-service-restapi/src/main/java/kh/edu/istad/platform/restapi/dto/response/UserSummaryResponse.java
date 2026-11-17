package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.application.dto.UserSummary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserSummaryResponse(
        UUID uuid,
        String username,
        String email,
        boolean emailVerified,
        String name,
        String pictureUrl,
        String phoneNumber,
        List<String> roles,
        boolean enabled,
        boolean locked,
        Instant lastLoginAt,
        Instant createdAt
) {
    public static UserSummaryResponse from(UserSummary user) {
        return new UserSummaryResponse(
                user.uuid(), user.username(), user.email(), user.emailVerified(), user.name(),
                user.pictureUrl(), user.phoneNumber(), user.roles(), user.enabled(), user.locked(),
                user.lastLoginAt(), user.createdAt()
        );
    }
}
