package kh.edu.istad.platform.iam.application.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResult(
        UUID id,
        String actor,
        String action,
        String targetType,
        String targetId,
        String detail,
        String ip,
        Instant occurredAt
) {
}
