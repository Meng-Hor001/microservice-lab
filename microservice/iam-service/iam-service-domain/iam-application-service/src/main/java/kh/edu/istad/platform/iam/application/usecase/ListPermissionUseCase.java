package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.dto.PermissionSummary;
import kh.edu.istad.platform.iam.application.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.application.query.PermissionFilterQuery;
import kh.edu.istad.platform.iam.domain.entity.Permission;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ListPermissionUseCase {
    private final PermissionRepository repository;

    public ListPermissionUseCase(PermissionRepository repository) {
        this.repository = repository;
    }

    public PageResult<PermissionSummary> execute(
            PermissionFilterQuery query
    ) {
        Objects.requireNonNull(query, "Permission filter is required");
        PageResult<Permission> page = repository.findAll(query);

        // One count query for the whole page, not one per permission.
        List<Long> ids = page.content().stream().map(Permission::getId).toList();
        Map<Long, Long> roles = repository.countRoles(ids);

        return page.map(permission -> PermissionSummary.from(
                permission,
                roles.getOrDefault(permission.getId(), 0L)
        ));
    }
}
