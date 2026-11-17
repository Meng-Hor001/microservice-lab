package kh.edu.istad.platform.iam.application.dto;

import kh.edu.istad.platform.iam.domain.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserSummary(
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
    public UserSummary {
        roles = roles == null ? List.of() : List.copyOf(roles);
    }

    public static UserSummary from(User user, List<String> roles) {
        return new UserSummary(
                user.getUuid(),
                user.getUsername(),
                user.getEmail(),
                user.isEmailVerified(),
                user.getName(),
                user.getPictureUrl(),
                user.getPhoneNumber(),
                roles,
                user.isEnabled(),
                !user.isAccountNonLocked(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }
}
