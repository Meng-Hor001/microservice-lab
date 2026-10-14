package kh.edu.istad.platform.order.domain.valueobject;

import kh.edu.istad.platform.order.domain.exception.DomainException;

import java.util.UUID;

public record StreetAddress(
        UUID id,
        String street,
        String postalCode,
        String city
) {
    public StreetAddress {
        if (id == null){
            throw new DomainException("Address ID is required");
        }

        street = validateText (street, "street", 255);
        postalCode = validateText (postalCode, "Postal Code", 30);
        city = validateText (city, "city", 100);
    }

    private static  String validateText(
            String value,
            String field,
            int maxLength
    ){
        if (value == null || value.isBlank()){
            throw new DomainException(field + "is required");
        }

        String normalized = value.strip();

        if (normalized.length() > maxLength){
            throw new DomainException(field + "must not exceed" + maxLength + "character");
        }
        return normalized;
    }
}
