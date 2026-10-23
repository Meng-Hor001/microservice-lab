package kh.edu.istad.common.domain.valueobject;

import java.util.UUID;

public record StreetAddress(
        UUID id,
        String address,
        String postalCode,
        String city) {
}
