package kh.edu.istad.platform.iam.application.dto;

import java.time.Instant;
import java.util.UUID;

public record LoginHistoryResult(
        UUID id,
        String result,
        String method,
        String reason,
        String device,
        String browser,
        String os,
        String ip,
        String location,
        Instant at
) {
}
