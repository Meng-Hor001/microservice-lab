package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.application.dto.UserResult;
import kh.edu.istad.platform.iam.application.dto.UserAuthoritiesResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID uuid,
        String username,
        String email,
        boolean emailVerified,
        String name,
        String locale,
        boolean enabled,
        List<RoleReferenceResponse> roles,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
    public UserResponse {
        roles = List.copyOf(roles);
    }

    public static UserResponse from(UserResult user, UserAuthoritiesResult authorities) {
        return new UserResponse(
                user.uuid(), user.username(), user.email(), user.emailVerified(),
                user.givenName() + " " + user.familyName(), user.locale(), user.enabled(),
                authorities.roles().stream().map(RoleReferenceResponse::from).toList(),
                user.createdAt(), user.updatedAt(), user.version()
        );
    }
}
