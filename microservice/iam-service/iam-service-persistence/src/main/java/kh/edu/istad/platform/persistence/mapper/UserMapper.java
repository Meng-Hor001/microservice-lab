package kh.edu.istad.platform.persistence.mapper;

import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.persistence.entity.UserJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toDomain(UserJpaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    UserJpaEntity toNewEntity(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(User user, @MappingTarget UserJpaEntity entity);

    @ObjectFactory
    default UserJpaEntity createEntity() {
        return UserJpaEntity.create();
    }
}
