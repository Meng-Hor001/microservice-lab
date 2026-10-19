package kh.edu.istad.platform.business.restapi.dto;

import java.util.List;
import java.util.UUID;

public record DecisionMessage(
        UUID approvalId,
        UUID businessId,
        String status,
        List<String> failureMessages
){
}
