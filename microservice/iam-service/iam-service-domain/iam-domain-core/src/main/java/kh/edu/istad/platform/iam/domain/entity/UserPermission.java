package kh.edu.istad.platform.iam.domain.entity;

import static kh.edu.istad.platform.iam.domain.support.DomainRules.*;


import kh.edu.istad.platform.iam.domain.enums.PermissionEffect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserPermission {
    private Long id;
    private Long userId;
    private Long permissionId;
    private PermissionEffect effect;

    private Instant assignedAt;
    private String assignedBy;
    private Instant expiresAt;
    private Instant revokedAt;
    private String revokedBy;
    private String reason;

    private long version;

    public static class UserPermissionBuilder {

        public UserPermission build() {
            UserPermission assignment = new UserPermission(
                    id, userId, permissionId, effect,
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
        persistedId(permissionId, "Permission ID");
        required(effect, "Permission effect");

        assignment(
                id,
                assignedAt, assignedBy,
                expiresAt,
                revokedAt, revokedBy,
                reason, version
        );
    }

    public static UserPermission assign(
            User user,
            Permission permission,
            PermissionEffect effect,
            String actor,
            Instant now,
            Instant expiresAt,
            String reason
    ) {
        required(user, "User");
        required(permission, "Permission");

        check(
                !user.isDeleted(),
                "Deleted users cannot receive permissions"
        );

        permission.ensureAssignable();

        return UserPermission.builder()
                .userId(persistedId(user.getId(), "User ID"))
                .permissionId(
                        persistedId(permission.getId(), "Permission ID")
                )
                .effect(effect)
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
