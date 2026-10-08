package kh.edu.istad.platform.customer.restapi.dto;

import java.util.UUID;

public record CustomerUpdateResponse(
        UUID customerId,
        String familyName,
        String givenName
) {}
