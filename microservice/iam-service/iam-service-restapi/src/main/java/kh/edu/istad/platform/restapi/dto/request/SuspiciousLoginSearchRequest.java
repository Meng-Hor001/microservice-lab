package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** Query parameters of GET /api/login-history/suspicious. */
public record SuspiciousLoginSearchRequest(
        @Min(1) @Max(365) Integer days,
        @Min(1) @Max(1000) Integer threshold
) {
    public int daysOrDefault() {
        return days == null ? 1 : days;
    }

    public int thresholdOrDefault() {
        return threshold == null ? 5 : threshold;
    }
}
