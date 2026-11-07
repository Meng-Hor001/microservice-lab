package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.PermissionResult;
import kh.edu.istad.platform.iam.application.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.application.support.PermissionLookup;

import java.util.Objects;
import java.util.UUID;


public final class GetPermissionUseCase {
    private final PermissionRepository repository;

    public GetPermissionUseCase(PermissionRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public PermissionResult execute(UUID uuid) {
        return PermissionResult.from(
                PermissionLookup.require(repository, uuid)
        );
    }
}
