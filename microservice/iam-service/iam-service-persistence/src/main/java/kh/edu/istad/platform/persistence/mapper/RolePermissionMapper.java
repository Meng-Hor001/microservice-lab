package kh.edu.istad.platform.persistence.mapper;

import kh.edu.istad.platform.iam.domain.entity.RolePermission;
import kh.edu.istad.platform.persistence.entity.RolePermissionJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface RolePermissionMapper {
    RolePermission toDomain(RolePermissionJpaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    RolePermissionJpaEntity toNewEntity(RolePermission assignment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(
            RolePermission assignment,
            @MappingTarget RolePermissionJpaEntity entity
    );

    @ObjectFactory
    default RolePermissionJpaEntity createEntity() {
        return RolePermissionJpaEntity.create();
    }
}
