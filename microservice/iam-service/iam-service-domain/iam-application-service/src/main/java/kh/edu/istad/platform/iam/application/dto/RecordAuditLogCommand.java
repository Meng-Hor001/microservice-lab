package kh.edu.istad.platform.iam.application.dto;

import java.time.Instant;

public record RecordAuditLogCommand(
        String actor,
        String action,
        String targetType,
        String targetId,
        String detail,
        String ip,
        Instant occurredAt
) {
}
