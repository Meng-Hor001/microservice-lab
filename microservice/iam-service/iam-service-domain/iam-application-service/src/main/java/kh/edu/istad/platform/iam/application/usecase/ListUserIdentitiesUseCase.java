package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.IdentityResult;
import kh.edu.istad.platform.iam.application.port.out.IdentityRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ListUserIdentitiesUseCase {
    private final IdentityRepository identities;
    private final UserRepository users;

    public ListUserIdentitiesUseCase(IdentityRepository identities, UserRepository users) {
        this.identities = Objects.requireNonNull(identities);
        this.users = Objects.requireNonNull(users);
    }

    public List<IdentityResult> execute(UUID userUuid) {
        Objects.requireNonNull(userUuid, "User UUID is required");

        users.findByUuid(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User", userUuid));

        return identities.findByUserUuid(userUuid).stream()
                .filter(identity -> !identity.isDeleted())
                .map(IdentityResult::from)
                .toList();
    }
}
