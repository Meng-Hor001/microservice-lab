package kh.edu.istad.platform.payment.restapi.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID orderId,
        UUID customerId,
        String status,
        BigDecimal amount,
        BigDecimal remainingCredit,
        List<String> failureMessages
) {
}
