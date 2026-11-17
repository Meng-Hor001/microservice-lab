package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.UserAvatarResult;
import kh.edu.istad.platform.iam.application.port.out.UserAvatarRepository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class GetAvatarUseCase {
    private final UserAvatarRepository avatars;

    public GetAvatarUseCase(UserAvatarRepository avatars) {
        this.avatars = Objects.requireNonNull(avatars);
    }

    public Optional<UserAvatarResult> execute(UUID userUuid) {
        return avatars.findByUserUuid(userUuid);
    }
}
