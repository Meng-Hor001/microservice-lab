package kh.edu.istad.platform.order.restapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ApprovalRequest(
        @NotNull
        UUID approvalId,
        @NotNull
        UUID businessId,
        @NotNull
        @Pattern(regexp = "APPROVED|REJECTED")
        String status,
        @NotNull
        @NotBlank
        @Size(max = 1000)
        List< String> failureMessages
) {
}
