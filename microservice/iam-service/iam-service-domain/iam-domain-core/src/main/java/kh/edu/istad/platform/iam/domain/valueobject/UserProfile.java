package kh.edu.istad.platform.iam.domain.valueobject;

import static kh.edu.istad.platform.iam.domain.support.DomainRules.*;

import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;


import java.time.LocalDate;

public record UserProfile(
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
    public UserProfile {
        email = kh.edu.istad.platform.iam.domain.support.DomainRules.email(
                email, true
        );

        givenName = text(givenName, "Given name", 100);
        familyName = text(familyName, "Family name", 100);
        middleName = optional(middleName, "Middle name", 100);
        nickname = optional(nickname, "Nickname", 100);
        gender = optional(gender, "Gender", 20);

        if (gender != null) {
            check(
                    Set.of("female", "male", "other").contains(gender),
                    "Gender must be female, male, or other"
            );
        }

        profileUrl = optional(profileUrl, "Profile URL", 500);
        pictureUrl = optional(pictureUrl, "Picture URL", 500);
        websiteUrl = optional(websiteUrl, "Website URL", 500);

        zoneinfo = optional(zoneinfo, "Time zone", 50);

        if (zoneinfo != null) {
            check(
                    ZoneId.getAvailableZoneIds().contains(zoneinfo),
                    "Invalid IANA time zone"
            );
        }

        locale = text(locale, "Locale", 10);
        check(Set.of("en", "km").contains(locale), "Locale must be en or km");

        phoneNumber = optional(phoneNumber, "Phone number", 20);

        if (phoneNumber != null) {
            check(
                    phoneNumber.matches("^\\+[1-9]\\d{1,14}$"),
                    "Phone number must use E.164 format"
            );
        }

        addressStreetAddress = optional(
                addressStreetAddress, "Street address", 255
        );

        addressLocality = optional(addressLocality, "Locality", 100);
        addressRegion = optional(addressRegion, "Region", 100);
        addressPostalCode = optional(addressPostalCode, "Postal code", 20);
        addressCountry = optional(addressCountry, "Country", 2);

        if (addressCountry != null) {
            addressCountry = addressCountry.toUpperCase(Locale.ROOT);

            check(
                    Locale.getISOCountries(
                            Locale.IsoCountryCode.PART1_ALPHA2
                    ).contains(addressCountry),
                    "Country must be an ISO two-letter country code"
            );
        }
    }
}
