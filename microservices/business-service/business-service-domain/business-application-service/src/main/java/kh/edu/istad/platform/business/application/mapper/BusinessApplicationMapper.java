package kh.edu.istad.platform.business.application.mapper;

import kh.edu.istad.platform.business.application.dto.RegisterBusinessResult;
import kh.edu.istad.platform.business.domain.entity.Business;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BusinessApplicationMapper {

    @Mapping(target = "businessId", source = "id.value")
    @Mapping(target = "legalName", source = "name.legalName")
    @Mapping(target = "displayName", source = "name.displayName")
    @Mapping(target = "email", source = "contact.email")
    @Mapping(target = "phoneNumber", source = "contact.phoneNumber")
    RegisterBusinessResult toResult(Business business);
}
