package kh.edu.istad.platform.iam.application.dto;

import java.util.UUID;

public record ResetUserPasswordCommand(
        UUID userUuid,
        String newPassword
) {
}
