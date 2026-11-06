package kh.edu.istad.platform.iam.valueobject;

public record Address(
        String streetAddress,
        String locality,
        String region,
        String postalCode,
        String country
) {
}
