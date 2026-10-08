package kh.edu.istad.platform.customer.domain.dto;

import java.util.UUID;

public record UpdateCustomerCommand (
        UUID customerId,
        String familyName,
        String givenName
){}
