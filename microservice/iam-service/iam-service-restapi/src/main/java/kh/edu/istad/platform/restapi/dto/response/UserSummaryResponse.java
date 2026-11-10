package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.application.dto.UserSummary;

import java.util.UUID;

public record UserSummaryResponse(
        UUID uuid, String username, String email, String name, boolean enabled
) {
    public static UserSummaryResponse from(UserSummary user) {
        return new UserSummaryResponse(user.uuid(), user.username(), user.email(), user.name(), user.enabled());
    }
}
