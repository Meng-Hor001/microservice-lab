package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;


public record UpdateUserProfileRequest(
        @Email @Size(min = 1, max = 255) String email,
        @Pattern(regexp = "(?s).*\\S.*") @Size(max = 100) String givenName,
        @Pattern(regexp = "(?s).*\\S.*") @Size(max = 100) String familyName,
        @Size(max = 100) String middleName,
        @Size(max = 100) String nickname,
        @Pattern(regexp = "female|male|other") String gender,
        @PastOrPresent LocalDate birthdate,
        @Size(max = 500) String profileUrl,
        @Size(max = 500) String pictureUrl,
        @Size(max = 500) String websiteUrl,
        @Size(max = 50) String zoneinfo,
        @Pattern(regexp = "en|km") String locale,
        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "must use E.164 format") String phoneNumber,
        @Valid AddressRequest address
) {}
