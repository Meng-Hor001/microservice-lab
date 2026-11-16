package kh.edu.istad.platform.iam.application.dto;

import java.time.Instant;
import java.util.UUID;

public record AccountActivityResult(
        UUID id,
        String type,
        String detail,
        Instant at
) {
}
