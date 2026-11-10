package kh.edu.istad.platform.iam.domain.entity;

import static kh.edu.istad.platform.iam.domain.support.DomainRules.*;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserRole {
    private Long id;
    private Long userId;
    private Long roleId;

    private Instant assignedAt;
    private String assignedBy;
    private Instant expiresAt;
    private Instant revokedAt;
    private String revokedBy;
    private String reason;

    private long version;

    public static class UserRoleBuilder {

        public UserRole build() {
            UserRole assignment = new UserRole(
                    id, userId, roleId,
                    assignedAt, assignedBy,
                    expiresAt, revokedAt, revokedBy,
                    reason, version
            );

            assignment.validate();
            return assignment;
        }
    }

    private void validate() {
        persistedId(userId, "User ID");
        persistedId(roleId, "Role ID");

        assignment(
                id,
                assignedAt, assignedBy,
                expiresAt,
                revokedAt, revokedBy,
                reason, version
        );
    }

    public static UserRole assign(
            User user,
            Role role,
            String actor,
            Instant now,
            Instant expiresAt,
            String reason
    ) {
        required(user, "User");
        required(role, "Role");

        check(!user.isDeleted(), "Deleted users cannot receive roles");
        role.ensureAssignable();

        return UserRole.builder()
                .userId(persistedId(user.getId(), "User ID"))
                .roleId(persistedId(role.getId(), "Role ID"))
                .assignedAt(now)
                .assignedBy(actor)
                .expiresAt(expiresAt)
                .reason(reason)
                .build();
    }

    public boolean isActiveAt(Instant now) {
        return activeAssignment(
                assignedAt, expiresAt, revokedAt, now
        );
    }

    public void revoke(String actor, String reason, Instant now) {
        if (revokedAt != null) {
            return;
        }

        String normalizedReason = optional(reason, "Reason", 255);
        revocation(assignedAt, actor, now);

        revokedAt = now;
        revokedBy = actor.trim();
        this.reason = normalizedReason;
    }
}
