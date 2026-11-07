package kh.edu.istad.platform.iam.application.dto;

import kh.edu.istad.platform.iam.domain.entity.User;

import java.util.UUID;

public record UserSummary(
        UUID uuid,
        String username,
        String email,
        String name,
        boolean enabled
) {
    public static UserSummary from(User user) {
        return new UserSummary(
                user.getUuid(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.isEnabled()
        );
    }
}
