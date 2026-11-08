package kh.edu.istad.platform.persistence.mapper;

import kh.edu.istad.platform.iam.domain.entity.Identity;
import kh.edu.istad.platform.persistence.entity.IdentityJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface IdentityMapper {
    Identity toDomain(IdentityJpaEntity entity);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    IdentityJpaEntity toNewEntity(Identity identity);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(Identity identity, @MappingTarget IdentityJpaEntity entity);
    @ObjectFactory default IdentityJpaEntity createEntity() { return IdentityJpaEntity.create(); }
}
