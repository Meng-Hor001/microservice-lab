package kh.edu.istad.platform.customer.restapi.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

public record CustomerDeactivateResponse (
        UUID customerId,
        ZonedDateTime deactivatedAt
){
}
