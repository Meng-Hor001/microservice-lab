package kh.edu.istad.platform.iam.application.port.out;

import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.query.PermissionFilterQuery;
import kh.edu.istad.platform.iam.domain.entity.Permission;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository {

    boolean existsByName(String name);

    Optional<Permission> findByUuid(UUID uuid);

    Optional<Permission> findById(Long id);

    PageResult<Permission> findAll(PermissionFilterQuery query);

    Permission save(Permission permission);
}
