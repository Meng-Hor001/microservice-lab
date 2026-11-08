package kh.edu.istad.platform.iam.application.dto;

import kh.edu.istad.platform.iam.domain.entity.User;
import java.util.UUID;

public record AuthUserResult(
        UUID uuid,
        String username,
        String email,
        String passwordHash,
        boolean enabled,
        boolean accountNonExpired,
        boolean accountNonLocked,
        boolean credentialsNonExpired
) {
    public static AuthUserResult from(User user) {
        return new AuthUserResult(
                user.getUuid(), user.getUsername(), user.getEmail(),
                user.getPasswordHash(), user.isEnabled(),
                user.isAccountNonExpired(), user.isAccountNonLocked(),
                user.isCredentialsNonExpired()
        );
    }
}
