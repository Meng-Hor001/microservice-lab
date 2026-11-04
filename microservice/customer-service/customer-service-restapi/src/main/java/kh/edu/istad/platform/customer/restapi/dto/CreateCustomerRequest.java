package kh.edu.istad.platform.customer.restapi.dto;

public record CreateCustomerRequest(
        String username,
        String familyName,
        String givenName,
        String email,
        String phoneNumber
) {
}
