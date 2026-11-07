package kh.edu.istad.platform.iam.application.dto;

import java.util.UUID;

public record UpdatePermissionCommand(
        UUID id,
        String resource,
        String action,
        String description,
        long version
) {
}
