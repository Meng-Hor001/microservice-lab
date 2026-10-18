package kh.edu.istad.platform.payment.restapi.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TopUpResponse(
        UUID customerId,
        BigDecimal totalCreditAmount
) {
}
