package kh.edu.istad.platform.iam.application.dto;

import java.time.Instant;
import java.util.UUID;

public record AccountSessionResult(
        UUID id,
        boolean current,
        String type,
        String device,
        String browser,
        String os,
        String ip,
        String location,
        Instant signedIn,
        Instant lastActive
) {
}
