package kh.edu.istad.platform.iam.application.dto;

import java.time.Instant;
import java.util.UUID;

public record SuspiciousLoginResult(
        UUID userId,
        String username,
        long failedCount,
        long distinctIps,
        Instant lastFailedAt
) {
}
