package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.dto.PermissionResult;
import kh.edu.istad.platform.iam.application.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.application.query.PermissionFilterQuery;

import java.util.Objects;

public final class ListPermissionUseCase {
    private final PermissionRepository repository;

    public ListPermissionUseCase(PermissionRepository repository) {
        this.repository = repository;
    }


    public PageResult<PermissionResult> execute(
            PermissionFilterQuery query
    ) {
        Objects.requireNonNull(query, "Permission filter is required");

        return repository.findAll(query)
                .map(PermissionResult::from);
    }
}
