package kh.edu.istad.platform.customer.domain.dto;

public record InitiateCustomerCommand(
        String username,
        String familyName,
        String givenName,
        String email,
        String phoneNumber
) {
}
