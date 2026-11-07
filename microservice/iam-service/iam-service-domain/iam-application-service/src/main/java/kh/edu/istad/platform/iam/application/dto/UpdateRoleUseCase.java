package kh.edu.istad.platform.iam.application.dto;

import java.util.UUID;

public record UpdateRoleUseCase(
        UUID uuid,
        String name,
        String description,
        long version
) {
}
