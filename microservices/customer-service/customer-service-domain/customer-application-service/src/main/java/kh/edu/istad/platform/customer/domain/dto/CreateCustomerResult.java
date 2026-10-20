package kh.edu.istad.platform.customer.domain.dto;

import java.util.UUID;

public record CreateCustomerResult(
        UUID customerId,
        String username,
        String familyName,
        String givenName,
        String email,
        String phoneNumber
) {

}
