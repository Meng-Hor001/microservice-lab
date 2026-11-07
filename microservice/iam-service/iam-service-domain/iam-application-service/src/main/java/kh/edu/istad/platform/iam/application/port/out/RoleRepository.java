package kh.edu.istad.platform.iam.application.port.out;

import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.query.RoleFilterQuery;
import kh.edu.istad.platform.iam.domain.entity.Role;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {
    boolean existsByName(String name);

    Optional<Role> findByUuid(UUID uuid);

    Role save(Role role);


    Optional<Long> findIdByUuid(UUID uuid);
    PageResult<Role> findAll(RoleFilterQuery query);
}
