package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.UserResult;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;

import java.util.Objects;
import java.util.UUID;

public final class GetUserUseCase {
    private final UserRepository users;

    public GetUserUseCase(UserRepository users) {
        this.users = Objects.requireNonNull(users);
    }

    public UserResult execute(UUID userUuid) {
        Objects.requireNonNull(userUuid, "User UUID is required");

        return users.findByUuid(userUuid)
                .map(UserResult::from)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", userUuid));
    }
}
