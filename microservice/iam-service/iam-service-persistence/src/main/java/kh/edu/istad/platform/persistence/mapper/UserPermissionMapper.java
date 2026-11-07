package kh.edu.istad.platform.persistence.mapper;

import kh.edu.istad.platform.iam.domain.entity.UserPermission;
import kh.edu.istad.platform.persistence.entity.UserPermissionJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface UserPermissionMapper {
    UserPermission toDomain(UserPermissionJpaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    UserPermissionJpaEntity toNewEntity(UserPermission assignment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(
            UserPermission assignment,
            @MappingTarget UserPermissionJpaEntity entity
    );

    @ObjectFactory
    default UserPermissionJpaEntity createEntity() {
        return UserPermissionJpaEntity.create();
    }
}
