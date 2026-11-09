package kh.edu.istad.platform.iam.application.dto;

import kh.edu.istad.platform.iam.domain.entity.User;

import java.time.LocalDate;
import java.util.UUID;

public record AuthUserResult(
        UUID uuid,
        String username,
        String email,
        String passwordHash,
        String givenName,
        String familyName,
        String middleName,
        String nickname,
        String profileUrl,
        String pictureUrl,
        String websiteUrl,
        String gender,
        LocalDate birthdate,
        String zoneinfo,
        String locale,
        boolean emailVerified,
        boolean enabled,
        boolean accountNonExpired,
        boolean accountNonLocked,
        boolean credentialsNonExpired
) {
    public static AuthUserResult from(User user) {
        return new AuthUserResult(
                user.getUuid(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getGivenName(),
                user.getFamilyName(),
                user.getMiddleName(),
                user.getNickname(),
                user.getProfileUrl(),
                user.getPictureUrl(),
                user.getWebsiteUrl(),
                user.getGender(),
                user.getBirthdate(),
                user.getZoneinfo(),
                user.getLocale(),
                user.isEmailVerified(),
                user.isEnabled(),
                user.isAccountNonExpired(),
                user.isAccountNonLocked(),
                user.isCredentialsNonExpired()
        );
    }
}
