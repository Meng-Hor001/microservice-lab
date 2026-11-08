package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.AuthUserResult;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import java.util.Objects;

public final class LoadUserForAuthenticationUseCase {
    private final UserRepository users;

    public LoadUserForAuthenticationUseCase(UserRepository users) {
        this.users = Objects.requireNonNull(users);
    }

    public AuthUserResult execute(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new IllegalArgumentException("Username or email is required");
        }
        return users.findByUsernameOrEmail(usernameOrEmail.trim())
                .map(AuthUserResult::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", usernameOrEmail
                ));
    }
}
