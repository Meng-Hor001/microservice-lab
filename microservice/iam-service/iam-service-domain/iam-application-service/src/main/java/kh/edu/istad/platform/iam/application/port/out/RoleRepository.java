package kh.edu.istad.platform.iam.application.port.out;

import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.query.RoleFilterQuery;
import kh.edu.istad.platform.iam.domain.entity.Role;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {
    boolean existsByName(String name);

    Optional<Role> findByUuid(UUID uuid);

    Optional<Role> findById(Long id);

    Role save(Role role);


    Optional<Long> findIdByUuid(UUID uuid);

    PageResult<Role> findAll(RoleFilterQuery query);

    /** Role id → number of permissions it currently grants. Roles with none are left out. */
    Map<Long, Long> countPermissions(Collection<Long> roleIds);

    /** Role id → number of users who currently hold it. Roles with none are left out. */
    Map<Long, Long> countUsers(Collection<Long> roleIds);
}
