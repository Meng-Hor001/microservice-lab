package kh.edu.istad.platform.iam.domain.entity;

import static kh.edu.istad.platform.iam.domain.support.DomainRules.*;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Permission {

    private Long id;
    private UUID uuid;

    private String name;
    private String resource;
    private String action;
    private String description;

    private boolean system;
    private boolean active;

    private boolean deleted;
    private Instant deletedAt;
    private String deletedBy;

    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    private long version;

    public static class PermissionBuilder {

        private boolean active = true;

        public Permission build() {
            Permission permission = new Permission(
                    id, uuid,
                    name, resource, action, description,
                    system, active,
                    deleted, deletedAt, deletedBy,
                    createdAt, createdBy,
                    updatedAt == null ? createdAt : updatedAt,
                    updatedBy == null ? createdBy : updatedBy,
                    version
            );

            permission.validate();
            return permission;
        }
    }

    private static String permissionPart(String value, String field) {
        String normalized = text(value, field, 50);

        check(
                normalized.matches("[a-z][a-z0-9_-]*"),
                field + " must use lowercase letters, digits, _ or -"
        );

        return normalized;
    }

    private void validate() {
        uuid = publicId(id, uuid);

        resource = permissionPart(resource, "Resource");
        action = permissionPart(action, "Action");

        String expectedName = text(
                resource + ":" + action,
                "Permission name",
                100
        );

        check(
                name == null || name.equals(expectedName),
                "Permission name must equal resource:action"
        );

        name = expectedName;
        description = optional(description, "Description", 255);

        audit(
                deleted, deletedAt, deletedBy,
                createdAt, createdBy,
                updatedAt, updatedBy,
                version
        );
    }

    public void update(
            String resource,
            String action,
            String description,
            String actor,
            Instant now
    ) {
        String newResource = permissionPart(resource, "Resource");
        String newAction = permissionPart(action, "Action");

        String newName = text(
                newResource + ":" + newAction,
                "Permission name",
                100
        );

        String newDescription = optional(
                description, "Description", 255
        );

        touch(actor, now);

        this.resource = newResource;
        this.action = newAction;
        this.name = newName;
        this.description = newDescription;
    }

    public void activate(String actor, Instant now) {
        touch(actor, now);
        active = true;
    }

    public void deactivate(String actor, Instant now) {
        touch(actor, now);
        active = false;
    }

    public void softDelete(String actor, Instant now) {
        touch(actor, now);

        deleted = true;
        deletedAt = now;
        deletedBy = actor.trim();
        active = false;
    }

    public void ensureAssignable() {
        check(!deleted, "Deleted permissions cannot be assigned");
        check(active, "Inactive permissions cannot be assigned");
    }

    private void touch(String actor, Instant now) {
        check(!system, "System permissions cannot be modified or deleted");
        mutation(deleted, updatedAt, actor, now);

        updatedAt = now;
        updatedBy = actor.trim();
    }

}
