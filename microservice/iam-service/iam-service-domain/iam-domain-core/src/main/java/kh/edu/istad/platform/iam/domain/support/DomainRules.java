package kh.edu.istad.platform.iam.domain.support;

import kh.edu.istad.platform.iam.domain.exception.DomainRuleException;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class DomainRules {
    private DomainRules() {
        // Utility class: do not instantiate.
    }

    public static void check(boolean condition, String message) {
        if (!condition) {
            throw new DomainRuleException(message);
        }
    }

    public static <T> T required(T value, String field) {
        check(value != null, field + " is required");
        return value;
    }

    public static String text(
            String value,
            String field,
            int maxLength
    ) {
        check(
                value != null && !value.isBlank(),
                field + " is required"
        );

        String normalized = value.trim();

        check(
                normalized.length() <= maxLength,
                field + " must not exceed " + maxLength + " characters"
        );

        return normalized;
    }

    public static String optional(
            String value,
            String field,
            int maxLength
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return text(value, field, maxLength);
    }

    public static String email(String value, boolean mandatory) {
        String normalized = mandatory
                ? text(value, "Email", 255)
                : optional(value, "Email", 255);

        if (normalized == null) {
            return null;
        }

        normalized = normalized.toLowerCase(Locale.ROOT);

        // Basic domain validation; API validation may be stricter.
        check(
                normalized.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"),
                "Invalid email address"
        );

        return normalized;
    }

    public static long persistedId(Long id, String field) {
        check(
                id != null && id > 0,
                field + " must be a persisted ID"
        );

        return id;
    }

    public static UUID publicId(Long id, UUID uuid) {
        check(id == null || id > 0, "ID must be positive");

        // Restored records must keep their original UUID.
        if (id != null) {
            required(uuid, "Stored UUID");
        }

        return uuid == null ? UUID.randomUUID() : uuid;
    }

    public static void audit(
            boolean deleted,
            Instant deletedAt,
            String deletedBy,
            Instant createdAt,
            String createdBy,
            Instant updatedAt,
            String updatedBy,
            long version
    ) {
        required(createdAt, "Created timestamp");
        text(createdBy, "Created by", 100);

        required(updatedAt, "Updated timestamp");
        text(updatedBy, "Updated by", 100);

        check(version >= 0, "Version cannot be negative");

        check(
                !updatedAt.isBefore(createdAt),
                "Update cannot precede creation"
        );

        if (deleted) {
            required(deletedAt, "Deleted timestamp");
            text(deletedBy, "Deleted by", 100);

            check(
                    !deletedAt.isBefore(createdAt),
                    "Deletion cannot precede creation"
            );

            check(
                    !updatedAt.isBefore(deletedAt),
                    "Update cannot precede deletion"
            );
        } else {
            check(
                    deletedAt == null && deletedBy == null,
                    "An active record cannot have deletion metadata"
            );
        }
    }

    public static void mutation(
            boolean deleted,
            Instant updatedAt,
            String actor,
            Instant now
    ) {
        check(!deleted, "Record has been deleted");

        required(updatedAt, "Updated timestamp");
        text(actor, "Actor", 100);
        required(now, "Current timestamp");

        check(
                !now.isBefore(updatedAt),
                "Mutation cannot precede the latest update"
        );
    }

    public static void assignment(
            Long id,
            Instant assignedAt,
            String assignedBy,
            Instant expiresAt,
            Instant revokedAt,
            String revokedBy,
            String reason,
            long version
    ) {
        check(
                id == null || id > 0,
                "Assignment ID must be positive"
        );

        required(assignedAt, "Assigned timestamp");
        text(assignedBy, "Assigned by", 100);
        optional(reason, "Reason", 255);

        check(version >= 0, "Version cannot be negative");

        if (expiresAt != null) {
            check(
                    expiresAt.isAfter(assignedAt),
                    "Expiry must be after assignment"
            );
        }

        if (revokedAt == null) {
            check(
                    revokedBy == null,
                    "Revoked by requires a revocation time"
            );
        } else {
            text(revokedBy, "Revoked by", 100);

            check(
                    !revokedAt.isBefore(assignedAt),
                    "Revocation cannot precede assignment"
            );
        }
    }

    public static boolean activeAssignment(
            Instant assignedAt,
            Instant expiresAt,
            Instant revokedAt,
            Instant now
    ) {
        required(assignedAt, "Assigned timestamp");
        required(now, "Current timestamp");

        return !now.isBefore(assignedAt)
                && revokedAt == null
                && (expiresAt == null || expiresAt.isAfter(now));
    }

    public static void revocation(
            Instant assignedAt,
            String actor,
            Instant now
    ) {
        required(assignedAt, "Assigned timestamp");
        text(actor, "Actor", 100);
        required(now, "Revocation timestamp");

        check(
                !now.isBefore(assignedAt),
                "Revocation cannot precede assignment"
        );
    }
}
