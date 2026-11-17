package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.UserAvatarRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.entity.User;

import java.util.Objects;
import java.util.UUID;

public final class DeleteAvatarUseCase {
    private final UserRepository users;
    private final UserAvatarRepository avatars;

    public DeleteAvatarUseCase(UserRepository users, UserAvatarRepository avatars) {
        this.users = Objects.requireNonNull(users);
        this.avatars = Objects.requireNonNull(avatars);
    }

    public void execute(UUID userUuid) {
        User user = users.findByUuid(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User", userUuid));
        avatars.deleteByUserId(user.getId());
    }
}
