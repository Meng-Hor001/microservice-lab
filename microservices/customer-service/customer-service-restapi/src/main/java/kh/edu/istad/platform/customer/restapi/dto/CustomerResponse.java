package kh.edu.istad.platform.customer.restapi.dto;

import java.util.UUID;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerStatus;

public record CustomerResponse(
        UUID customerId,
        String username,
        String familyName,
        String givenName,
        String email,
        String phoneNumber,
        String segmentId,
        CustomerStatus status
) {
}
