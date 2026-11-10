package kh.edu.istad.platform.iam.domain.entity;

import static kh.edu.istad.platform.iam.domain.support.DomainRules.*;

import java.util.Objects;


import kh.edu.istad.platform.iam.domain.valueobject.UserProfile;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    private Long id;
    private UUID uuid;

    private String username;
    private String email;
    private boolean emailVerified;
    private String passwordHash;

    private String givenName;
    private String familyName;
    private String middleName;
    private String nickname;

    private String gender;
    private LocalDate birthdate;

    private String profileUrl;
    private String pictureUrl;
    private String websiteUrl;

    private String zoneinfo;
    private String locale;

    private String phoneNumber;
    private boolean phoneNumberVerified;

    private String addressStreetAddress;
    private String addressLocality;
    private String addressRegion;
    private String addressPostalCode;
    private String addressCountry;

    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;

    private int failedLoginAttempts;
    private Instant lastLoginAt;
    private Instant passwordChangedAt;

    private boolean deleted;
    private Instant deletedAt;
    private String deletedBy;

    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    private long version;

    public static class UserBuilder {

        private boolean enabled = true;
        private boolean accountNonExpired = true;
        private boolean accountNonLocked = true;
        private boolean credentialsNonExpired = true;

        public User build() {
            User user = new User(
                    id, uuid,
                    username, email, emailVerified, passwordHash,
                    givenName, familyName, middleName, nickname,
                    gender, birthdate,
                    profileUrl, pictureUrl, websiteUrl,
                    zoneinfo, locale,
                    phoneNumber, phoneNumberVerified,
                    addressStreetAddress,
                    addressLocality,
                    addressRegion,
                    addressPostalCode,
                    addressCountry,
                    enabled,
                    accountNonExpired,
                    accountNonLocked,
                    credentialsNonExpired,
                    failedLoginAttempts,
                    lastLoginAt,
                    passwordChangedAt,
                    deleted, deletedAt, deletedBy,
                    createdAt, createdBy,
                    updatedAt == null ? createdAt : updatedAt,
                    updatedBy == null ? createdBy : updatedBy,
                    version
            );

            user.validate();
            return user;
        }
    }

    private void validate() {
        uuid = publicId(id, uuid);
        username = text(username, "Username", 50);

        validatePasswordHash(passwordHash);

        check(
                failedLoginAttempts >= 0,
                "Failed login attempts cannot be negative"
        );

        applyProfile(new UserProfile(
                email,
                givenName,
                familyName,
                middleName,
                nickname,
                gender,
                birthdate,
                profileUrl,
                pictureUrl,
                websiteUrl,
                zoneinfo,
                locale,
                phoneNumber,
                addressStreetAddress,
                addressLocality,
                addressRegion,
                addressPostalCode,
                addressCountry
        ));

        check(
                !phoneNumberVerified || phoneNumber != null,
                "A missing phone number cannot be verified"
        );

        audit(
                deleted, deletedAt, deletedBy,
                createdAt, createdBy,
                updatedAt, updatedBy,
                version
        );

        if (lastLoginAt != null) {
            check(
                    !lastLoginAt.isAfter(updatedAt),
                    "Last login cannot be after the latest update"
            );
        }

        if (passwordChangedAt != null) {
            check(
                    !passwordChangedAt.isAfter(updatedAt),
                    "Password change cannot be after the latest update"
            );
        }
    }

    public String getName() {
        return givenName + " " + familyName;
    }

    public void updateProfile(
            UserProfile profile,
            String actor,
            Instant now
    ) {
        required(profile, "Profile");
        mutation(deleted, updatedAt, actor, now);

        boolean emailChanged = !email.equals(profile.email());

        boolean phoneChanged = !Objects.equals(
                phoneNumber,
                profile.phoneNumber()
        );

        applyProfile(profile);

        if (emailChanged) {
            emailVerified = false;
        }

        if (phoneChanged) {
            phoneNumberVerified = false;
        }

        updatedAt = now;
        updatedBy = actor.trim();
    }

    private void applyProfile(UserProfile profile) {
        email = profile.email();
        givenName = profile.givenName();
        familyName = profile.familyName();
        middleName = profile.middleName();
        nickname = profile.nickname();
        gender = profile.gender();
        birthdate = profile.birthdate();
        profileUrl = profile.profileUrl();
        pictureUrl = profile.pictureUrl();
        websiteUrl = profile.websiteUrl();
        zoneinfo = profile.zoneinfo();
        locale = profile.locale();
        phoneNumber = profile.phoneNumber();
        addressStreetAddress = profile.addressStreetAddress();
        addressLocality = profile.addressLocality();
        addressRegion = profile.addressRegion();
        addressPostalCode = profile.addressPostalCode();
        addressCountry = profile.addressCountry();
    }

    public void enable(String actor, Instant now) {
        touch(actor, now);
        enabled = true;
    }

    public void disable(String actor, Instant now) {
        touch(actor, now);
        enabled = false;
    }

    public void lock(String actor, Instant now) {
        touch(actor, now);
        accountNonLocked = false;
    }

    public void unlock(String actor, Instant now) {
        touch(actor, now);
        accountNonLocked = true;
    }

    public void expireAccount(String actor, Instant now) {
        touch(actor, now);
        accountNonExpired = false;
    }

    public void expireCredentials(String actor, Instant now) {
        touch(actor, now);
        credentialsNonExpired = false;
    }

    public void verifyEmail(String actor, Instant now) {
        touch(actor, now);
        emailVerified = true;
    }

    public void verifyPhoneNumber(String actor, Instant now) {
        check(phoneNumber != null, "Phone number is missing");
        touch(actor, now);
        phoneNumberVerified = true;
    }

    public void changePasswordHash(
            String passwordHash,
            String actor,
            Instant now
    ) {
        required(passwordHash, "Password hash");
        validatePasswordHash(passwordHash);

        touch(actor, now);

        this.passwordHash = passwordHash;
        passwordChangedAt = now;
        credentialsNonExpired = true;
    }

    public void recordLoginAttempt(
            boolean success,
            String actor,
            Instant now
    ) {
        if (success) {
            check(canSignIn(), "User cannot sign in");
        }

        touch(actor, now);

        if (success) {
            failedLoginAttempts = 0;
            lastLoginAt = now;
        } else if (failedLoginAttempts < Integer.MAX_VALUE) {
            failedLoginAttempts++;
        }
    }

    public void softDelete(String actor, Instant now) {
        touch(actor, now);

        deleted = true;
        deletedAt = now;
        deletedBy = actor.trim();
        enabled = false;
    }

    public boolean canSignIn() {
        return !deleted
                && enabled
                && accountNonExpired
                && accountNonLocked;
    }

    public boolean canSignInWithPassword() {
        return canSignIn()
                && hasUsablePassword();
    }

    public boolean hasUsablePassword() {
        return passwordHash != null
                && credentialsNonExpired;
    }

    private static void validatePasswordHash(String hash) {
        if (hash != null) {
            check(!hash.isBlank(), "Password hash cannot be blank");
            check(hash.length() <= 255, "Password hash is too long");
        }
    }

    private void touch(String actor, Instant now) {
        mutation(deleted, updatedAt, actor, now);

        updatedAt = now;
        updatedBy = actor.trim();
    }
}
