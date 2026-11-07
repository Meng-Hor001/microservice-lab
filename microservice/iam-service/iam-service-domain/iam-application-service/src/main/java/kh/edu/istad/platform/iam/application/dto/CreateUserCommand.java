package kh.edu.istad.platform.iam.application.dto;

import kh.edu.istad.platform.iam.domain.valueobject.UserProfile;

public record CreateUserCommand(
        String username,
        String password,
        UserProfile profile
) {
}
