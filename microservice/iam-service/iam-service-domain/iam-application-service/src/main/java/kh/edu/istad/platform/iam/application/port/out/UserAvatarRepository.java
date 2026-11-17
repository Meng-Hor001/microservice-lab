package kh.edu.istad.platform.iam.application.port.out;

import kh.edu.istad.platform.iam.application.dto.UserAvatarResult;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserAvatarRepository {

    /** Stores the user's photo, replacing any earlier one. */
    void save(Long userId, String contentType, byte[] data, Instant now);

    /** The photo of a user who isn't deleted, if they uploaded one. */
    Optional<UserAvatarResult> findByUserUuid(UUID userUuid);

    void deleteByUserId(Long userId);
}
