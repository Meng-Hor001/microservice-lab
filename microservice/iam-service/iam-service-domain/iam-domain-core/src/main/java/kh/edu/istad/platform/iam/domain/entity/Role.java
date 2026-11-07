package kh.edu.istad.platform.iam.domain.entity;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


import static kh.edu.istad.platform.iam.domain.support.DomainRules.*;
import java.time.Instant;
import java.util.UUID;
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Role {
    private Long id;
    private UUID uuid;
    private String name;
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

    public static class RoleBuilder {

        private boolean active = true;

        public Role build() {
            Role role = new Role(
                    id, uuid, name, description,
                    system, active,
                    deleted, deletedAt, deletedBy,
                    createdAt, createdBy,
                    updatedAt == null ? createdAt : updatedAt,
                    updatedBy == null ? createdBy : updatedBy,
                    version
            );

            role.validate();
            return role;
        }
    }

    private void validate() {
        uuid = publicId(id, uuid);
        name = text(name, "Role name", 50);
        description = optional(description, "Description", 255);

        audit(
                deleted, deletedAt, deletedBy,
                createdAt, createdBy,
                updatedAt, updatedBy,
                version
        );
    }

    public void rename(String name, String actor, Instant now) {
        String normalized = text(name, "Role name", 50);
        touch(actor, now);
        this.name = normalized;
    }

    public void updateDescription(
            String description,
            String actor,
            Instant now
    ) {
        String normalized = optional(description, "Description", 255);
        touch(actor, now);
        this.description = normalized;
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
        check(!deleted, "Deleted roles cannot be assigned");
        check(active, "Inactive roles cannot be assigned");
    }

    private void touch(String actor, Instant now) {
        check(!system, "System roles cannot be modified or deleted");
        mutation(deleted, updatedAt, actor, now);

        updatedAt = now;
        updatedBy = actor.trim();
    }
}
