package kh.edu.istad.platform.persistence.mapper;

import kh.edu.istad.platform.iam.application.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.persistence.entity.PermissionJpaEntity;
import kh.edu.istad.platform.persistence.repository.PermissionJpaRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    Permission toDomain(PermissionJpaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    PermissionJpaEntity toNewEntity(Permission permission);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(Permission permission, @MappingTarget PermissionJpaEntity entity);

    @ObjectFactory
    default PermissionJpaEntity createEntity() {
        return PermissionJpaEntity.create();
    }
}
