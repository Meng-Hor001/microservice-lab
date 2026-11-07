package kh.edu.istad.platform.persistence.mapper;

import kh.edu.istad.platform.iam.domain.entity.UserRole;
import kh.edu.istad.platform.persistence.entity.UserRoleJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface UserRoleMapper {
    UserRole toDomain(UserRoleJpaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    UserRoleJpaEntity toNewEntity(UserRole assignment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(
            UserRole assignment,
            @MappingTarget UserRoleJpaEntity entity
    );

    @ObjectFactory
    default UserRoleJpaEntity createEntity() {
        return UserRoleJpaEntity.create();
    }
}
