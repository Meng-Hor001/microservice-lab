package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.dto.RoleSummary;
import kh.edu.istad.platform.iam.application.port.out.RoleRepository;
import kh.edu.istad.platform.iam.application.query.RoleFilterQuery;

import java.util.Objects;

public final class ListRolesUseCase {
    private final RoleRepository roles;

    public ListRolesUseCase(RoleRepository roles) {
        this.roles = Objects.requireNonNull(roles);
    }

    public PageResult<RoleSummary> execute(RoleFilterQuery query) {
        Objects.requireNonNull(query, "Role filter is required");
        return roles.findAll(query).map(RoleSummary::from);
    }
}
