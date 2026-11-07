package kh.edu.istad.platform.iam.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateUserProfileCommand(
        UUID userUuid,
        String email,
        String givenName,
        String familyName,
        String middleName,
        String nickname,
        String gender,
        LocalDate birthdate,
        String profileUrl,
        String pictureUrl,
        String websiteUrl,
        String zoneinfo,
        String locale,
        String phoneNumber,
        String addressStreetAddress,
        String addressLocality,
        String addressRegion,
        String addressPostalCode,
        String addressCountry
) {
}
