package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.dto.RoleSummary;
import kh.edu.istad.platform.iam.application.port.out.RoleRepository;
import kh.edu.istad.platform.iam.application.query.RoleFilterQuery;
import kh.edu.istad.platform.iam.domain.entity.Role;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ListRolesUseCase {
    private final RoleRepository roles;

    public ListRolesUseCase(RoleRepository roles) {
        this.roles = Objects.requireNonNull(roles);
    }

    public PageResult<RoleSummary> execute(RoleFilterQuery query) {
        Objects.requireNonNull(query, "Role filter is required");
        PageResult<Role> page = roles.findAll(query);

        // One count query each for the whole page, not one per role.
        List<Long> ids = page.content().stream().map(Role::getId).toList();
        Map<Long, Long> permissions = roles.countPermissions(ids);
        Map<Long, Long> users = roles.countUsers(ids);

        return page.map(role -> RoleSummary.from(
                role,
                permissions.getOrDefault(role.getId(), 0L),
                users.getOrDefault(role.getId(), 0L)
        ));
    }
}
