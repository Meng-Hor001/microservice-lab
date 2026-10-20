package kh.edu.istad.platform.customer.restapi.dto;

import jakarta.validation.constraints.NotNull;
import kh.edu.istad.platform.customer.domain.valueobject.SegmentName;

public record SegmentRequest(
        @NotNull(message = "Segment name is required")
        SegmentName name,

        String description
) {
}
