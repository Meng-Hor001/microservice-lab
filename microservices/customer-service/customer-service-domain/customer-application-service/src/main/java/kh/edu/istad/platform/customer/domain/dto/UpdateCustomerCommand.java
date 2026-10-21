package kh.edu.istad.platform.customer.domain.dto;

import java.util.UUID;

public record UpdateCustomerCommand (
        String username,
        String familyName,
        String givenName,
        String email,
        String phoneNumber,
        String segmentId
){}
