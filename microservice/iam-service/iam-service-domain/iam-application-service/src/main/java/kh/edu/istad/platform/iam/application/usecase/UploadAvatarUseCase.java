package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.UploadAvatarCommand;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.UserAvatarRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.entity.User;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/** Saves a profile photo. Returns when it was saved, so the photo's URL can change with each upload. */
public final class UploadAvatarUseCase {
    public static final int MAX_BYTES = 2 * 1024 * 1024;
    private static final Set<String> TYPES = Set.of("image/png", "image/jpeg", "image/webp");

    private final UserRepository users;
    private final UserAvatarRepository avatars;
    private final Clock clock;

    public UploadAvatarUseCase(UserRepository users, UserAvatarRepository avatars, Clock clock) {
        this.users = Objects.requireNonNull(users);
        this.avatars = Objects.requireNonNull(avatars);
        this.clock = Objects.requireNonNull(clock);
    }

    public Instant execute(UploadAvatarCommand command) {
        Objects.requireNonNull(command, "Command is required");
        byte[] data = command.data();
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Choose an image to upload");
        }
        if (data.length > MAX_BYTES) {
            throw new IllegalArgumentException("Image must be 2 MB or smaller");
        }
        // Trust the file's first bytes, not the name or the type the browser claims.
        String type = detectType(data);
        if (type == null || !TYPES.contains(type)) {
            throw new IllegalArgumentException("Image must be PNG, JPEG or WebP");
        }

        User user = users.findByUuid(command.userUuid())
                .orElseThrow(() -> new ResourceNotFoundException("User", command.userUuid()));
        Instant now = clock.instant();
        avatars.save(user.getId(), type, data, now);
        return now;
    }

    private static String detectType(byte[] d) {
        if (d.length >= 8 && (d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G') return "image/png";
        if (d.length >= 3 && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF) return "image/jpeg";
        if (d.length >= 12 && d[0] == 'R' && d[1] == 'I' && d[2] == 'F' && d[3] == 'F'
                && d[8] == 'W' && d[9] == 'E' && d[10] == 'B' && d[11] == 'P') return "image/webp";
        return null;
    }
}
