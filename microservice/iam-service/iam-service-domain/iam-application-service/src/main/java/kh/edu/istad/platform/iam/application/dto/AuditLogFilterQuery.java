package kh.edu.istad.platform.iam.application.dto;

import java.time.Instant;

public record AuditLogFilterQuery (
        String action,
        String actor,
        String targetType,
        String targetId,
        Instant from,
        Instant to,
        int page,
        int size
) {
    public AuditLogFilterQuery {
        if (page < 0) throw new IllegalArgumentException("Page cannot be negative");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Page size must be between 1 and 100");
        if (from != null && to != null && from.isAfter(to)) throw new IllegalArgumentException("'from' must be before 'to'");
        action = blankToNull(action);
        actor = blankToNull(actor);
        targetType = blankToNull(targetType);
        targetId = blankToNull(targetId);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
