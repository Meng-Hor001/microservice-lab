package kh.edu.istad.platform.business.restapi.dto;

import java.util.List;
import java.util.UUID;

public record OrderApprovalResponse(
        UUID orderApprovalId,
        UUID businessId,
        UUID orderId,
        String status,
        List<String> failureMessages
) {
}
