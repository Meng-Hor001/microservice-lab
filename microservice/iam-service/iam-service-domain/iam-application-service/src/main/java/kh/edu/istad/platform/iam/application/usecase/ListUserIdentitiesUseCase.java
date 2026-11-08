package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.IdentityResult;
import kh.edu.istad.platform.iam.application.port.out.IdentityRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ListUserIdentitiesUseCase {
    private final IdentityRepository identities;

    public ListUserIdentitiesUseCase(IdentityRepository identities) {
        this.identities = Objects.requireNonNull(identities);
    }

    public List<IdentityResult> execute(UUID userUuid) {
        Objects.requireNonNull(userUuid, "User UUID is required");

        return identities.findByUserUuid(userUuid).stream()
                .filter(identity -> !identity.isDeleted())
                .map(IdentityResult::from)
                .toList();
    }
}
