package kh.edu.istad.platform.iam.application.dto;

import java.util.UUID;

public record UpdateRoleCommand(
        UUID uuid,
        String name,
        String description,
        long version
) {
}
