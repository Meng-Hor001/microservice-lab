package kh.edu.istad.platform.persistence.mapper;

import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.persistence.entity.RoleJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    Role toDomain(RoleJpaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    RoleJpaEntity toNewEntity(Role role);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(Role role, @MappingTarget RoleJpaEntity entity);

    @ObjectFactory
    default RoleJpaEntity createEntity(){
        return RoleJpaEntity.create();
    }

}
