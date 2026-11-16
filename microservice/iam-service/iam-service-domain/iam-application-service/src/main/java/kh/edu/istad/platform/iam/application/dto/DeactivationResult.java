package kh.edu.istad.platform.iam.application.dto;

import java.time.Instant;

public record DeactivationResult(
        String status,
        Instant requestedAt
) {
}
