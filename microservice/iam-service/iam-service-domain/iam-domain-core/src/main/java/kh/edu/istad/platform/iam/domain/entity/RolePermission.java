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
public class RolePermission {
    private Long id;
    private Long roleId;
    private Long permissionId;

    private Instant assignedAt;
    private String assignedBy;
    private Instant expiresAt;
    private Instant revokedAt;
    private String revokedBy;
    private String reason;

    private long version;

    public static class RolePermissionBuilder {

        public RolePermission build() {
            RolePermission assignment = new RolePermission(
                    id, roleId, permissionId,
                    assignedAt, assignedBy,
                    expiresAt, revokedAt, revokedBy,
                    reason, version
            );

            assignment.validate();
            return assignment;
        }
    }

    private void validate() {
        persistedId(roleId, "Role ID");
        persistedId(permissionId, "Permission ID");

        assignment(
                id,
                assignedAt, assignedBy,
                expiresAt,
                revokedAt, revokedBy,
                reason, version
        );
    }

    public static RolePermission assign(
            Role role,
            Permission permission,
            String actor,
            Instant now,
            Instant expiresAt,
            String reason
    ) {
        required(role, "Role");
        required(permission, "Permission");

        role.ensureAssignable();
        permission.ensureAssignable();

        check(
                !role.isSystem(),
                "System role permissions cannot be modified"
        );

        return RolePermission.builder()
                .roleId(persistedId(role.getId(), "Role ID"))
                .permissionId(
                        persistedId(permission.getId(), "Permission ID")
                )
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

    public void revoke(
            Role owner,
            String actor,
            String reason,
            Instant now
    ) {
        required(owner, "Role");

        check(roleId.equals(owner.getId()), "Role does not own assignment");
        check(!owner.isDeleted(), "Role has been deleted");

        check(
                !owner.isSystem(),
                "System role permissions cannot be modified"
        );

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
