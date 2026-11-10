package kh.edu.istad.platform.iam.application.support;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.entity.Permission;

import java.util.Objects;
import java.util.UUID;

public final class PermissionLookup {
    private PermissionLookup() {
    }

    public static Permission require(
            PermissionRepository repository,
            UUID uuid
    ) {
        Objects.requireNonNull(uuid, "Permission UUID is required");

        return repository.findByUuid(uuid)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Permission",
                                uuid
                        )
                );
    }
}
