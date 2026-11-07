package kh.edu.istad.platform.iam.domain.entity;


import kh.edu.istad.platform.iam.domain.enums.IdentityProvider;
import kh.edu.istad.platform.iam.domain.support.JsonAttributes;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static kh.edu.istad.platform.iam.domain.support.DomainRules.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Identity {
    private Long id;
    private UUID uuid;
    private Long userId;

    private IdentityProvider provider;
    private String providerSubject;

    private String email;
    private boolean emailVerified;
    private String displayName;
    private String username;
    private String pictureUrl;
    private Map<String, Object> attributes;

    private Instant linkedAt;
    private Instant lastLoginAt;

    private boolean deleted;
    private Instant deletedAt;
    private String deletedBy;

    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    private long version;

    public static class IdentityBuilder {

        public Identity build() {
            Identity identity = new Identity(
                    id, uuid, userId,
                    provider, providerSubject,
                    email, emailVerified,
                    displayName, username, pictureUrl, attributes,
                    linkedAt == null ? createdAt : linkedAt,
                    lastLoginAt,
                    deleted, deletedAt, deletedBy,
                    createdAt, createdBy,
                    updatedAt == null ? createdAt : updatedAt,
                    updatedBy == null ? createdBy : updatedBy,
                    version
            );

            identity.validate();
            return identity;
        }
    }

    private void validate() {
        uuid = publicId(id, uuid);
        persistedId(userId, "User ID");
        required(provider, "Identity provider");

        providerSubject = text(
                providerSubject, "Provider subject", 255
        );

        email = kh.edu.istad.platform.iam.domain.support.DomainRules.email(
                email, false
        );

        check(
                !emailVerified || email != null,
                "A missing email cannot be verified"
        );

        displayName = optional(displayName, "Display name", 150);
        username = optional(username, "Provider username", 100);
        pictureUrl = optional(pictureUrl, "Picture URL", 500);
        attributes = JsonAttributes.copy(attributes);

        required(linkedAt, "Linked timestamp");

        audit(
                deleted, deletedAt, deletedBy,
                createdAt, createdBy,
                updatedAt, updatedBy,
                version
        );

        check(
                !linkedAt.isBefore(createdAt)
                        && !linkedAt.isAfter(updatedAt),
                "Linked timestamp must be within the identity audit period"
        );

        if (lastLoginAt != null) {
            check(
                    !lastLoginAt.isBefore(linkedAt)
                            && !lastLoginAt.isAfter(updatedAt),
                    "Invalid identity login timestamp"
            );
        }
    }

    public void ensureCanLink(
            User owner,
            Collection<Identity> existingIdentities
    ) {
        required(owner, "Owner");
        required(existingIdentities, "Existing identities");

        check(!deleted, "Deleted identity cannot be linked");
        check(!owner.isDeleted(), "Deleted user cannot link identities");

        check(
                userId.equals(owner.getId()),
                "Identity does not belong to this user"
        );

        boolean duplicateProvider = existingIdentities.stream()
                .filter(identity -> identity.getUserId().equals(userId))
                .filter(identity -> !identity.isDeleted())
                .anyMatch(identity -> identity.getProvider() == provider);

        check(
                !duplicateProvider,
                "User already has an identity for this provider"
        );
    }

    public void recordLogin(String actor, Instant now) {
        mutation(deleted, updatedAt, actor, now);

        check(
                !now.isBefore(linkedAt),
                "Login cannot precede identity linking"
        );

        lastLoginAt = now;
        updatedAt = now;
        updatedBy = actor.trim();
    }

    public void updateProviderProfile(
            String email,
            boolean emailVerified,
            String displayName,
            String username,
            String pictureUrl,
            Map<String, Object> attributes,
            String actor,
            Instant now
    ) {
        String newEmail =
                kh.edu.istad.platform.iam.domain.support.DomainRules.email(
                        email, false
                );

        check(
                !emailVerified || newEmail != null,
                "A missing email cannot be verified"
        );

        String newDisplayName = optional(
                displayName, "Display name", 150
        );

        String newUsername = optional(
                username, "Provider username", 100
        );

        String newPictureUrl = optional(
                pictureUrl, "Picture URL", 500
        );

        Map<String, Object> newAttributes = JsonAttributes.copy(attributes);

        mutation(deleted, updatedAt, actor, now);

        this.email = newEmail;
        this.emailVerified = emailVerified;
        this.displayName = newDisplayName;
        this.username = newUsername;
        this.pictureUrl = newPictureUrl;
        this.attributes = newAttributes;

        updatedAt = now;
        updatedBy = actor.trim();
    }

    public void unlink(
            User owner,
            Collection<Identity> existingIdentities,
            String actor,
            Instant now
    ) {
        required(owner, "Owner");
        required(existingIdentities, "Existing identities");

        check(!owner.isDeleted(), "User has been deleted");

        check(
                userId.equals(owner.getId()),
                "Identity does not belong to this user"
        );

        boolean anotherIdentityExists = existingIdentities.stream()
                .filter(identity -> identity.getUserId().equals(userId))
                .filter(identity -> !identity.isDeleted())
                .anyMatch(identity -> !identity.getUuid().equals(uuid));

        check(
                owner.hasUsablePassword() || anotherIdentityExists,
                "Cannot unlink the last usable sign-in method"
        );

        mutation(deleted, updatedAt, actor, now);

        deleted = true;
        deletedAt = now;
        deletedBy = actor.trim();
        updatedAt = now;
        updatedBy = actor.trim();
    }

}
